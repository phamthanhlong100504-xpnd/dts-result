package com.dts.result.config;

import com.dts.result.application.event.LearningResultEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import static org.junit.jupiter.api.Assertions.*;

class PublicProfileKafkaIsolationTest {
    @Test void profileFactoryPreservesDefaultResultConsumerAndObjectRetryTemplate() {
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(KafkaAutoConfiguration.class))
            .withUserConfiguration(KafkaConfig.class,PublicProfileKafkaConfig.class)
            .withPropertyValues("spring.kafka.bootstrap-servers=127.0.0.1:1",
                "spring.kafka.admin.auto-create=false","spring.kafka.listener.auto-startup=false",
                "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.ErrorHandlingDeserializer")
            .run(context->{
                assertNull(context.getStartupFailure());
                var original=context.getBean("kafkaListenerContainerFactory",ConcurrentKafkaListenerContainerFactory.class);
                var profiles=context.getBean("publicProfileKafkaListenerContainerFactory",ConcurrentKafkaListenerContainerFactory.class);
                assertNotSame(original,profiles);
                assertEquals("org.springframework.kafka.support.serializer.ErrorHandlingDeserializer",
                    original.getConsumerFactory().getConfigurationProperties().get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG).toString().replace("class ",""));
                assertTrue(context.containsBean("learningResultRetryKafkaTemplate"));
                assertNotSame(original.getConsumerFactory(),profiles.getConsumerFactory());
            });
    }
    @Test void questionEnvelopeSurvivesTheExistingRetryAndDltSerializer() {
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(KafkaAutoConfiguration.class))
            .withUserConfiguration(KafkaConfig.class).withPropertyValues("spring.kafka.admin.auto-create=false")
            .run(context->{
                @SuppressWarnings("unchecked") var producer=(DefaultKafkaProducerFactory<String,Object>)context.getBean("learningResultProducerFactory");
                var id=UUID.randomUUID();
                var event=LearningResultEvent.builder().eventId(id).sourceId(id).userId(UUID.randomUUID())
                    .eventType("QUESTION_CORRECT_RECORDED").eventVersion(1).sourceType("PRACTICE_QUESTION")
                    .targetType("QUESTION").licenseClass("A1").occurredAt(Instant.parse("2026-10-16T07:00:00Z"))
                    .metadata(Map.of("questionId",1,"bankVersion","bank-test","context","CHAPTER_LEARNING",
                        "captureStartedAt","2026-10-16T06:00:00Z")).build();
                try(Serializer<Object> serializer=producer.getValueSerializerSupplier().get();
                    var deserializer=new JsonDeserializer<>(LearningResultEvent.class,false)) {
                    var decoded=deserializer.deserialize("learning-results-retry-1000",serializer.serialize("learning-results-retry-1000",event));
                    assertEquals(id,decoded.getEventId());assertEquals(event.getMetadata(),decoded.getMetadata());
                    assertEquals(event.getOccurredAt(),decoded.getOccurredAt());
                }
            });
    }
}
