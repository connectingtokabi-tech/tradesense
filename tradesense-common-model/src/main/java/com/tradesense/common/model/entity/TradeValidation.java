package com.tradesense.common.model.entity;

import com.tradesense.common.model.entity.enums.TradeValidationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "trade_validation")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeValidation {

    public static TradeValidation result(
            Trade trade,
            TradeValidationStatus status,
            String errorCode,
            String errorMessage,
            String ruleName,
            String ruleVersion) {
        if (trade == null || status == null) {
            throw new IllegalArgumentException("Trade and validation status are required");
        }
        TradeValidation validation = new TradeValidation();
        validation.trade = trade;
        validation.validationStatus = status;
        validation.errorCode = errorCode;
        validation.errorMessage = errorMessage;
        validation.ruleName = ruleName;
        validation.ruleVersion = ruleVersion;
        return validation;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trade_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_trade_validation_trade"))
    private Trade trade;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", length = 20, nullable = false)
    private TradeValidationStatus validationStatus;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "rule_name", length = 100)
    private String ruleName;

    @Column(name = "rule_version", length = 30)
    private String ruleVersion;

    @CreationTimestamp
    @Column(name = "validated_at", nullable = false, updatable = false)
    private OffsetDateTime validatedAt;
}
