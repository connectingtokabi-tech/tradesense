package com.tradesense.ingestion.adapters.batch;

import com.tradesense.common.model.records.ParsedTradeCsvRow;
import com.tradesense.common.model.records.TradeCsvRow;
import org.springframework.batch.infrastructure.item.ItemProcessor;


public class TradeCsvItemProcessor implements ItemProcessor<TradeCsvRow, ParsedTradeCsvRow> {

    private final String sourceSystem;

    public TradeCsvItemProcessor(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    @Override
    public ParsedTradeCsvRow process(TradeCsvRow row) {
        if (row.rawPayload() == null || row.rawPayload().isEmpty()) {
            throw new IllegalArgumentException(
                    "CSV row " + row.sourceRowNumber() + " does not contain any values");
        }
        return new ParsedTradeCsvRow(row.sourceRowNumber(), sourceSystem, row.rawPayload());
    }
}
