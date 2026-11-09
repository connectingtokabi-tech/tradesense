package com.tradesense.ingestion.application;

import com.tradesense.common.model.entity.IngestionBatch;
import com.tradesense.common.model.entity.enums.SourceType;
import com.tradesense.common.model.records.UploadResult;
import com.tradesense.ingestion.infrastructure.persistence.IngestionBatchRepository;
import com.tradesense.ingestion.infrastructure.storage.FileStorageManager;
import com.tradesense.ingestion.infrastructure.tracing.TraceparentHeader;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class TradeFileIngestionServiceImpl implements TradeFileIngestionService {

    private final IngestionBatchRepository batchRepository;
    private final FileStorageManager fileStorageManager;
    private final Tracer tracer;

    @Transactional
    @Override
    public UploadResult upload(MultipartFile file, SourceType sourceType) {
        if (sourceType != SourceType.EOD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "CSV file uploads are for EOD feeds; intraday records must come from MQ");
        }
        String fileName = safeFileName(file.getOriginalFilename());
        String checksum = calculateSha256(file);

        if (batchRepository.existsByFileChecksum(checksum)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This file has already been uploaded (SHA-256 checksum already exists)");
        }

        Path storedFile = fileStorageManager.createStoredFile(fileName);

        try {
            fileStorageManager.saveFile(file, storedFile);

            String absoluteLocation = storedFile.toString();
            if (absoluteLocation.length() > 500) {
                fileStorageManager.deleteIfExists(storedFile);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Configured storage path exceeds the database column limit");
            }

            String batchName = fileName.length() <= 150 ? fileName : fileName.substring(0, 150);
            IngestionBatch batch = new IngestionBatch(
                    batchName, sourceType, fileName, absoluteLocation, checksum);
            batch.setTraceparent(TraceparentHeader.fromCurrentSpan(tracer));
            batch = batchRepository.saveAndFlush(batch);

            return new UploadResult(batch, file.getSize(), checksum);
        } catch (DataIntegrityViolationException exception) {
            fileStorageManager.deleteIfExists(storedFile);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This file has already been uploaded (SHA-256 checksum already exists)", exception);
        } catch (RuntimeException exception) {
            fileStorageManager.deleteIfExists(storedFile);
            throw exception;
        }
    }

    private String safeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File name is required");
        }
        String normalized = originalFileName.replace('\\', '/');
        String fileName = normalized.substring(normalized.lastIndexOf('/') + 1);
        if (fileName.isBlank() || fileName.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File name must be between 1 and 255 characters");
        }
        return fileName;
    }

    private String calculateSha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = new DigestInputStream(file.getInputStream(), digest)) {
                input.transferTo(java.io.OutputStream.nullOutputStream());
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not read uploaded file", exception);
        }
    }
}

