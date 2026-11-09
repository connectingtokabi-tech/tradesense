package com.tradesense.common.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "trade_instrumentation")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeInstrumentation {

    @Builder(builderMethodName = "builder")
    public static TradeInstrumentation forTrade(
            Trade trade,
            String instrumentId,
            Long quantity,
            BigDecimal tradePrice,
            OffsetDateTime tradeTimestamp) {
        if (trade == null) {
            throw new IllegalArgumentException("Trade is required");
        }
        TradeInstrumentation instrumentation = new TradeInstrumentation();
        instrumentation.trade = trade;
        instrumentation.instrumentId = instrumentId;
        instrumentation.quantity = quantity;
        instrumentation.tradePrice = tradePrice;
        instrumentation.tradeTimestamp = tradeTimestamp;
        return instrumentation;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trade_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_trade_instrumentation_trade"))
    private Trade trade;

    @Column(name = "instrument_id", length = 100)
    private String instrumentId;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "trade_price", precision = 20, scale = 8)
    private BigDecimal tradePrice;

    @Column(name = "trade_timestamp")
    private OffsetDateTime tradeTimestamp;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
