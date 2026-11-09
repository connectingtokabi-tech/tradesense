package com.tradesense.processor.application;

import com.tradesense.common.model.entity.TradeEventInbox;
import com.tradesense.common.model.entity.TradeEventInboxId;
import com.tradesense.common.model.entity.enums.TradeEventInboxStatus;
import com.tradesense.processor.infrastructure.persistence.TradeEventInboxRepository;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TradeProcessingFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(TradeProcessingFailureHandler.class);

    private final TradeEventInboxRepository inboxRepository;
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ObservationRegistry observationRegistry;

    @Value("${tradesense.kafka.dead-letter-topic}")
    private String deadLetterTopic;

    @Transactional
    public void markFailedAndPublishToDlq(TradeEventInboxId id, RuntimeException cause) {
        Observation observation = Observation.createNotStarted("tradesense.trade.processing.recovery", observationRegistry)
                .lowCardinalityKeyValue("consumer", id.getConsumerName())
                .start();
        try (Observation.Scope ignored = observation.openScope()) {
            TradeEventInbox inbox = inboxRepository.findById(id).orElse(null);
            if (inbox == null || inbox.getProcessingStatus() == TradeEventInboxStatus.COMPLETED) {
                return;
            }

            inbox.setRetryCount(inbox.getRetryCount() + 1);
            inbox.setProcessingStatus(TradeEventInboxStatus.FAILED);
            publishToDeadLetterQueue(inbox, id, cause);
            inboxRepository.save(inbox);
            log.error("Trade event processing exhausted retries for event {} and consumer {}",
                    id.getEventId(), id.getConsumerName(), cause);
        } catch (RuntimeException exception) {
            observation.error(exception);
            throw exception;
        } finally {
            observation.stop();
        }
    }

    private void publishToDeadLetterQueue(TradeEventInbox inbox, TradeEventInboxId id, RuntimeException cause) {
        try {
            kafkaTemplate.send(deadLetterTopic, id.getEventId().toString(), inbox.getPayload().toString()).join();
            log.warn("Published failed trade event {} to {}", id.getEventId(), deadLetterTopic);
        } catch (RuntimeException publishFailure) {
            publishFailure.addSuppressed(cause);
            log.error("Could not publish failed trade event {} to {}", id.getEventId(), deadLetterTopic,
                    publishFailure);
        }
    }
}
