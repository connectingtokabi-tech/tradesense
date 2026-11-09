package com.tradesense.gateway;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Locale;

/** Forwards the active W3C trace context on gateway requests to downstream services. */
@Component
public class TraceparentForwardingFilter implements GlobalFilter, Ordered {

    private final Tracer tracer;

    public TraceparentForwardingFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Span activeSpan = tracer.currentSpan();
        if (activeSpan == null) {
            return chain.filter(exchange);
        }

        TraceContext context = activeSpan.context();
        String flags = Boolean.TRUE.equals(context.sampled()) ? "01" : "00";
        String traceparent = "00-%s-%s-%s".formatted(
                context.traceId().toLowerCase(Locale.ROOT),
                context.spanId().toLowerCase(Locale.ROOT),
                flags);
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> headers.set("traceparent", traceparent))
                .build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
