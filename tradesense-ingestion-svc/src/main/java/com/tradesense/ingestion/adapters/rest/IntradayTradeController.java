package com.tradesense.ingestion.adapters.rest;

import com.tradesense.common.model.dto.IntradayTradeRequest;
import com.tradesense.common.model.dto.IntradayTradeResponse;
import com.tradesense.common.model.dto.IntradayTradeStatusResponse;
import com.tradesense.ingestion.application.IntradayTradeIngestionService;
import com.tradesense.ingestion.application.IntradayTradeStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trades/intraday")
@Tag(name = "Intraday trade ingestion", description = "Endpoints for intraday trade events")
@RequiredArgsConstructor
@Slf4j
public class IntradayTradeController {

    private final IntradayTradeIngestionService intradayTradeIngestionService;
    private final IntradayTradeStatusService intradayTradeStatusService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Ingest an intraday trade",
            description = "Stores one intraday trade with its original MQ event ID and creates an ingestion batch.")
    public ResponseEntity<IntradayTradeResponse> ingest(@RequestBody IntradayTradeRequest request) {
        log.info("Received intraday trade event: sourceSystem={}, sourceEventId={}",
                request.sourceSystem(), request.sourceEventId());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(intradayTradeIngestionService.ingest(request));
    }

    @GetMapping("/{batchId}/status")
    @Operation(summary = "Get intraday ingestion status")
    public IntradayTradeStatusResponse getStatus(@PathVariable UUID batchId) {
        return intradayTradeStatusService.getStatus(batchId);
    }
}
