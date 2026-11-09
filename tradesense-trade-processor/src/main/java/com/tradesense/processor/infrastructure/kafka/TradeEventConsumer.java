package com.tradesense.processor.infrastructure.kafka;

import com.tradesense.common.model.entity.TradeEventInboxId;
import com.tradesense.processor.application.TradeEventInboxService;
import com.tradesense.processor.application.TradeProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/** Durably records incoming events before dispatching detached trade processing. */
@Component
@RequiredArgsConstructor
public class TradeEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TradeEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final TradeEventInboxService inboxService;
    private final TradeProcessingService processingService;
    @Value("${tradesense.kafka.consumer-name:tradesense-trade-processor}")
    private String consumerName;

    @KafkaListener(topics = "${tradesense.kafka.trade-events-topic}")
    public void consume(ConsumerRecord<String, String> record) {
        JsonNode payload = objectMapper.readTree(record.value());
        UUID eventId = eventIdFrom(payload, record);
        TradeEventInboxId inboxId = new TradeEventInboxId(eventId, consumerName);

        if (inboxService.record(inboxId, payload)) {
            log.info("Persisted trade event to inbox: topic={}, partition={}, offset={}, eventId={}",
                    record.topic(), record.partition(), record.offset(), eventId);
            processingService.process(inboxId);
        } else {
            log.debug("Skipping already received trade event {}", eventId);
        }
    }

    private UUID eventIdFrom(JsonNode payload, ConsumerRecord<String, String> record) {
        JsonNode eventId = payload.get("eventId");
        if (eventId != null && eventId.isString()) {
            return UUID.fromString(eventId.asString());
        }
        String kafkaPosition = record.topic() + ":" + record.partition() + ":" + record.offset();
        return UUID.nameUUIDFromBytes(kafkaPosition.getBytes(StandardCharsets.UTF_8));
    }
}
