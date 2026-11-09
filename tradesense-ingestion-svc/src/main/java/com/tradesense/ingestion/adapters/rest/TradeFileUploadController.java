package com.tradesense.ingestion.adapters.rest;

import com.tradesense.common.model.dto.FileStatusResponse;
import com.tradesense.common.model.dto.TradeFileUploadResponse;
import com.tradesense.common.model.entity.IngestionBatch;
import com.tradesense.common.model.entity.enums.SourceType;
import com.tradesense.common.model.records.UploadResult;
import com.tradesense.ingestion.application.FileStatusService;
import com.tradesense.ingestion.application.TradeFileUploadFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trades")
@Tag(name = "Trade ingestion", description = "Endpoints for submitting trade files")
@RequiredArgsConstructor
@Slf4j
public class TradeFileUploadController {

    private final FileStatusService fileStatusService;
    private final TradeFileUploadFacade tradeFileUploadFacade;

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload a trade file",
            description = "Uploads a UTF-8 CSV with a header row for EOD ingestion. Intraday records are received from MQ.")
    public ResponseEntity<TradeFileUploadResponse> uploadTradeFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam SourceType sourceType,
            @RequestParam(required = false) String sourceSystem) {
        log.info("Received trade upload request: fileName={}, sourceType={}, sourceSystem={}",
                file.getOriginalFilename(), sourceType, sourceSystem);

        if (file.isEmpty()) {
            log.warn("Upload rejected: empty file for sourceType={}, sourceSystem={}", sourceType, sourceSystem);
            return ResponseEntity.badRequest().body(
                    TradeFileUploadResponse.builder()
                            .message("Uploaded file is empty")
                            .sizeBytes(0)
                            .build());
        }

        UploadResult result = tradeFileUploadFacade.uploadAndLaunch(file, sourceType, sourceSystem);
        var batch = result.batch();
        log.info("Trade upload accepted: batchId={}, fileName={}, sizeBytes={}",
                batch.getBatchId(), batch.getFileName(), result.sizeBytes());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                buildUploadResponse(batch, result.sizeBytes(), result.checksum(), "Trade file stored; ingestion job started"));
    }

    @GetMapping("/files/{batchId}/status")
    @Operation(summary = "Get uploaded file status", description = "Returns processing status and record counts for an uploaded file.")
    public FileStatusResponse getFileStatus(@PathVariable UUID batchId) {
        log.info("Retrieving status for batchId={}", batchId);
        return fileStatusService.getStatus(batchId);
    }

    private TradeFileUploadResponse buildUploadResponse(
            IngestionBatch batch,
            long sizeBytes,
            String checksum,
            String message) {
        return TradeFileUploadResponse.builder()
                .batchId(batch.getBatchId())
                .fileName(batch.getFileName())
                .sizeBytes(sizeBytes)
                .checksum(checksum)
                .status("PROCESSING")
                .message(message)
                .build();
    }
}
