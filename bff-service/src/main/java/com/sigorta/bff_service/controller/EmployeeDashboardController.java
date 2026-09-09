package com.sigorta.bff_service.controller;

import com.sigorta.bff_service.dto.PolicyValidationRequest;
import com.sigorta.bff_service.dto.PolicyValidationResponse;
import com.sigorta.bff_service.service.EmployeeDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Employee Dashboard Controller
 * 
 * Sigorta çalışanları için AI-powered dashboard endpoint'leri sağlar.
 * 
 * Özellikler:
 * - Poliçe validasyon: "Bu kullanıcının poliçesi buna uygun mu?"
 * - Doküman önerisi: "Şu dosyaları kontrol et"
 * - AI-powered policy analysis
 * - Kapsam kontrolü ve ödeme tahmini
 * 
 * @author Insurance Development Team
 */
@RestController
@RequestMapping("/api/bff/employee")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employee Dashboard", description = "AI-powered employee dashboard for policy validation and document recommendations")
public class EmployeeDashboardController {
    
    private final EmployeeDashboardService employeeDashboardService;
    
    /**
     * Poliçe Validasyon
     * 
     * Çalışan sorusu: "Müşteri hastanede x işlemini gerçekleştiriyor, 
     * bu kullanıcının poliçesi buna uygun mu?"
     * 
     * AI kullanarak poliçe kapsamını analiz eder ve detaylı yanıt verir.
     * 
     * @param request Validasyon isteği (müşteri bilgileri, işlem detayları)
     * @return AI analizli validasyon cevabı
     */
    @PostMapping("/validate-policy")
    @Operation(summary = "Validate policy coverage for medical procedure",
            description = "AI analyzes if the policy covers the medical procedure and provides detailed coverage information. " +
                    "This is the main endpoint for employee dashboard where agents ask: 'Does this policy cover this procedure?'")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy validation completed successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error during validation")
    })
    public ResponseEntity<PolicyValidationResponse> validatePolicy(
            @Parameter(description = "Policy validation request with customer info and medical procedure details", 
                    required = true)
            @RequestBody PolicyValidationRequest request) {
        
        log.info("Policy validation request for policy: {}, procedure: {}", 
                request.getPolicyId(), request.getMedicalProcedure());
        
        try {
            PolicyValidationResponse response = employeeDashboardService.validatePolicy(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating policy", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Doküman Önerisi
     * 
     * Çalışan sorusu: "Bu durum için hangi dokümanları kontrol etmeliyim?"
     * 
     * AI kullanarak gerekli dokümanları listeler ve öncelik sıralar.
     * 
     * @param request Validasyon isteği (durum detayları)
     * @return Gerekli doküman listesi ve öneriler
     */
    @PostMapping("/recommend-documents")
    @Operation(summary = "Get document recommendations for claim processing",
            description = "AI recommends required documents and their priority for the given situation. " +
                    "Agents can ask: 'What documents should I check for this case?'")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document recommendations generated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error during recommendation")
    })
    public ResponseEntity<Map<String, Object>> recommendDocuments(
            @Parameter(description = "Request with procedure details for document recommendation", required = true)
            @RequestBody PolicyValidationRequest request) {
        
        log.info("Document recommendation request for policy: {}, procedure: {}", 
                request.getPolicyId(), request.getMedicalProcedure());
        
        try {
            Map<String, Object> response = employeeDashboardService.recommendDocuments(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error recommending documents", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Document recommendation failed: " + e.getMessage()));
        }
    }
    
    /**
     * Hızlı Policy Check
     * 
     * Basit soru-yanıt formatında hızlı kontrol
     * 
     * @param question Çalışan sorusu
     * @param policyId Poliçe ID
     * @return AI yanıtı
     */
    @GetMapping("/quick-check")
    @Operation(summary = "Quick policy check with AI",
            description = "Ask a quick question about policy coverage and get AI response. " +
                    "Useful for quick inquiries like 'Does this policy cover check-up?'")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quick check completed successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error during quick check")
    })
    public ResponseEntity<Map<String, String>> quickCheck(
            @Parameter(description = "Question about policy coverage", required = true, example = "Does this policy cover check-up?")
            @RequestParam String question,
            @Parameter(description = "Policy ID to check", required = true)
            @RequestParam String policyId) {
        
        log.info("Quick check request for policy: {}, question: {}", policyId, question);
        
        try {
            Map<String, String> response = employeeDashboardService.quickCheck(question, policyId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in quick check", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Quick check failed: " + e.getMessage()));
        }
    }
    
    /**
     * Toplu Policy Validasyon
     * 
     * Birden fazla işlem için aynı anda validasyon
     * 
     * @param requests Validasyon istekleri listesi
     * @return Validasyon cevapları listesi
     */
    @PostMapping("/validate-policies-batch")
    @Operation(summary = "Batch policy validation",
            description = "Validate multiple procedures at once for efficiency. " +
                    "Useful when processing multiple claims for the same customer or comparing different scenarios.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Batch validation completed successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error during batch validation")
    })
    public ResponseEntity<List<PolicyValidationResponse>> validatePoliciesBatch(
            @Parameter(description = "List of policy validation requests", required = true)
            @RequestBody List<PolicyValidationRequest> requests) {
        
        log.info("Batch policy validation request for {} procedures", requests.size());
        
        try {
            List<PolicyValidationResponse> responses = 
                    employeeDashboardService.validatePoliciesBatch(requests);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error in batch validation", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Policy Comparison
     * 
     * İki poliçe arasındaki farkları karşılaştır
     * 
     * @param policyId1 İlk poliçe ID
     * @param policyId2 İkinci poliçe ID
     * @return Karşılaştırma sonucu
     */
    @GetMapping("/compare-policies")
    @Operation(summary = "Compare two policies",
            description = "AI compares coverage, limits, and features between two policies. " +
                    "Useful for upselling or helping customers choose the right policy.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy comparison completed successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error during comparison")
    })
    public ResponseEntity<Map<String, Object>> comparePolicies(
            @Parameter(description = "First policy ID to compare", required = true)
            @RequestParam String policyId1,
            @Parameter(description = "Second policy ID to compare", required = true)
            @RequestParam String policyId2) {
        
        log.info("Policy comparison request: {} vs {}", policyId1, policyId2);
        
        try {
            Map<String, Object> response = employeeDashboardService.comparePolicies(policyId1, policyId2);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error comparing policies", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Policy comparison failed: " + e.getMessage()));
        }
    }
    
    /**
     * Hata response'u oluşturma
     */
    private PolicyValidationResponse createErrorResponse(String errorMessage) {
        return PolicyValidationResponse.builder()
                .isCovered(false)
                .decision("HATA")
                .explanation("Validasyon sırasında hata oluştu: " + errorMessage)
                .confidence(0.0)
                .analysisSource("ERROR")
                .build();
    }
}