package com.tradesense.report.application;

import com.tradesense.common.model.entity.Trade;
import com.tradesense.common.model.entity.TradeInstrumentation;
import com.tradesense.common.model.entity.enums.TradeStatus;
import com.tradesense.report.infrastructure.persistence.TradeReportRepository;
import com.tradesense.report.infrastructure.persistence.TradeSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TradeReportService {

    private final TradeReportRepository repository;

    public TradeReportService(TradeReportRepository repository) {
        this.repository = repository;
    }

    public Page<Trade> search(TradeStatus status, String bookingSystem,
                              OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        Specification<Trade> spec = TradeSpecifications.filterTrades(status, bookingSystem, from, to);
        return repository.findAll(spec, pageable);
    }

    public Optional<Trade> findById(UUID id) {
        return repository.findReportById(id);
    }

    public Optional<TradeInstrumentation> findInstrumentationByTradeId(UUID tradeId) {
        return repository.findInstrumentationByTradeId(tradeId);
    }
}
