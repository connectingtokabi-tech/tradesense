package com.tradesense.common.model.entity;

import com.tradesense.common.model.entity.enums.SourceType;
import com.tradesense.common.model.entity.enums.TradeStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TradeTest {

    @Test
    void pendingValidation_shouldCreateTradeWithDefaultInvalidStatus() {
        UUID ingestionRecordId = UUID.randomUUID();

        Trade trade = Trade.pendingValidation(ingestionRecordId, "BATS", 101L, SourceType.EOD);

        assertNotNull(trade);
        assertEquals(ingestionRecordId, trade.getIngestionRecordId());
        assertEquals("BATS", trade.getBookingSystem());
        assertEquals(101L, trade.getTradeId());
        assertEquals(SourceType.EOD, trade.getSourceType());
        assertEquals(TradeStatus.INVALID, trade.getTradeStatus());
    }

    @Test
    void pendingValidation_shouldRejectNullIngestionRecordId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Trade.pendingValidation(null, "BATS", 101L, SourceType.EOD));

        assertEquals("Ingestion record ID is required", exception.getMessage());
    }
}

