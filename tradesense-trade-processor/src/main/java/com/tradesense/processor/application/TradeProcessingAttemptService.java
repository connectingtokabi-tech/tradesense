package com.tradesense.processor.application;

import com.tradesense.common.model.dto.TradeValidationResponse;
import com.tradesense.common.model.entity.Trade;
import com.tradesense.common.model.entity.TradeEventInbox;
import com.tradesense.common.model.entity.TradeEventInboxId;
import com.tradesense.common.model.entity.TradeInstrumentation;
import com.tradesense.common.model.entity.enums.TradeEventInboxStatus;
import com.tradesense.common.model.entity.enums.TradeStatus;
import com.tradesense.processor.infrastructure.persistence.TradeEventInboxRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TradeProcessingAttemptService {

    private static final Logger log = LoggerFactory.getLogger(TradeProcessingAttemptService.class);

    private final TradeEventInboxRepository inboxRepository;
    private final TradeValidationClient tradeValidationClient;
    private final TradePersistenceService tradePersistenceService;
    private final TradeEntityMapper tradeEntityMapper;
    private final TradeCdcEventMapper tradeCdcEventMapper;
    private final TradeSavedEventPublisher tradeSavedEventPublisher;

    public void process(TradeEventInboxId id) {
        TradeEventInbox inbox = inboxRepository.findById(id).orElse(null);
        if (inbox == null) {
            log.warn("Trade event inbox entry was not found: eventId={}, consumer={}",
                    id.getEventId(), id.getConsumerName());
            return;
        }
        if (inbox.getProcessingStatus() == TradeEventInboxStatus.COMPLETED) {
            log.info("Skipping already completed trade event: eventId={}, consumer={}",
                    id.getEventId(), id.getConsumerName());
            return;
        }
        NormalizedTradeEvent normalizedEvent = tradeCdcEventMapper.map(inbox.getPayload());
        UUID ingestionRecordId = normalizedEvent.ingestionRecordId();
        TradePayload tradePayload = normalizedEvent.tradePayload();
        log.info("Mapped trade event: eventId={}, ingestionRecordId={}, bookingSystem={}, tradeId={}, sourceType={}, instrument={}",
                id.getEventId(), ingestionRecordId, tradePayload.bookingSystem(), tradePayload.tradeId(),
                tradePayload.sourceType(), tradePayload.instrumentId());
        Trade trade = tradeEntityMapper.toTrade(ingestionRecordId, tradePayload);
        TradeInstrumentation instrumentation = tradeEntityMapper.toInstrumentation(trade, tradePayload);
        TradeValidationResponse validation = tradeValidationClient.validate(
                ingestionRecordId,
                trade,
                instrumentation);
        log.info("Trade validation completed: eventId={}, ingestionRecordId={}, valid={}, violationCount={}",
                id.getEventId(), ingestionRecordId, validation.valid(), validation.violations().size());

        trade.setTradeStatus(validation.valid() ? TradeStatus.VALID : TradeStatus.INVALID);
        if (!validation.valid()) {
            log.warn("Trade record {} failed {} Drools rule(s): {}",
                    ingestionRecordId, validation.violations().size(), validation.violations());
        }
        tradePersistenceService.persist(id, trade, instrumentation, validation);
        log.info("Persisted trade and instrumentation: eventId={}, ingestionRecordId={}, tradeStatus={}",
                id.getEventId(), ingestionRecordId, trade.getTradeStatus());
        tradeSavedEventPublisher.publish(trade, instrumentation);
        log.info("Published trade-saved stream notification: eventId={}, ingestionRecordId={}",
                id.getEventId(), ingestionRecordId);
    }
}
