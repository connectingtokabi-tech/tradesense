package com.tradesense.processor.adapters.rest;

import com.tradesense.processor.application.TradeStreamService;
import com.tradesense.processor.application.TradeSavedNotification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
public class TradeStreamController {

    private static final Logger log = LoggerFactory.getLogger(TradeStreamController.class);

    private final TradeStreamService tradeStreamService;

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<TradeSavedNotification>> streamSavedTrades() {
        Flux<ServerSentEvent<TradeSavedNotification>> tradeEvents = tradeStreamService.streamSnapshotThenLive()
                .map(notification -> ServerSentEvent.builder(notification)
                        .event("trade-saved")
                        .id(notification.ingestionRecordId().toString())
                        .build());
        Flux<ServerSentEvent<TradeSavedNotification>> keepAlives = Flux.interval(Duration.ofSeconds(15))
                .map(ignored -> ServerSentEvent.<TradeSavedNotification>builder()
                        .comment("keepalive")
                        .build());
        return Flux.merge(tradeEvents, keepAlives)
                .doOnSubscribe(ignored -> log.info("Trade stream client connected"))
                .doFinally(signal -> log.info("Trade stream client disconnected: signal={}", signal));
    }
}
