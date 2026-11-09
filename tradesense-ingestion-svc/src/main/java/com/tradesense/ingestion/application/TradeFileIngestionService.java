package com.tradesense.ingestion.application;

import com.tradesense.common.model.entity.enums.SourceType;
import com.tradesense.common.model.records.UploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface TradeFileIngestionService {
    UploadResult upload(MultipartFile file, SourceType sourceType);
}
