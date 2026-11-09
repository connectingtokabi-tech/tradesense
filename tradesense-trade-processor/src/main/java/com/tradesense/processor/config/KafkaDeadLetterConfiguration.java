package com.tradesense.processor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaDeadLetterConfiguration {

    @Bean
    public DeadLetterPublishingRecoverer tradeEventDeadLetterRecoverer(
            KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${tradesense.kafka.dead-letter-topic}") String deadLetterTopic) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> new org.apache.kafka.common.TopicPartition(
                        deadLetterTopic, record.partition()));
    }

    @Bean
    public DefaultErrorHandler tradeEventErrorHandler(DeadLetterPublishingRecoverer recoverer) {
        // Initial delivery plus three retries, one second apart, then publish to the DLT.
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1_000L, 3L));
    }

}
