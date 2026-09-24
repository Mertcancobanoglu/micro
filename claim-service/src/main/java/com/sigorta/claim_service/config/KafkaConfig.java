package com.sigorta.claim_service.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Konfigürasyon Sınıfı
 * 
 * Bu sınıf, Apache Kafka için gerekli tüm konfigürasyonları sağlar:
 * - Producer (message sender) ayarları
 * - Consumer (message receiver) ayarları
 * - Serialization/deserialization konfigürasyonu
 * - Saga pattern için event-driven communication
 * 
 * Producer Konfigürasyonu:
 * - Bootstrap servers: localhost:9093
 * - Key serializer: String (hasar numarası için)
 * - Value serializer: JSON (event objeleri için)
 * 
 * Consumer Konfigürasyonu:
 * - Bootstrap servers: localhost:9093
 * - Group ID: claim-service-group (consumer grouping)
 * - Key deserializer: String
 * - Value deserializer: JSON
 * - Auto offset reset: earliest (baştan başla)
 * - Trusted packages: * (tüm paketlere güven)
 * 
 * Kafka Topic'leri:
 * - claim.submitted: Hasar gönderildi event'i (producer)
 * - policy.verified: Poliçe doğrulama sonucu (consumer)
 * - claim.approved: Hasar onaylandı event'i (producer)
 * - claim.rejected: Hasar reddedildi event'i (producer)
 * 
 * Saga Pattern Kullanımı:
 * - Event-driven communication
 * - Asynchronous processing
 * - Loose coupling between services
 * - Event sourcing pattern
 * 
 * @author Insurance Development Team
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Producer Factory Bean
     * 
     * Kafka message producer'ı oluşturur:
     * - Event'leri policy service'e gönderir
     * - Saga orchestration için kullanılır
     * - JSON serialization kullanır
     * 
     * @return Producer factory konfigürasyonu
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 1000);
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * Kafka Template Bean
     * 
     * Message gönderme işlemleri için kullanılır:
     * - claimSagaOrchestrator'da event gönderme
     * - Saga sonuçlarını yayınlar
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
     * Kafka message consumer'ı oluşturur:
     * - Policy service'den event'leri dinler
     * - Saga response'larını işler
     * - JSON deserialization kullanır
     * 
     * Consumer Group:
     * - claim-service-group
     * - Offset management
     * - Load balancing
     * 
     * @return Consumer factory konfigürasyonu
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "claim-service-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
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
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}