package com.tradesense.report.infrastructure.persistence;

import com.tradesense.common.model.entity.Trade;
import com.tradesense.common.model.entity.TradeInstrumentation;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/** Reporting-only queries. This repository intentionally exposes no write operations. */
public interface TradeReportRepository extends Repository<Trade, UUID>, JpaSpecificationExecutor<Trade> {

    @Query("select t from Trade t where t.id = :id")
    Optional<Trade> findReportById(@Param("id") UUID id);

    @Query("select i from TradeInstrumentation i join fetch i.trade t where t.id = :tradeId")
    Optional<TradeInstrumentation> findInstrumentationByTradeId(@Param("tradeId") UUID tradeId);
}
