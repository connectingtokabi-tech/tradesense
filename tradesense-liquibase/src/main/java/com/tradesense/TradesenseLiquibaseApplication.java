package com.tradesense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class TradesenseLiquibaseApplication {

    public static void main(String[] args) {
        // Keep PostgreSQL connections independent of a host-specific timezone alias.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(TradesenseLiquibaseApplication.class, args);
    }
}
