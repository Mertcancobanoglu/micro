package com.sigorta.bff_service.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * BFF Service Kafka Konfigürasyon Sınıfı
 * 
 * Bu sınıf, BFF service için Apache Kafka konfigürasyonlarını sağlar:
 * - Backend servislerden event'leri dinleme
 * - Chatbot event'lerini Kafka üzerinden yayınlama
 * - Employee dashboard event'lerini yönetme
 * - Real-time notification sistemi
 * 
 * Producer Konfigürasyonu:
 * - Bootstrap servers: localhost:9092
 * - Key serializer: String (session ID, conversation ID)
 * - Value serializer: JSON (event objeleri)
 * 
 * Consumer Konfigürasyonu:
 * - Bootstrap servers: localhost:9092
 * - Group ID: bff-service-group
 * - Key deserializer: String
 * - Value deserializer: JSON
 * - Auto offset reset: earliest
 * 
 * Kafka Topic'leri:
 * - claim.updated: Hasar güncellendi event'i
 * - policy.updated: Poliçe güncellendi event'i
 * - chatbot.message: Chatbot mesajları
 * - chatbot.action: Chatbot aksiyonları
 * - dashboard.insight: Dashboard analitikleri
 * - notification.alert: Bildirimler
 * 
 * @author Insurance Development Team
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    /**
     * Producer Factory Bean
     * 
     * BFF service için Kafka message producer'ı:
     * - Chatbot event'lerini yayınlar
     * - Dashboard notification'ları gönderir
     * - Analytics event'leri gönderir
     * 
     * @return Producer factory konfigürasyonu
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 1000);
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * Kafka Template Bean
     * 
     * Message gönderme işlemleri için kullanılır:
     * - ChatbotService'de event gönderme
     * - EmployeeDashboardService'de notification gönderme
     * - Type-safe message publishing
     * 
     * @return Kafka template örneği
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Consumer Factory Bean
     * 
     * BFF service için Kafka message consumer'ı:
     * - Claim service'den event'leri dinler
     - Policy service'den event'leri dinler
     - Real-time updates için kullanılır
     * 
     * Consumer Group:
     * - bff-service-group
     * - Offset management
     * - Load balancing
     * 
     * @return Consumer factory konfigürasyonu
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "bff-service-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new JsonDeserializer<>(Object.class));
    }

    /**
     * Kafka Listener Container Factory Bean
     * 
     * @KafkaListener annotation'ları için container factory:
     * - Message listener'ları yönetir
     * - Concurrent processing
     * - Error handling
     * - Acknowledgment management
     * 
     * @return Kafka listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        return factory;
    }
}