package com.tradesense.processor.application;

import com.tradesense.common.model.entity.TradeEventInboxId;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeProcessingService {

    private static final Logger log = LoggerFactory.getLogger(TradeProcessingService.class);

    private final TradeProcessingAttemptService attemptService;
    private final TradeProcessingFailureHandler failureHandler;
    private final ObservationRegistry observationRegistry;

    @Async("tradeProcessingExecutor")
    @Retryable(
            retryFor = RuntimeException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1_000L, multiplier = 2.0))
    public void process(TradeEventInboxId id) {
        Observation observation = Observation.createNotStarted("tradesense.trade.processing", observationRegistry)
                .lowCardinalityKeyValue("consumer", id.getConsumerName())
                .start();
        try (Observation.Scope ignored = observation.openScope()) {
            log.info("Starting trade event processing: eventId={}, consumer={}",
                    id.getEventId(), id.getConsumerName());
            attemptService.process(id);
            log.info("Completed trade event processing: eventId={}, consumer={}",
                    id.getEventId(), id.getConsumerName());
        } catch (RuntimeException exception) {
            observation.error(exception);
            log.warn("Trade event processing attempt failed for event {} and consumer {}; retrying if attempts remain",
                    id.getEventId(), id.getConsumerName(), exception);
            throw exception;
        } finally {
            observation.stop();
        }
    }

    @Recover
    public void recover(RuntimeException exception, TradeEventInboxId id) {
        failureHandler.markFailedAndPublishToDlq(id, exception);
    }
}
