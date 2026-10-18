package com.dts.result.config;

import com.dts.result.application.event.LearningResultEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public DefaultKafkaProducerFactory<String, Object> learningResultProducerFactory(
            KafkaProperties properties, ObjectProvider<SslBundles> sslBundles) {
        // Retry/DLT forwards decoded events. ErrorHandlingDeserializer can also
        // forward the original bytes when decoding fails; both forms must work.
        return new DefaultKafkaProducerFactory<>(
                properties.buildProducerProperties(sslBundles.getIfAvailable()),
                StringSerializer::new,
                () -> new DelegatingByTypeSerializer(Map.of(
                        LearningResultEvent.class, new JsonSerializer<LearningResultEvent>(),
                        byte[].class, new ByteArraySerializer())));
    }

    @Bean(name = "learningResultRetryKafkaTemplate")
    public KafkaTemplate<String, Object> learningResultRetryKafkaTemplate(
            @Qualifier("learningResultProducerFactory") ProducerFactory<String, Object> factory) {
        return new KafkaTemplate<>(factory);
    }

    @Bean
    public NewTopic learningResultsTopic() {
        return TopicBuilder.name("learning-results")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic learningResultsDltTopic() {
        return TopicBuilder.name("learning-results-dlt")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
