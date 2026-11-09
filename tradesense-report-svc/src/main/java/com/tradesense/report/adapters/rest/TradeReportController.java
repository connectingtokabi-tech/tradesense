package com.tradesense.report.adapters.rest;

import com.tradesense.common.model.entity.Trade;
import com.tradesense.common.model.entity.TradeInstrumentation;
import com.tradesense.common.model.entity.enums.TradeStatus;
import com.tradesense.report.application.TradeReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports/trades")
public class TradeReportController {

    private final TradeReportService service;

    public TradeReportController(TradeReportService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Trade> search(
            @RequestParam(required = false) TradeStatus status,
            @RequestParam(required = false) String bookingSystem,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (from != null && to != null && !from.isBefore(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be before to");
        }
        return service.search(status, bookingSystem, from, to, pageable);
    }

    @GetMapping("/{id}")
    public Trade getById(@PathVariable UUID id) {
        return service.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade not found"));
    }

    @GetMapping("/{id}/instrumentation")
    public TradeInstrumentation getInstrumentation(@PathVariable UUID id) {
        return service.findInstrumentationByTradeId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade instrumentation not found"));
    }
}
