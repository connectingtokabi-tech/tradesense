package com.tradesense.ingestion.application;

import com.tradesense.common.model.entity.enums.SourceType;
import com.tradesense.common.model.records.UploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class TradeFileUploadFacade {

    private final TradeFileIngestionService tradeFileIngestionService;
    private final TradeFileBatchJobService tradeFileBatchJobService;

    public UploadResult uploadAndLaunch(MultipartFile file, SourceType sourceType, String sourceSystem) {
        UploadResult result = tradeFileIngestionService.upload(file, sourceType);
        tradeFileBatchJobService.launch(result.batch().getBatchId(), sourceSystem);
        return result;
    }
}
