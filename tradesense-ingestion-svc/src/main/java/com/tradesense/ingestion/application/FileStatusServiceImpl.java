package com.tradesense.ingestion.application;

import com.tradesense.common.model.dto.FileStatusResponse;
import com.tradesense.common.model.entity.IngestionBatch;
import com.tradesense.ingestion.infrastructure.persistence.IngestionBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStatusServiceImpl implements FileStatusService {

    private final IngestionBatchRepository batchRepository;

    @Transactional(readOnly = true)
    @Override
    public FileStatusResponse getStatus(UUID batchId) {
        log.info("Fetching file status for batchId={}", batchId);

        try {
            IngestionBatch batch = batchRepository.findById(batchId)
                    .orElseThrow(() -> {
                        log.warn("No uploaded file found for batchId={}", batchId);
                        return new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "No uploaded file found for batch " + batchId);
                    });

            return FileStatusResponse.builder()
                    .batchId(batch.getBatchId())
                    .fileName(batch.getFileName())
                    .status(batch.getStatus())
                    .totalRecords(batch.getTotalRecords())
                    .processedRecords(batch.getProcessedRecords())
                    .validRecords(batch.getValidRecords())
                    .invalidRecords(batch.getInvalidRecords())
                    .uploadedAt(batch.getCreatedAt())
                    .completedAt(batch.getCompletedAt())
                    .build();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to fetch file status for batchId={}", batchId, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to fetch file status for batch " + batchId,
                    ex);
        }
    }
}
