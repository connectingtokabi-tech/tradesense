package com.tradesense.processor.infrastructure.persistence;

import com.tradesense.common.model.entity.TradeInstrumentation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TradeInstrumentationRepository extends JpaRepository<TradeInstrumentation, UUID> {

    @Query("select instrumentation from TradeInstrumentation instrumentation join fetch instrumentation.trade")
    List<TradeInstrumentation> findAllWithTrade();
}
