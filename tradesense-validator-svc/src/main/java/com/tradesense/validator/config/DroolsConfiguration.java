package com.tradesense.validator.config;

import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DroolsConfiguration {

    @Bean
    public KieContainer tradeValidationKieContainer() {
        KieContainer kieContainer = KieServices.Factory.get().getKieClasspathContainer();
        Results results = kieContainer.verify();
        if (results.hasMessages(Message.Level.ERROR)) {
            throw new IllegalStateException("Drools rule compilation failed: " + results.getMessages(Message.Level.ERROR));
        }
        return kieContainer;
    }
}
