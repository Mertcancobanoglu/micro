package com.sigorta.claim_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Claim Service (Hasar Yönetim Servisi) Ana Uygulama Sınıfı
 * 
 * Bu sınıf, sağlık sigortası sisteminin hasar yönetim microservisinin giriş noktasıdır.
 * Claim Service, sigorta hasarlarının tamamını yönetir:
 * - Hasar ihbarı ve kaydı
 * - Hasar işleme ve değerlendirme
 * - Saga pattern ile dağıtık işlem yönetimi
 * - Policy service ile entegrasyon
 * - Redis ile performans optimizasyonu
 * 
 * Görevleri:
 * - Hasar dosyalarının oluşturulması ve yönetimi
 * - Hasar durumlarının takibi (DRAFT -> SUBMITTED -> APPROVED/REJECTED)
 * - Otomatik hasar değerlendirme
 * - Payment processing entegrasyonu
 * - Rücu (iade) süreçleri
 * 
 * Port: 8081
 * 
 * @author Insurance Development Team
 * @version 1.0.0
 */
@SpringBootApplication
public class ClaimServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClaimServiceApplication.class, args);
	}

}
