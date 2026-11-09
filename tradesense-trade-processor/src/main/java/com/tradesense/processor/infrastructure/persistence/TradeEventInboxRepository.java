package com.tradesense.processor.infrastructure.persistence;

import com.tradesense.common.model.entity.TradeEventInbox;
import com.tradesense.common.model.entity.TradeEventInboxId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TradeEventInboxRepository extends JpaRepository<TradeEventInbox, TradeEventInboxId> {

    @Modifying
    @Query(value = """
            INSERT INTO trade_event_inbox (event_id, consumer_name, payload)
            VALUES (:eventId, :consumerName, CAST(:payload AS jsonb))
            ON CONFLICT (event_id, consumer_name) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("eventId") java.util.UUID eventId,
            @Param("consumerName") String consumerName,
            @Param("payload") String payload);
}
