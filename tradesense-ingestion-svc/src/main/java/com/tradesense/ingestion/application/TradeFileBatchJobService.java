package com.tradesense.ingestion.application;

import java.util.UUID;

public interface TradeFileBatchJobService {
    void launch(UUID batchId, String sourceSystem);
}
