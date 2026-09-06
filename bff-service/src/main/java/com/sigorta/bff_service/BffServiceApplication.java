package com.sigorta.bff_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BFF (Backend for Frontend) Service Ana Uygulama Sınıfı
 * 
 * Bu sınıf, sağlık sigortası sisteminin BFF katmanının giriş noktasıdır.
 * BFF service, frontend uygulamaları için tek bir giriş noktası sağlar ve
 * diğer mikro servislerle (Policy Service, Claim Service) iletişim kurar.
 * 
 * Görevleri:
 * - API Gateway olarak çalışır
 * - İstekleri uygun microservislere yönlendirir
 * - JWT token doğrulaması yapar
 * - Response'ları aggregate eder
 * - Frontend için optimize edilmiş API'lar sunar
 * 
 * Port: 8080
 * 
 * @author Insurance Development Team
 * @version 1.0.0
 */
@SpringBootApplication
public class BffServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BffServiceApplication.class, args);
	}

}
