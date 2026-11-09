package com.tradesense.ingestion.adapters.batch;

import com.tradesense.common.model.records.TradeCsvRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class TradeCsvItemReader implements ItemStreamReader<TradeCsvRow> {

    private static final String READ_COUNT_KEY = "tradeCsvItemReader.readCount";
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .get();

    private final Path filePath;
    private final ObjectMapper objectMapper;

    private CSVParser parser;
    private Iterator<CSVRecord> records;
    private List<String> headers;
    private long readCount;

    public TradeCsvItemReader(Path filePath, ObjectMapper objectMapper) {
        this.filePath = filePath;
        this.objectMapper = objectMapper;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            parser = CSVParser.parse(filePath, StandardCharsets.UTF_8, CSV_FORMAT);
            headers = parser.getHeaderNames();
            validateHeaders(headers);
            records = parser.iterator();

            long restartCount = executionContext.getLong(READ_COUNT_KEY, 0);
            for (long skipped = 0; skipped < restartCount; skipped++) {
                if (!records.hasNext()) {
                    throw new ItemStreamException("CSV file ended before the saved restart position");
                }
                records.next();
            }
            readCount = restartCount;
        } catch (IOException | IllegalArgumentException exception) {
            closeReader();
            throw new ItemStreamException("Could not open CSV file: " + filePath, exception);
        }
    }

    @Override
    public TradeCsvRow read() {
        if (records == null || !records.hasNext()) {
            return null;
        }

        CSVRecord record = records.next();
        long sourceRowNumber = record.getRecordNumber() + 1; // Data records start after the header row.
        if (record.size() != headers.size()) {
            throw new IllegalArgumentException("CSV row " + sourceRowNumber + " has "
                    + record.size() + " columns; expected " + headers.size() + " from the header");
        }

        ObjectNode payload = objectMapper.createObjectNode();
        for (int i = 0; i < headers.size(); i++) {
            payload.put(headers.get(i), record.get(i));
        }
        readCount++;
        return new TradeCsvRow(sourceRowNumber, payload);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putLong(READ_COUNT_KEY, readCount);
    }

    @Override
    public void close() throws ItemStreamException {
        closeReader();
    }

    private void validateHeaders(List<String> headers) {
        if (headers.isEmpty()) {
            throw new IllegalArgumentException("CSV file must contain a header row");
        }
        Set<String> uniqueHeaders = new HashSet<>();
        for (String header : headers) {
            if (header == null || header.isBlank() || !uniqueHeaders.add(header)) {
                throw new IllegalArgumentException("CSV headers must be non-empty and unique");
            }
        }
    }

    private void closeReader() {
        if (parser != null) {
            try {
                parser.close();
            } catch (IOException ignored) {
                // Preserve the original reader or job failure.
            } finally {
                parser = null;
                records = null;
            }
        }
    }
}
