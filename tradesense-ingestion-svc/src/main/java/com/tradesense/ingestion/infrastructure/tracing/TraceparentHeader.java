package com.tradesense.ingestion.infrastructure.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;

import java.util.Locale;

public final class TraceparentHeader {

    private TraceparentHeader() {
    }

    public static String fromCurrentSpan(Tracer tracer) {
        Span span = tracer.currentSpan();
        if (span == null) {
            return null;
        }
        TraceContext context = span.context();
        String flags = Boolean.TRUE.equals(context.sampled()) ? "01" : "00";
        return "00-%s-%s-%s".formatted(
                context.traceId().toLowerCase(Locale.ROOT),
                context.spanId().toLowerCase(Locale.ROOT),
                flags);
    }
}
