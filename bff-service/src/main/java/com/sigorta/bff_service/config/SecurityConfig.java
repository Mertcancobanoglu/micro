package com.sigorta.bff_service.config;

import com.sigorta.bff_service.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Konfigürasyon Sınıfı
 * 
 * Bu sınıf, BFF servisinin güvenlik ayarlarını yapılandırır:
 * - JWT tabanlı stateless authentication
 * - Role-based access control (RBAC)
 * - CSRF korumasının devre dışı bırakılması (JWT için gerekli)
 * - Endpoint bazlı yetkilendirme
 * 
 * Güvenlik Kuralları:
 * - /api/auth/**: Herkese açık (login, token validation)
 * - /swagger-ui/**, /v3/api-docs/**: Dokümantasyon için açık
 * - /actuator/**: Monitoring endpoint'leri için açık
 * - /api/bff/**: USER ve ADMIN rollerine açık
 * - /api/admin/**: Sadece ADMIN rolüne açık
 * - Diğer tüm endpoint'ler: Authentication gerektirir
 * 
 * @author Insurance Development Team
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()
                        .requestMatchers("/api/bff/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}