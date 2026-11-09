package com.tradesense.validator.application;

import com.tradesense.common.model.dto.TradeValidationRequest;
import com.tradesense.common.model.dto.TradeValidationResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeValidationService {

    private static final Logger log = LoggerFactory.getLogger(TradeValidationService.class);

    private final KieContainer kieContainer;
    private final ObservationRegistry observationRegistry;
    private final MeterRegistry meterRegistry;

    public TradeValidationResponse validate(TradeValidationRequest trade) {
        String sourceType = trade.trade() == null || trade.trade().getSourceType() == null
                ? "unknown"
                : trade.trade().getSourceType().name();
        Observation observation = Observation.createNotStarted(
                        "tradesense.trade.validation", observationRegistry)
                .lowCardinalityKeyValue("validation.source_type", sourceType)
                .start();
        TradeValidationResult result = new TradeValidationResult();
        try (Observation.Scope ignored = observation.openScope()) {
            KieSession session = kieContainer.newKieSession("tradeValidationKSession");
            try {
                session.setGlobal("validationResult", result);
                session.insert(trade);
                session.fireAllRules();
                TradeValidationResponse response = new TradeValidationResponse(
                        result.isValid(), result.getViolations());
                String outcome = response.valid() ? "valid" : "invalid";
                observation.lowCardinalityKeyValue("validation.result", outcome);
                recordOutcome(outcome, sourceType);
                log.info("Trade validation completed: ingestionRecordId={}, sourceType={}, outcome={}, violationCount={}",
                        trade.ingestionRecordId(), sourceType, outcome, response.violations().size());
                return response;
            } finally {
                session.dispose();
            }
        } catch (RuntimeException exception) {
            observation.error(exception);
            observation.lowCardinalityKeyValue("validation.result", "error");
            recordOutcome("error", sourceType);
            log.error("Trade validation failed: ingestionRecordId={}, sourceType={}",
                    trade.ingestionRecordId(), sourceType, exception);
            throw exception;
        } finally {
            observation.stop();
        }
    }

    private void recordOutcome(String outcome, String sourceType) {
        meterRegistry.counter(
                "tradesense.trade.validation.outcomes",
                Tags.of("result", outcome, "source_type", sourceType))
                .increment();
    }
}
