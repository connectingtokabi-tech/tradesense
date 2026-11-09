package com.tradesense.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.retry.annotation.EnableRetry;

import java.util.TimeZone;

@SpringBootApplication
@EnableKafka
@EnableAsync
@EnableRetry
@EntityScan(basePackages = "com.tradesense.common.model.entity")
@EnableJpaRepositories(basePackages = "com.tradesense.processor")
public class TradesenseTradeProcessorApplication {

    public static void main(String[] args) {
        // Avoid passing host-specific timezone aliases (for example Asia/Calcutta) to PostgreSQL.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(TradesenseTradeProcessorApplication.class, args);
    }
}
