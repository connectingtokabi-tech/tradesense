package com.tradesense.processor.application;

import com.tradesense.common.model.entity.Trade;
import com.tradesense.common.model.entity.TradeInstrumentation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/** In-process live event stream for trades successfully committed by the processor. */
@Component
@Slf4j
public class TradeSavedEventPublisher {

    private final Sinks.Many<TradeSavedNotification> events = Sinks.many()
            .multicast()
            .onBackpressureBuffer();

    public Flux<TradeSavedNotification> stream() {
        return events.asFlux();
    }

    public void publish(Trade trade, TradeInstrumentation instrumentation) {
        TradeSavedNotification notification = TradeSavedNotification.from(trade,
                new TradeSavedNotification.TradeInstrumentationData(
                        instrumentation.getInstrumentId(),
                        instrumentation.getQuantity(),
                        instrumentation.getTradePrice(),
                        instrumentation.getTradeTimestamp()));
        Sinks.EmitResult result = events.tryEmitNext(notification);
        if (result.isFailure()) {
            log.debug("Trade saved notification was not delivered for ingestion record {}: {}",
                    trade.getIngestionRecordId(), result);
        }
    }
}
