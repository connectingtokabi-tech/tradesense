package com.tradesense.processor.application;

import com.tradesense.common.model.entity.TradeEventInboxId;
import com.tradesense.processor.infrastructure.persistence.TradeEventInboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class TradeEventInboxService {

    private final TradeEventInboxRepository inboxRepository;
    private final ObservationRegistry observationRegistry;

    @Transactional
    public boolean record(TradeEventInboxId id, JsonNode payload) {
        Observation observation = Observation.createNotStarted("tradesense.trade.inbox", observationRegistry)
                .lowCardinalityKeyValue("consumer", id.getConsumerName())
                .start();
        try (Observation.Scope ignored = observation.openScope()) {
            return inboxRepository.insertIfAbsent(
                    id.getEventId(), id.getConsumerName(), payload.toString()) == 1;
        } catch (RuntimeException exception) {
            observation.error(exception);
            throw exception;
        } finally {
            observation.stop();
        }
    }
}
