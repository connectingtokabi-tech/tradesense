package com.tradesense.common.model.dto;

public record TradeRuleViolation(
        String ruleName,
        String ruleVersion,
        String errorCode,
        String message) {
}
