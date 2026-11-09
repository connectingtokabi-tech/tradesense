package com.tradesense.processor.infrastructure.persistence;

import com.tradesense.common.model.entity.TradeValidation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TradeValidationRepository extends JpaRepository<TradeValidation, UUID> {
}
