package com.tradesense.ingestion.adapters.batch;

import com.tradesense.common.model.entity.IngestionBatch;
import com.tradesense.common.model.entity.enums.IngestionBatchStatus;
import com.tradesense.ingestion.infrastructure.persistence.IngestionBatchRepository;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class TradeFileJobListener implements JobExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(TradeFileJobListener.class);

    private final IngestionBatchRepository batchRepository;

    public TradeFileJobListener(IngestionBatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String batchIdParameter = jobExecution.getJobParameters().getString("batchId");
        if (batchIdParameter == null) {
            return;
        }

        batchRepository.findById(UUID.fromString(batchIdParameter)).ifPresent(batch -> {
            boolean completed = jobExecution.getStatus() == BatchStatus.COMPLETED;
            if (!completed) {
                if (jobExecution.getAllFailureExceptions().isEmpty()) {
                    logger.error("Trade file batch {} ended with status {} and no reported exception",
                            batchIdParameter, jobExecution.getStatus());
                } else {
                    jobExecution.getAllFailureExceptions().forEach(exception ->
                            logger.error("Trade file batch {} failed", batchIdParameter, exception));
                }
            }
            batch.setStatus(completed ? IngestionBatchStatus.COMPLETED : IngestionBatchStatus.FAILED);
            if (completed) {
                batch.setProcessedRecords(batch.getTotalRecords());
            }
            batch.setCompletedAt(OffsetDateTime.now());
            batchRepository.save(batch);
        });
    }
}
