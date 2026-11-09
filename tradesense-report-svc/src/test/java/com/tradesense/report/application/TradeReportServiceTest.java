package com.tradesense.report.application;

import com.tradesense.common.model.entity.Trade;
import com.tradesense.common.model.entity.TradeInstrumentation;
import com.tradesense.common.model.entity.enums.SourceType;
import com.tradesense.common.model.entity.enums.TradeStatus;
import com.tradesense.report.infrastructure.persistence.TradeReportRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TradeReportServiceTest {

    @Test
    void search_shouldReturnFilteredPageFromRepository() {
        TradeReportRepository repository = mock(TradeReportRepository.class);
        TradeReportService service = new TradeReportService(repository);

        Trade trade = Trade.pendingValidation(UUID.randomUUID(), "BATS", 123L, SourceType.EOD);
        trade.setTradeStatus(TradeStatus.VALID);
        Page<Trade> expected = new PageImpl<>(List.of(trade), PageRequest.of(0, 20), 1);

        when(repository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(expected);

        Page<Trade> result = service.search(TradeStatus.VALID, "BATS",
                OffsetDateTime.now().minusDays(1), OffsetDateTime.now(), PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals(TradeStatus.VALID, result.getContent().get(0).getTradeStatus());
        verify(repository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void findById_shouldReturnTradeWhenPresent() {
        TradeReportRepository repository = mock(TradeReportRepository.class);
        TradeReportService service = new TradeReportService(repository);
        UUID id = UUID.randomUUID();
        Trade trade = Trade.pendingValidation(id, "BATS", 99L, SourceType.EOD);
        trade.setId(id);

        when(repository.findReportById(id)).thenReturn(Optional.of(trade));

        Optional<Trade> result = service.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void findInstrumentationByTradeId_shouldReturnInstrumentation() {
        TradeReportRepository repository = mock(TradeReportRepository.class);
        TradeReportService service = new TradeReportService(repository);
        UUID tradeId = UUID.randomUUID();
        Trade trade = Trade.pendingValidation(UUID.randomUUID(), "BATS", 55L, SourceType.EOD);
        TradeInstrumentation instrumentation = TradeInstrumentation.forTrade(
                trade, "IBM", 10L, new BigDecimal("100.50"), OffsetDateTime.now());

        when(repository.findInstrumentationByTradeId(tradeId)).thenReturn(Optional.of(instrumentation));

        Optional<TradeInstrumentation> result = service.findInstrumentationByTradeId(tradeId);

        assertTrue(result.isPresent());
        assertSame(instrumentation, result.get());
    }
}
