package com.sigorta.claim_service.init;

import com.github.javafaker.Faker;
import com.sigorta.claim_service.model.Claim;
import com.sigorta.claim_service.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Dummy Data Initializer
 * 
 * Bu sınıf, development ortamında test verilerini otomatik olarak oluşturur.
 * Application startup'ta çalışır ve 100-150 adet rastgele hasar verisi üretir.
 * 
 * Özellikler:
 * - Faker library kullanarak realistic data üretir
 * - Rastgele Türk isimleri ve hastane isimleri
 * - Gerçekçi hasar tipleri ve durumları
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

    private final ClaimRepository claimRepository;
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
        long existingCount = claimRepository.count();
        if (existingCount > 0) {
            log.info("Database already contains {} claims. Skipping dummy data generation.", existingCount);
            return;
        }

        // Generate 100-150 dummy claims
        int count = ThreadLocalRandom.current().nextInt(100, 151);
        log.info("Generating {} dummy claims...", count);

        for (int i = 0; i < count; i++) {
            Claim claim = generateRandomClaim(i);
            claimRepository.save(claim);
        }

        log.info("Successfully generated {} dummy claims", count);
    }

    /**
     * Rastgele bir hasar üretir
     * 
     * @param index İndeks (claim number generation için)
     * @return Rastgele Claim nesnesi
     */
    private Claim generateRandomClaim(int index) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime incidentDate = now.minusDays(ThreadLocalRandom.current().nextInt(0, 90));
        LocalDateTime reportDate = incidentDate.plusDays(ThreadLocalRandom.current().nextInt(0, 7));

        Claim.ClaimType[] claimTypes = Claim.ClaimType.values();
        Claim.ClaimStatus[] claimStatuses = Claim.ClaimStatus.values();

        String[] turkishNames = {
            "Ahmet Yılmaz", "Mehmet Demir", "Ayşe Kaya", "Fatma Çelik", 
            "Ali Özkan", "Zeynep Aksoy", "Mustafa Şahin", "Elif Yıldız",
            "Hasan Aydın", "Hülya Koç", "İbrahim Uçar", "Şenay Bülent",
            "Merve Polat", "Emre Gürsoy", "Selin Kara", "Burak Eroğlu"
        };

        String[] turkishHospitals = {
            "İstanbul Üniversitesi Hastanesi", "Acıbadem Hastanesi", "Florence Nightingale Hastanesi",
            "Medicana Hastanesi", "Memorial Hastanesi", "American Hospital",
            "Ankara Şehir Hastanesi", "Ege Üniversitesi Hastanesi", "İzmir Dr. Suat Seren Hastanesi",
            "Bursa Uludağ Üniversitesi Hastanesi", "Antalya Akdeniz Üniversitesi Hastanesi"
        };

        String[] diagnoses = {
            "Akut bronşit", "Grip", "Baş ağrısı", "Mide rahatsızlığı", "Yüksek tansiyon",
            "Diabet kontrolü", "Kalp ritim bozukluğu", "Kemik kırığı", "Cerrahi müdahale",
            "MR çekimi", "CT taraması", "Fizik tedavi seansı", "Diş tedavisi"
        };

        String randomName = turkishNames[ThreadLocalRandom.current().nextInt(turkishNames.length)];
        String randomHospital = turkishHospitals[ThreadLocalRandom.current().nextInt(turkishHospitals.length)];
        String randomDiagnosis = diagnoses[ThreadLocalRandom.current().nextInt(diagnoses.length)];
        String randomTcNo = generateRandomTcNo();

        BigDecimal claimedAmount = BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(1000, 50000));
        Claim.ClaimStatus status = claimStatuses[ThreadLocalRandom.current().nextInt(claimStatuses.length)];
        
        BigDecimal approvedAmount = null;
        BigDecimal rejectedAmount = null;
        
        if (status == Claim.ClaimStatus.APPROVED || status == Claim.ClaimStatus.PAYMENT_COMPLETED || status == Claim.ClaimStatus.CLOSED) {
            approvedAmount = claimedAmount.multiply(BigDecimal.valueOf(0.8 + (ThreadLocalRandom.current().nextDouble() * 0.2)));
        } else if (status == Claim.ClaimStatus.REJECTED || status == Claim.ClaimStatus.PARTIALLY_APPROVED) {
            rejectedAmount = claimedAmount.multiply(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble() * 0.5));
            if (status == Claim.ClaimStatus.PARTIALLY_APPROVED) {
                approvedAmount = claimedAmount.subtract(rejectedAmount);
            }
        }

        return Claim.builder()
                .claimNumber("HASAR-" + String.format("%04d", index + 1) + "-" + LocalDateTime.now().getYear())
                .policyId(UUID.randomUUID()) // Fake policy ID - gerçek entegrasyonda değiştirilmeli
                .insuredPersonName(randomName)
                .insuredPersonTcNo(randomTcNo)
                .claimType(claimTypes[ThreadLocalRandom.current().nextInt(claimTypes.length)])
                .status(status)
                .incidentDate(incidentDate)
                .reportDate(reportDate)
                .hospitalName(randomHospital)
                .diagnosis(randomDiagnosis)
                .claimedAmount(claimedAmount)
                .approvedAmount(approvedAmount)
                .rejectedAmount(rejectedAmount)
                .description(faker.lorem().sentence(10, 20))
                .reportedBy(randomName.split(" ")[0] + " " + faker.name().lastName())
                .assignedTo("OPERATOR-" + String.format("%03d", ThreadLocalRandom.current().nextInt(1, 21)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
