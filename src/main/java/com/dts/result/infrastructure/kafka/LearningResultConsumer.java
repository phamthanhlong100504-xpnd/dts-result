package com.dts.result.infrastructure.kafka;

import com.dts.result.application.event.LearningResultEvent;
import com.dts.result.api.exception.BusinessException;
import com.dts.result.api.exception.ValidationException;
import com.dts.result.application.service.LearningResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LearningResultConsumer {

    private final LearningResultService learningResultService;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 5000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            exclude = {ValidationException.class, BusinessException.class}
    )
    @KafkaListener(topics = "${spring.kafka.topic.learning-results}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(LearningResultEvent event) {
        try {
            // Setup MDC for log correlation
            MDC.put("eventId", event.getEventId() != null ? event.getEventId().toString() : "unknown");
            MDC.put("userId", event.getUserId() != null ? event.getUserId().toString() : "unknown");
            MDC.put("sourceType", event.getSourceType());
            MDC.put("sourceId", event.getSourceId() != null ? event.getSourceId().toString() : "unknown");
            MDC.put("targetType", event.getTargetType());
            MDC.put("targetId", event.getTargetId() != null ? event.getTargetId().toString() : "unknown");

            log.info("Received LearningResultEvent from Kafka");
            
            learningResultService.processLearningResult(event);
            
            log.info("Finished processing LearningResultEvent");
        } finally {
            MDC.clear();
        }
    }
}
