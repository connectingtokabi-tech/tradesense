package com.tradesense.processor.application;

import com.tradesense.common.model.entity.enums.SourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;

/** Extracts and normalizes the supported CDC envelope and raw trade payload shapes. */
@Component
@RequiredArgsConstructor
public class TradeCdcEventMapper {

    private final ObjectMapper objectMapper;

    public NormalizedTradeEvent map(JsonNode cdcEvent) {
        UUID ingestionRecordId = uuidField(cdcEvent, "record_id");
        if (ingestionRecordId == null) {
            throw new IllegalArgumentException("CDC event must include a valid record_id");
        }

        JsonNode payload = cdcEvent == null ? null : cdcEvent.get("raw_payload");
        if (payload == null || payload.isNull()) {
            payload = cdcEvent;
        } else if (payload.isString()) {
            try {
                payload = objectMapper.readTree(payload.asString());
            } catch (JacksonException invalidPayload) {
                throw new IllegalArgumentException("CDC event raw_payload must contain valid JSON", invalidPayload);
            }
        }

        String bookingSystem = textField(payload, "bookingSystem", "booking_system");
        if (bookingSystem == null || bookingSystem.isBlank()) {
            bookingSystem = textField(cdcEvent, "source_system");
        }
        SourceType sourceType = sourceTypeField(payload, "sourceType", "source_type");
        if (sourceType == null) {
            sourceType = sourceTypeField(cdcEvent, "source_type");
        }

        TradePayload tradePayload = new TradePayload(
                bookingSystem,
                longField(payload, "tradeId", "trade_id"),
                sourceType,
                textField(payload, "instrument", "instrumentId", "instrument_id", "product"),
                longField(payload, "quantity"),
                decimalField(payload, "price", "tradePrice", "trade_price"),
                timestampField(payload, "tradeTimestamp", "trade_timestamp", "tradeDate", "trade_date"));
        return new NormalizedTradeEvent(ingestionRecordId, tradePayload);
    }

    private UUID uuidField(JsonNode object, String field) {
        String value = textField(object, field);
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException invalidUuid) {
            return null;
        }
    }

    private SourceType sourceTypeField(JsonNode object, String... fields) {
        String value = textField(object, fields);
        if (value == null) {
            return null;
        }
        try {
            return SourceType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException invalidValue) {
            return null;
        }
    }

    private String textField(JsonNode object, String... fields) {
        JsonNode value = firstField(object, fields);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.isString() ? value.asString() : value.isNumber() ? value.toString() : null;
    }

    private Long longField(JsonNode object, String... fields) {
        JsonNode value = firstField(object, fields);
        if (value == null || value.isNull()) {
            return null;
        }
        try {
            if (value.isNumber()) {
                return value.longValue();
            }
            if (value.isString()) {
                return Long.valueOf(value.asString().trim());
            }
        } catch (NumberFormatException ignored) {
            // Preserve invalid values as null so Drools can report a validation violation.
        }
        return null;
    }

    private BigDecimal decimalField(JsonNode object, String... fields) {
        JsonNode value = firstField(object, fields);
        if (value == null || value.isNull()) {
            return null;
        }
        try {
            if (value.isNumber()) {
                return value.decimalValue();
            }
            if (value.isString()) {
                return new BigDecimal(value.asString().trim());
            }
        } catch (NumberFormatException ignored) {
            // Preserve invalid values as null so Drools can report a validation violation.
        }
        return null;
    }

    private OffsetDateTime timestampField(JsonNode object, String... fields) {
        String value = textField(object, fields);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            // Try common UTC and date-only CSV formats below.
        }
        try {
            return Instant.parse(value).atOffset(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            // Try local timestamps and date-only CSV values below.
        }
        try {
            return LocalDateTime.parse(value).atOffset(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            // EOD dates are interpreted at the start of the UTC date.
        }
        try {
            return LocalDate.parse(value).atStartOfDay().atOffset(ZoneOffset.UTC);
        } catch (DateTimeParseException invalidTimestamp) {
            return null;
        }
    }

    private JsonNode firstField(JsonNode object, String... fields) {
        if (object == null || !object.isObject()) {
            return null;
        }
        for (String field : fields) {
            JsonNode value = object.get(field);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
