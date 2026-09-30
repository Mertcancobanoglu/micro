package com.sigorta.bff_service.controller;

import com.sigorta.bff_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Authentication Controller
 * 
 * Bu controller, kullanıcı kimlik doğrulama işlemlerini yönetir:
 * - Login işlemi ve JWT token oluşturma
 * - Token validasyonu ve kullanıcı bilgisi çıkarma
 * - Role-based authentication
 * 
 * Endpoint'ler:
 * - POST /api/auth/login: Kullanıcı girişi ve token oluşturma
 * - POST /api/auth/validate: Token validasyonu
 * 
 * Rol Sistemi:
 * - ADMIN: Sistem yöneticisi - tüm yetkilere sahip
 * - POLICY_MANAGER: Poliçe yöneticisi - poliçe işlemleri
 * - CLAIMS_PROCESSOR: Hasar işlemcisi - hasar işlemleri
 * - USER: Normal kullanıcı - kendi işlemleri
 * 
 * @author Insurance Development Team
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    /**
     * Local token endpoint (Development için)
     * 
     * @param username Opsiyonel username (default: localUser)
     * @return JWT token
     */
    @GetMapping("/local-token")
    public ResponseEntity<?> getLocalToken(@RequestParam(defaultValue = "localUser") String username) {
        String role = determineUserRole(username);
        String token = jwtUtil.generateToken(username, role);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("type", "Bearer");
        response.put("username", username);
        response.put("role", role);
        response.put("message", "Development token - Production kullanmayın!");

        return ResponseEntity.ok(response);
    }

    /**
     * Kullanıcı login işlemi
     * 
     * @param credentials username ve password içeren map
     * @return JWT token ve kullanıcı bilgileri
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (Objects.isNull(username) || Objects.isNull(password)) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }

        String role = determineUserRole(username);
        String token = jwtUtil.generateToken(username, role);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", username);
        response.put("role", role);

        return ResponseEntity.ok(response);
    }

    /**
     * JWT token validasyonu
     * 
     * @param authHeader Authorization header (Bearer token)
     * @return Token geçerlilik durumu ve kullanıcı bilgileri
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (Objects.isNull(authHeader) || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid token format");
        }

        String token = authHeader.substring(7);
        try {
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            if (jwtUtil.validateToken(token, username)) {
                Map<String, String> response = new HashMap<>();
                response.put("username", username);
                response.put("role", role);
                response.put("valid", "true");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(401).body("Token is invalid or expired");
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token validation failed: " + e.getMessage());
        }
    }

    /**
     * Kullanıcı adına göre rol belirleme
     * Demo amaçlı username prefix'ine göre rol ataması
     * 
     * @param username Kullanıcı adı
     * @return Kullanıcı rolü
     */
    private String determineUserRole(String username) {
        if (username.startsWith("admin")) {
            return "ADMIN";
        } else if (username.startsWith("agent")) {
            return "POLICY_MANAGER";
        } else if (username.startsWith("processor")) {
            return "CLAIMS_PROCESSOR";
        } else {
            return "USER";
        }
    }
}