package com.dts.result.infrastructure.kafka;

import com.dts.result.application.event.LearningResultEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LearningResultDltConsumer {

    @DltHandler
    public void processDltMessage(LearningResultEvent event,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage) {
        log.error("DLT Event Received - Topic: {}, EventId: {}, SourceId: {}, SourceType: {}", 
                topic, event.getEventId(), event.getSourceId(), event.getSourceType());
        log.error("DLT Exception: {}", errorMessage);
        
        // Here we could add logic to send alerts to ops team, save to a dead-letter database table, etc.
    }
}
