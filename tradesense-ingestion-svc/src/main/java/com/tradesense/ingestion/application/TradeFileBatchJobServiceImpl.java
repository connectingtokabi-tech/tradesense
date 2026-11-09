package com.tradesense.ingestion.application;

import com.tradesense.common.model.entity.IngestionBatch;
import com.tradesense.common.model.entity.enums.IngestionBatchStatus;
import com.tradesense.ingestion.infrastructure.persistence.IngestionBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TradeFileBatchJobServiceImpl implements TradeFileBatchJobService {

    private final IngestionBatchRepository batchRepository;
    @Qualifier("tradeFileJobOperator")
    private final JobOperator jobOperator;
    @Qualifier("tradeCsvIngestionJob")
    private final Job tradeCsvIngestionJob;

    @Override
    @Async
    public void launch(UUID batchId, String sourceSystem) {
        IngestionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No uploaded file found for batch " + batchId));
        batch.setStatus(IngestionBatchStatus.PROCESSING);
        batchRepository.saveAndFlush(batch);

        JobParametersBuilder parametersBuilder = new JobParametersBuilder()
                .addString("batchId", batchId.toString())
                .addString("filePath", batch.getFileLocation(), false);
        if (sourceSystem != null && !sourceSystem.isBlank()) {
            parametersBuilder.addString("sourceSystem", sourceSystem, false);
        }
        JobParameters parameters = parametersBuilder.toJobParameters();
        try {
            jobOperator.start(tradeCsvIngestionJob, parameters);
        } catch (Exception exception) {
            batch.setStatus(IngestionBatchStatus.FAILED);
            batch.setCompletedAt(java.time.OffsetDateTime.now());
            batchRepository.save(batch);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Could not start trade file batch job", exception);
        }
    }
}
