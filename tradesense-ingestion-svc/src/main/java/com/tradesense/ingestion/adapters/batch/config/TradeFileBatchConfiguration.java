package com.tradesense.ingestion.adapters.batch.config;

import com.tradesense.common.model.records.ParsedTradeCsvRow;
import com.tradesense.common.model.records.TradeCsvRow;
import com.tradesense.ingestion.adapters.batch.IngestionRecordItemWriter;
import com.tradesense.ingestion.adapters.batch.TradeCsvItemProcessor;
import com.tradesense.ingestion.adapters.batch.TradeCsvItemReader;
import com.tradesense.ingestion.adapters.batch.TradeFileJobListener;
import com.tradesense.ingestion.infrastructure.persistence.IngestionBatchRepository;
import com.tradesense.ingestion.infrastructure.persistence.IngestionRecordRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;


import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.UUID;

@Configuration
public class TradeFileBatchConfiguration {

    @Bean
    public Job tradeCsvIngestionJob(
            JobRepository jobRepository,
            Step tradeCsvIngestionStep,
            TradeFileJobListener tradeFileJobListener) {
        return new JobBuilder("tradeCsvIngestionJob", jobRepository)
                .start(tradeCsvIngestionStep)
                .listener(tradeFileJobListener)
                .build();
    }

    @Bean
    public Step tradeCsvIngestionStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemStreamReader<TradeCsvRow> tradeCsvItemReader,
            TradeCsvItemProcessor tradeCsvItemProcessor,
            IngestionRecordItemWriter ingestionRecordItemWriter) {
        return new StepBuilder("tradeCsvIngestionStep", jobRepository)
                .<TradeCsvRow, ParsedTradeCsvRow>chunk(100)
                .reader(tradeCsvItemReader)
                .processor(tradeCsvItemProcessor)
                .writer(ingestionRecordItemWriter)
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public TradeCsvItemReader tradeCsvItemReader(
            @Value("#{jobParameters['filePath']}") String filePath,
            ObjectMapper objectMapper) {
        return new TradeCsvItemReader(Path.of(filePath), objectMapper);
    }

    @Bean
    @StepScope
    public TradeCsvItemProcessor tradeCsvItemProcessor(
            @Value("#{jobParameters['sourceSystem']}") String sourceSystem) {
        return new TradeCsvItemProcessor(sourceSystem);
    }

    @Bean
    @StepScope
    public IngestionRecordItemWriter ingestionRecordItemWriter(
            @Value("#{jobParameters['batchId']}") String batchId,
            @Value("#{jobParameters['sourceSystem']}") String sourceSystem,
            IngestionBatchRepository batchRepository,
            IngestionRecordRepository recordRepository) {
        return new IngestionRecordItemWriter(
                UUID.fromString(batchId), sourceSystem, batchRepository, recordRepository);
    }

    @Bean(name = "tradeFileJobExecutor")
    public TaskExecutor tradeFileJobExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("trade-file-batch-");
        executor.setConcurrencyLimit(4);
        return executor;
    }

    @Bean
    public JobRegistry jobRegistry() {
        return new MapJobRegistry();
    }

    @Bean(name = "tradeFileJobOperator")
    public JobOperator tradeFileJobOperator(
            JobRepository jobRepository,
            JobRegistry jobRegistry,
            @Qualifier("tradeFileJobExecutor") TaskExecutor taskExecutor) {
        TaskExecutorJobOperator jobOperator = new TaskExecutorJobOperator();
        jobOperator.setJobRepository(jobRepository);
        jobOperator.setJobRegistry(jobRegistry);
        jobOperator.setTaskExecutor(taskExecutor);
        return jobOperator;
    }
}
