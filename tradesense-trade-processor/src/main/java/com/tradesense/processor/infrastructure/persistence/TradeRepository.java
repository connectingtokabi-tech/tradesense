package com.tradesense.processor.infrastructure.persistence;

import com.tradesense.common.model.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, UUID> {

    Optional<Trade> findByIngestionRecordId(UUID ingestionRecordId);
}
