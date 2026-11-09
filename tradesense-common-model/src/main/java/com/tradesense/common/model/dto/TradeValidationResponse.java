package com.tradesense.common.model.dto;

import java.util.List;

public record TradeValidationResponse(boolean valid, List<TradeRuleViolation> violations) {

    public TradeValidationResponse {
        violations = violations == null ? List.of() : List.copyOf(violations);
    }
}
