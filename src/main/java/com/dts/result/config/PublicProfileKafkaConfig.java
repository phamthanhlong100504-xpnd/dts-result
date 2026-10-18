package com.dts.result.config;

import java.util.HashMap;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class PublicProfileKafkaConfig {
    /** Named separately: preserve the existing learning-results decoder/retry/DLT factory. */
    @Bean("publicProfileKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String,String> publicProfiles(
            KafkaProperties properties,ObjectProvider<SslBundles> bundles,
            @Value("${spring.kafka.listener.auto-startup:true}") boolean autoStartup) {
        var config=new HashMap<String,Object>(properties.buildConsumerProperties(bundles.getIfAvailable()));
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        var factory=new ConcurrentKafkaListenerContainerFactory<String,String>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(config,new StringDeserializer(),new StringDeserializer()));
        factory.setAutoStartup(autoStartup);
        // Invalid envelopes are discarded by the projection without logging personal payloads.
        // A database outage retries this independent profile partition instead of losing names.
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L,FixedBackOff.UNLIMITED_ATTEMPTS)));
        return factory;
    }
}
