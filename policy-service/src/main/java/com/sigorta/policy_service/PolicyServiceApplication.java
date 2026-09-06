package com.sigorta.policy_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Policy Service (Poliçe Yönetim Servisi) Ana Uygulama Sınıfı
 * 
 * Bu sınıf, sağlık sigortası sisteminin poliçe yönetim microservisinin giriş noktasıdır.
 * Policy Service, sigorta poliçelerinin tamamını yönetir:
 * - Poliçe oluşturma ve yönetimi
 * - Elementer sağlık sigortası (temel sağlık sigortası)
 * - Tamamlayıcı sağlık sigortası
 * - Özel sağlık sigortası
 * - Saga pattern ile claim doğrulama
 * - Redis ile performans optimizasyonu
 * 
 * Görevleri:
 * - Poliçe yaşam döngüsü yönetimi
 * - Poliçe validasyonu ve doğrulama
 * - Claim service ile entegrasyon
 * - Prim hesaplama ve tahsilat
 * - Yenileme ve iptal işlemleri
 * 
 * Port: 8082
 * 
 * @author Insurance Development Team
 * @version 1.0.0
 */
@SpringBootApplication
public class PolicyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PolicyServiceApplication.class, args);
	}

}
