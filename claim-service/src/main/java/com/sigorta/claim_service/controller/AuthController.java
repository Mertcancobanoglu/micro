package com.sigorta.claim_service.controller;

import com.sigorta.claim_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    /**
     * Local Development Token Endpoint
     * 
     * Bu endpoint sadece development ortamında kullanılmalıdır.
     * Production ortamında kaldırılmalı veya güvenli hale getirilmelidir.
     * 
     * @param username Kullanıcı adı
     * @return JWT token
     */
    @PostMapping("/local-token")
    public ResponseEntity<Map<String, String>> generateLocalToken(
            @RequestParam(defaultValue = "localUser") String username) {
        
        // Development için basit bir token üretimi
        String token = jwtUtil.generateToken(username);
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("type", "Bearer");
        response.put("username", username);
        response.put("message", "Development token - Production kullanmayın!");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Token Validation Endpoint
     * 
     * @param token JWT token
     * @return Token geçerlilik durumu
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = jwtUtil.extractUsername(token);
            boolean isValid = jwtUtil.validateToken(token, username);
            
            response.put("valid", isValid);
            response.put("username", username);
            
            if (isValid) {
                response.put("message", "Token geçerli");
            } else {
                response.put("message", "Token geçersiz veya süresi dolmuş");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "Token doğrulama hatası: " + e.getMessage());
            return ResponseEntity.status(401).body(response);
        }
    }
}
