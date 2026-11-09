package com.tradesense.validator.application;

import com.tradesense.common.model.dto.TradeRuleViolation;

import java.util.ArrayList;
import java.util.List;

public class TradeValidationResult {

    private final List<TradeRuleViolation> violations = new ArrayList<>();

    public void addViolation(TradeRuleViolation violation) {
        violations.add(violation);
    }

    public List<TradeRuleViolation> getViolations() {
        return List.copyOf(violations);
    }

    public boolean isValid() {
        return violations.isEmpty();
    }
}
