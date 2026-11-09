package com.tradesense.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties;
import org.springframework.cloud.client.discovery.simple.reactive.SimpleReactiveDiscoveryProperties;

/** Shares configured static instances with the reactive discovery client used by Gateway. */
@Configuration(proxyBeanMethods = false)
public class SimpleReactiveDiscoveryConfiguration {

    @Bean
    SimpleReactiveDiscoveryProperties simpleReactiveDiscoveryProperties(
            SimpleDiscoveryProperties simpleDiscoveryProperties) {
        SimpleReactiveDiscoveryProperties reactiveProperties = new SimpleReactiveDiscoveryProperties();
        reactiveProperties.setInstances(simpleDiscoveryProperties.getInstances());
        reactiveProperties.setOrder(simpleDiscoveryProperties.getOrder());
        return reactiveProperties;
    }
}
