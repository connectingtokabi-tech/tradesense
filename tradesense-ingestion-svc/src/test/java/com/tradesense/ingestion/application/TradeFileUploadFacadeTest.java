package com.tradesense.ingestion.application;

import com.tradesense.common.model.entity.IngestionBatch;
import com.tradesense.common.model.entity.enums.SourceType;
import com.tradesense.common.model.records.UploadResult;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TradeFileUploadFacadeTest {

    @Test
    void uploadAndLaunch_shouldDelegateToUploadAndBatchLaunch() {
        TradeFileIngestionService ingestionService = mock(TradeFileIngestionService.class);
        TradeFileBatchJobService batchJobService = mock(TradeFileBatchJobService.class);
        TradeFileUploadFacade facade = new TradeFileUploadFacade(ingestionService, batchJobService);

        MockMultipartFile file = new MockMultipartFile("file", "trades.csv", "text/csv", "id,name\n1,test".getBytes());
        IngestionBatch batch = new IngestionBatch(
                "batch-1",
                SourceType.EOD,
                "trades.csv",
                "/tmp/trades.csv",
                "checksum-123");
        batch.setBatchId(UUID.randomUUID());
        UploadResult expected = new UploadResult(batch, 10L, "abc");

        when(ingestionService.upload(file, SourceType.EOD)).thenReturn(expected);

        UploadResult result = facade.uploadAndLaunch(file, SourceType.EOD, "manual");

        assertSame(expected, result);
        verify(ingestionService).upload(file, SourceType.EOD);
        verify(batchJobService).launch(batch.getBatchId(), "manual");
    }
}
