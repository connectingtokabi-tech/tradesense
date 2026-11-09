package com.tradesense.common.model.entity;

import com.tradesense.common.model.entity.enums.SourceType;
import com.tradesense.common.model.entity.enums.TradeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "trade")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trade {

    @Builder(builderMethodName = "builder")
    public static Trade pendingValidation(
            UUID ingestionRecordId,
            String bookingSystem,
            Long tradeId,
            SourceType sourceType) {
        if (ingestionRecordId == null) {
            throw new IllegalArgumentException("Ingestion record ID is required");
        }
        Trade trade = new Trade();
        trade.ingestionRecordId = ingestionRecordId;
        trade.bookingSystem = bookingSystem;
        trade.tradeId = tradeId;
        trade.sourceType = sourceType;
        trade.tradeStatus = TradeStatus.INVALID;
        return trade;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "ingestion_record_id", nullable = false, unique = true, updatable = false)
    private UUID ingestionRecordId;

    @Column(name = "booking_system", length = 100)
    private String bookingSystem;

    @Column(name = "trade_id")
    private Long tradeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 20)
    private SourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_status", length = 20, nullable = false)
    private TradeStatus tradeStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
