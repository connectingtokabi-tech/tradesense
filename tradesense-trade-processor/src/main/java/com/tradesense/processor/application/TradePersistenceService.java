package com.tradesense.processor.application;

import com.tradesense.common.model.dto.TradeValidationResponse;
import com.tradesense.common.model.entity.Trade;
import com.tradesense.common.model.entity.TradeEventInbox;
import com.tradesense.common.model.entity.TradeEventInboxId;
import com.tradesense.common.model.entity.TradeInstrumentation;
import com.tradesense.common.model.entity.TradeValidation;
import com.tradesense.common.model.entity.enums.TradeEventInboxStatus;
import com.tradesense.common.model.entity.enums.TradeValidationStatus;
import com.tradesense.processor.infrastructure.persistence.TradeEventInboxRepository;
import com.tradesense.processor.infrastructure.persistence.TradeInstrumentationRepository;
import com.tradesense.processor.infrastructure.persistence.TradeRepository;
import com.tradesense.processor.infrastructure.persistence.TradeValidationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradePersistenceService {

    private final TradeRepository tradeRepository;
    private final TradeInstrumentationRepository instrumentationRepository;
    private final TradeValidationRepository validationRepository;
    private final TradeEventInboxRepository inboxRepository;

    @Transactional
    public void persist(
            TradeEventInboxId inboxId,
            Trade trade,
            TradeInstrumentation instrumentation,
            TradeValidationResponse validation) {
        TradeEventInbox inbox = inboxRepository.findById(inboxId)
                .orElseThrow(() -> new IllegalStateException("Trade inbox event not found: " + inboxId));
        if (tradeRepository.findByIngestionRecordId(trade.getIngestionRecordId()).isEmpty()) {
            Trade savedTrade = tradeRepository.saveAndFlush(trade);
            instrumentation.setTrade(savedTrade);
            instrumentationRepository.saveAndFlush(instrumentation);
            persistValidationResults(savedTrade, validation);
        }

        inbox.setProcessingStatus(TradeEventInboxStatus.COMPLETED);
        inbox.setProcessedAt(OffsetDateTime.now());
        inboxRepository.save(inbox);
    }

    private void persistValidationResults(Trade trade, TradeValidationResponse response) {
        if (response.valid()) {
            validationRepository.save(TradeValidation.result(
                    trade, TradeValidationStatus.VALID, null, null, "drools-ruleset", "1.0"));
            return;
        }

        List<TradeValidation> failedRules = response.violations().stream()
                .map(violation -> TradeValidation.result(
                        trade,
                        TradeValidationStatus.INVALID,
                        violation.errorCode(),
                        violation.message(),
                        violation.ruleName(),
                        violation.ruleVersion()))
                .toList();
        if (failedRules.isEmpty()) {
            failedRules = List.of(TradeValidation.result(
                    trade, TradeValidationStatus.INVALID, "TRADE_VALIDATION_FAILED",
                    "Trade validation failed without rule details", "drools-ruleset", "1.0"));
        }
        validationRepository.saveAll(failedRules);
    }

}
