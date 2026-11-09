package com.tradesense.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.discovery.simple.InstanceProperties;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties;
import org.springframework.cloud.client.discovery.simple.reactive.SimpleReactiveDiscoveryProperties;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SimpleReactiveDiscoveryConfigurationTest {

    @Test
    void simpleReactiveDiscoveryProperties_shouldCopyConfiguredInstances() {
        Map<String, List<InstanceProperties>> instances = new HashMap<>();
        InstanceProperties instance = new InstanceProperties();
        instance.setUri(URI.create("http://localhost:5052"));
        instances.put("tradesense-trade-processor", List.of(instance));

        SimpleDiscoveryProperties simpleDiscoveryProperties = new SimpleDiscoveryProperties();
        simpleDiscoveryProperties.setInstances(instances);

        SimpleReactiveDiscoveryConfiguration configuration = new SimpleReactiveDiscoveryConfiguration();
        SimpleReactiveDiscoveryProperties reactiveProperties = configuration.simpleReactiveDiscoveryProperties(simpleDiscoveryProperties);

        assertNotNull(reactiveProperties);
        assertEquals(1, reactiveProperties.getInstances("tradesense-trade-processor").size());
        assertEquals("http://localhost:5052", reactiveProperties.getInstances("tradesense-trade-processor").getFirst().getUri().toString());
    }
}
