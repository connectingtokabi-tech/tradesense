package com.tradesense.processor.application;

import com.tradesense.common.model.entity.Trade;
import com.tradesense.common.model.entity.TradeInstrumentation;
import com.tradesense.processor.infrastructure.persistence.TradeInstrumentationRepository;
import com.tradesense.processor.infrastructure.persistence.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TradeStreamService {

    private final TradeRepository tradeRepository;
    private final TradeInstrumentationRepository instrumentationRepository;
    private final TradeSavedEventPublisher eventPublisher;

    public Flux<TradeSavedNotification> streamSnapshotThenLive() {
        return Flux.<TradeSavedNotification>create(sink -> {
            Object lock = new Object();
            Set<UUID> snapshotRecordIds = new HashSet<>();
            Queue<TradeSavedNotification> eventsDuringSnapshot = new ArrayDeque<>();
            boolean[] snapshotComplete = {false};
            Disposable.Composite subscriptions = Disposables.composite();

            subscriptions.add(eventPublisher.stream().subscribe(event -> {
                synchronized (lock) {
                    if (sink.isCancelled()) {
                        return;
                    }
                    if (!snapshotComplete[0]) {
                        eventsDuringSnapshot.add(event);
                    } else if (!snapshotRecordIds.contains(event.ingestionRecordId())) {
                        sink.next(event);
                    }
                }
            }, sink::error));

            subscriptions.add(Mono.<java.util.List<TradeSavedNotification>>fromCallable(this::loadSnapshot)
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(snapshot -> {
                        synchronized (lock) {
                            if (sink.isCancelled()) {
                                return;
                            }
                            for (TradeSavedNotification notification : snapshot) {
                                snapshotRecordIds.add(notification.ingestionRecordId());
                                sink.next(notification);
                            }
                            snapshotComplete[0] = true;
                            while (!eventsDuringSnapshot.isEmpty()) {
                                TradeSavedNotification event = eventsDuringSnapshot.remove();
                                if (snapshotRecordIds.add(event.ingestionRecordId())) {
                                    sink.next(event);
                                }
                            }
                        }
                    }, sink::error));

            sink.onDispose(subscriptions::dispose);
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    private java.util.List<TradeSavedNotification> loadSnapshot() {
        Map<UUID, TradeSavedNotification.TradeInstrumentationData> instrumentationByTradeId = new HashMap<>();
        for (TradeInstrumentation instrumentation : instrumentationRepository.findAllWithTrade()) {
            instrumentationByTradeId.put(
                    instrumentation.getTrade().getId(),
                    new TradeSavedNotification.TradeInstrumentationData(
                            instrumentation.getInstrumentId(),
                            instrumentation.getQuantity(),
                            instrumentation.getTradePrice(),
                            instrumentation.getTradeTimestamp()));
        }
        return tradeRepository.findAll().stream()
                .map(trade -> {
                    TradeSavedNotification.TradeInstrumentationData instrumentation =
                            instrumentationByTradeId.get(trade.getId());
                    return instrumentation == null
                            ? null
                            : TradeSavedNotification.from(trade, instrumentation);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
