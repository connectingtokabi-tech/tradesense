package com.tradesense.common.model.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TradeInstrumentationTest {

    @Test
    void forTrade_shouldCreateInstrumentationForTrade() {
        Trade trade = new Trade();
        trade.setId(UUID.randomUUID());
        OffsetDateTime tradeTimestamp = OffsetDateTime.now();

        TradeInstrumentation instrumentation = TradeInstrumentation.forTrade(
                trade, "IBM", 250L, new BigDecimal("101.5"), tradeTimestamp);

        assertNotNull(instrumentation);
        assertSame(trade, instrumentation.getTrade());
        assertEquals("IBM", instrumentation.getInstrumentId());
        assertEquals(250L, instrumentation.getQuantity());
        assertEquals(new BigDecimal("101.5"), instrumentation.getTradePrice());
        assertEquals(tradeTimestamp, instrumentation.getTradeTimestamp());
    }

    @Test
    void forTrade_shouldRejectNullTrade() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TradeInstrumentation.forTrade(null, "IBM", 250L, new BigDecimal("101.5"), OffsetDateTime.now()));

        assertEquals("Trade is required", exception.getMessage());
    }
}

