package com.sigorta.policy_service.init;

import com.github.javafaker.Faker;
import com.sigorta.policy_service.model.Policy;
import com.sigorta.policy_service.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Dummy Data Initializer
 * 
 * Bu sınıf, development ortamında test verilerini otomatik olarak oluşturur.
 * Application startup'ta çalışır ve 100-150 adet rastgele poliçe verisi üretir.
 * 
 * Özellikler:
 * - Faker library kullanarak realistic data üretir
 * - Rastgele Türk isimleri ve adresleri
 * - Gerçekçi poliçe tipleri ve durumları
 * - Geçerli tarih aralıkları
 * - Finansal alanlar için realistic değerler
 * 
 * Enable/Disable:
 * - application.yaml'da data.init.enabled=true ile aktif edilebilir
 * - Sadece dev ve local profillerinde çalışır
 * 
 * @author Insurance Development Team
 */
@Component
@Profile({"dev", "local"})
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PolicyRepository policyRepository;
    private final Faker faker = new Faker();

    @Override
    public void run(String... args) {
        // Check if data initialization is enabled
        boolean initDataEnabled = Boolean.parseBoolean(
            System.getProperty("data.init.enabled", "true")
        );

        if (!initDataEnabled) {
            log.info("Data initialization is disabled. Skipping dummy data generation.");
            return;
        }

        // Check if data already exists
        long existingCount = policyRepository.count();
        if (existingCount > 0) {
            log.info("Database already contains {} policies. Skipping dummy data generation.", existingCount);
            return;
        }

        // Generate 100-150 dummy policies
        int count = ThreadLocalRandom.current().nextInt(100, 151);
        log.info("Generating {} dummy policies...", count);

        for (int i = 0; i < count; i++) {
            Policy policy = generateRandomPolicy(i);
            policyRepository.save(policy);
        }

        log.info("Successfully generated {} dummy policies", count);
    }

    /**
     * Rastgele bir poliçe üretir
     * 
     * @param index İndeks (policy number generation için)
     * @return Rastgele Policy nesnesi
     */
    private Policy generateRandomPolicy(int index) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusDays(ThreadLocalRandom.current().nextInt(0, 365));
        LocalDate endDate = startDate.plusYears(1);

        Policy.PolicyType[] policyTypes = Policy.PolicyType.values();
        Policy.PolicyStatus[] policyStatuses = Policy.PolicyStatus.values();

        String[] turkishNames = {
            "Ahmet Yılmaz", "Mehmet Demir", "Ayşe Kaya", "Fatma Çelik", 
            "Ali Özkan", "Zeynep Aksoy", "Mustafa Şahin", "Elif Yıldız",
            "Hasan Aydın", "Hülya Koç", "İbrahim Uçar", "Şenay Bülent",
            "Merve Polat", "Emre Gürsoy", "Selin Kara", "Burak Eroğlu"
        };

        String[] turkishCities = {
            "İstanbul", "Ankara", "İzmir", "Bursa", "Antalya", 
            "Adana", "Konya", "Mersin", "Gaziantep", "Eskişehir"
        };

        String randomName = turkishNames[ThreadLocalRandom.current().nextInt(turkishNames.length)];
        String randomCity = turkishCities[ThreadLocalRandom.current().nextInt(turkishCities.length)];
        String randomTcNo = generateRandomTcNo();

        return Policy.builder()
                .policyNumber("POL-" + String.format("%04d", index + 1) + "-" + LocalDate.now().getYear())
                .insuredPersonName(randomName)
                .insuredPersonTcNo(randomTcNo)
                .birthDate(LocalDate.now().minusYears(ThreadLocalRandom.current().nextInt(18, 70)))
                .gender(ThreadLocalRandom.current().nextBoolean() ? "KADIN" : "ERKEK")
                .address(faker.address().fullAddress() + ", " + randomCity)
                .phone(faker.phoneNumber().cellPhone())
                .email(faker.internet().emailAddress())
                .policyType(policyTypes[ThreadLocalRandom.current().nextInt(policyTypes.length)])
                .status(policyStatuses[ThreadLocalRandom.current().nextInt(policyStatuses.length)])
                .startDate(startDate)
                .endDate(endDate)
                .annualPremium(BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(5000, 50000)))
                .coverageLimit(BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000)))
                .usedCoverage(BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(0, 50000)))
                .deductible(BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(500, 5000)))
                .coPaymentPercentage(ThreadLocalRandom.current().nextInt(10, 31))
                .coverageDetails("Tam kapsamlı sağlık sigortası, yatarak tedavi, ayakta tedavi, ilaç temini")
                .exclusions("Kozmetik işlemler, estetik ameliyatlar, diş implantasyon")
                .beneficiaryName(randomName.split(" ")[0] + " " + faker.name().lastName())
                .beneficiaryTcNo(generateRandomTcNo())
                .agentId("AGENT-" + String.format("%03d", ThreadLocalRandom.current().nextInt(1, 51)))
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * Rastgele geçerli bir TC kimlik numarası üretir
     * 
     * @return 11 haneli TC kimlik numarası
     */
    private String generateRandomTcNo() {
        StringBuilder tcNo = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            tcNo.append(ThreadLocalRandom.current().nextInt(0, 10));
        }
        
        // 10. ve 11. haneleri hesapla (basit algoritma)
        int[] digits = new int[11];
        for (int i = 0; i < 9; i++) {
            digits[i] = Character.getNumericValue(tcNo.charAt(i));
        }
        
        int tenthDigit = 0;
        for (int i = 0; i < 9; i += 2) {
            tenthDigit += digits[i];
        }
        tenthDigit *= 7;
        tenthDigit -= (digits[1] + digits[3] + digits[5] + digits[7]);
        tenthDigit %= 10;
        
        int eleventhDigit = 0;
        for (int i = 0; i < 10; i++) {
            if (i < 9) {
                eleventhDigit += digits[i];
            } else {
                eleventhDigit += tenthDigit;
            }
        }
        eleventhDigit %= 10;
        
        tcNo.append(tenthDigit);
        tcNo.append(eleventhDigit);
        
        return tcNo.toString();
    }
}
