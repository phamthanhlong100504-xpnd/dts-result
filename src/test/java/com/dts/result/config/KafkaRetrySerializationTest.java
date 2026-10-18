package com.dts.result.config;

import com.dts.result.application.event.LearningResultEvent;
import org.apache.kafka.common.serialization.Serializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import com.dts.result.infrastructure.kafka.LearningResultConsumer;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class KafkaRetrySerializationTest {

    private DefaultKafkaProducerFactory<String, Object> factory() {
        KafkaProperties properties = new KafkaProperties();
        properties.setBootstrapServers(List.of("127.0.0.1:9092"));
        return new KafkaConfig().learningResultProducerFactory(properties,
                new DefaultListableBeanFactory().getBeanProvider(SslBundles.class));
    }

    @Test
    void decodedExamEventSurvivesRetrySerialization() {
        Instant submitted = Instant.parse("2026-10-14T08:29:45.287057Z");
        LearningResultEvent event = LearningResultEvent.builder()
                .eventId(UUID.randomUUID()).userId(UUID.randomUUID())
                .sourceId(UUID.randomUUID()).sourceType("EXAM_SESSION")
                .targetId(UUID.randomUUID()).targetType("EXAM")
                .result("PASSED").score(BigDecimal.valueOf(30))
                .completedAt(submitted).build();
        try (Serializer<Object> serializer = factory().getValueSerializerSupplier().get();
             JsonDeserializer<LearningResultEvent> deserializer = new JsonDeserializer<>(LearningResultEvent.class, false)) {
            byte[] encoded = serializer.serialize("learning-results-retry-1000", event);
            LearningResultEvent decoded = deserializer.deserialize("learning-results-retry-1000", encoded);
            assertEquals(event.getEventId(), decoded.getEventId());
            assertEquals(event.getSourceId(), decoded.getSourceId());
            assertEquals(event.getTargetId(), decoded.getTargetId());
            assertEquals("PASSED", decoded.getResult());
            assertEquals(submitted, decoded.getCompletedAt());
        }
    }

    @Test
    void undecodableValueIsForwardedAsOriginalBytes() {
        byte[] original = "{malformed-json".getBytes(StandardCharsets.UTF_8);
        try (Serializer<Object> serializer = factory().getValueSerializerSupplier().get()) {
            assertArrayEquals(original, serializer.serialize("learning-results-dlt", original));
        }
    }

    @Test
    void consumerSelectsTheObjectCapableRetryTemplate() throws Exception {
        RetryableTopic retry = LearningResultConsumer.class
                .getMethod("consume", LearningResultEvent.class).getAnnotation(RetryableTopic.class);
        assertEquals("learningResultRetryKafkaTemplate", retry.kafkaTemplate());
        var producerFactory = factory();
        assertSame(producerFactory,
                new KafkaConfig().learningResultRetryKafkaTemplate(producerFactory).getProducerFactory());
    }
    @Test
    void practiceV2RawJsonPreservesIndependentScoreAndCounts() {
        String json="""
            {"eventVersion":2,"eventType":"LEARNING_RESULT_CREATED",
             "sourceType":"PRACTICE_SESSION","sourceId":"00000000-0000-0000-0000-000000000001",
             "activityType":"RANDOM_EXAM","licenseClass":"A1","attemptNo":4,
             "score":92,"maxScore":100,"correctCount":23,"questionCount":25,
             "resultAvailable":true,"reviewAvailable":true,"durationSeconds":1111,
             "completedAt":"2026-10-15T10:00:00+00:00"}
            """;
        try (JsonDeserializer<LearningResultEvent> deserializer=new JsonDeserializer<>(LearningResultEvent.class,false)) {
            var event=deserializer.deserialize("learning-results",json.getBytes(StandardCharsets.UTF_8));
            assertEquals(23,event.getCorrectCount());assertEquals(25,event.getQuestionCount());
            assertEquals(new BigDecimal("92"),event.getScore());assertEquals(new BigDecimal("100"),event.getMaxScore());
            assertEquals(4,event.getAttemptNo());assertEquals("RANDOM_EXAM",event.getActivityType());
            assertTrue(event.getResultAvailable());assertEquals(Instant.parse("2026-10-15T10:00:00Z"),event.getCompletedAt());
        }
    }
}
