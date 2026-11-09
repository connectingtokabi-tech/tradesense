package com.tradesense.validator.application;

import com.tradesense.common.model.dto.TradeValidationRequest;
import com.tradesense.common.model.dto.TradeValidationResponse;
import com.tradesense.common.model.entity.Trade;
import com.tradesense.common.model.entity.TradeInstrumentation;
import com.tradesense.common.model.entity.enums.SourceType;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradeValidationServiceTest {

    private TradeValidationService tradeValidationService;

    @BeforeEach
    void setUp() {
        KieContainer kieContainer = KieServices.Factory.get().getKieClasspathContainer();
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        tradeValidationService = new TradeValidationService(kieContainer, observationRegistry, meterRegistry);
    }

    @Test
    void validate_shouldReturnInvalidWhenBookingSystemMissing() {
        Trade trade = Trade.pendingValidation(UUID.randomUUID(), null, 10L, SourceType.EOD);
        TradeInstrumentation instrumentation = TradeInstrumentation.forTrade(
                trade, "IBM", 100L, new BigDecimal("10.00"), OffsetDateTime.now());

        TradeValidationRequest request = new TradeValidationRequest(UUID.randomUUID(), trade, instrumentation);

        TradeValidationResponse response = tradeValidationService.validate(request);

        assertFalse(response.valid());
        assertTrue(response.violations().stream().anyMatch(v -> v.errorCode().equals("BOOKING_SYSTEM_REQUIRED")));
    }

    @Test
    void validate_shouldReturnValidWhenRequestMatchesRules() {
        Trade trade = Trade.pendingValidation(UUID.randomUUID(), "BATS", 10L, SourceType.INTRADAY);
        TradeInstrumentation instrumentation = TradeInstrumentation.forTrade(
                trade, "IBM", 100L, new BigDecimal("10.00"), OffsetDateTime.now());

        TradeValidationRequest request = new TradeValidationRequest(UUID.randomUUID(), trade, instrumentation);

        TradeValidationResponse response = tradeValidationService.validate(request);

        assertTrue(response.valid());
        assertTrue(response.violations().isEmpty());
    }

    @Test
    void validate_shouldCaptureValidationErrors() {
        Trade trade = Trade.pendingValidation(UUID.randomUUID(), "BATS", -1L, SourceType.EOD);
        TradeInstrumentation instrumentation = TradeInstrumentation.forTrade(
                trade, "", 0L, BigDecimal.ZERO, null);

        TradeValidationRequest request = new TradeValidationRequest(UUID.randomUUID(), trade, instrumentation);

        TradeValidationResponse response = tradeValidationService.validate(request);

        assertFalse(response.valid());
        assertFalse(response.violations().isEmpty());
    }
}
