package com.dts.result.infrastructure.kafka;

import com.dts.result.application.service.PublicProfileProjectionService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublicProfileConsumer {
    private final PublicProfileProjectionService service;
    @KafkaListener(topics="${result.public-profile.topic:user-events}",
            groupId="${spring.kafka.consumer.group-id}-public-profiles-v1",
            containerFactory="publicProfileKafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String,String> record) {
        service.record(record.value(),record.partition(),record.offset());
    }
}
