package com.tradesense.common.model.entity;

import com.tradesense.common.model.entity.enums.TradeEventInboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.time.OffsetDateTime;

@Entity
@Table(name = "trade_event_inbox")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeEventInbox {

    public static TradeEventInbox received(TradeEventInboxId id, JsonNode payload) {
        if (id == null || payload == null || payload.isNull()) {
            throw new IllegalArgumentException("Inbox event ID and payload are required");
        }
        TradeEventInbox inbox = new TradeEventInbox();
        inbox.id = id;
        inbox.payload = payload;
        return inbox;
    }

    @EmbeddedId
    private TradeEventInboxId id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private JsonNode payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", length = 30, nullable = false)
    private TradeEventInboxStatus processingStatus = TradeEventInboxStatus.RECEIVED;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @CreationTimestamp
    @Column(name = "received_at", nullable = false, updatable = false)
    private OffsetDateTime receivedAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;
}
