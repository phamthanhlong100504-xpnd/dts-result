package com.dts.result.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

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
