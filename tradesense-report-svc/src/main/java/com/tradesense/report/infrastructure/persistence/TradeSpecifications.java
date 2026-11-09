package com.tradesense.report.infrastructure.persistence;

import com.tradesense.common.model.entity.Trade;
import com.tradesense.common.model.entity.enums.TradeStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public final class TradeSpecifications {

    private TradeSpecifications() {
    }

    public static Specification<Trade> filterTrades(
            TradeStatus status,
            String bookingSystem,
            OffsetDateTime startAt,
            OffsetDateTime endAt) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("tradeStatus"), status));
            }
            if (bookingSystem != null && !bookingSystem.isBlank()) {
                predicates.add(cb.equal(root.get("bookingSystem"), bookingSystem));
            }
            if (startAt != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startAt));
            }
            if (endAt != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), endAt));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

