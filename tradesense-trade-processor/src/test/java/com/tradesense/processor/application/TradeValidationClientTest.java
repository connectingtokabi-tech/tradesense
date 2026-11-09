package com.tradesense.processor.application;

import com.tradesense.common.model.dto.TradeValidationRequest;
import com.tradesense.common.model.dto.TradeValidationResponse;
import com.tradesense.common.model.entity.Trade;
import com.tradesense.common.model.entity.TradeInstrumentation;
import com.tradesense.common.model.entity.enums.SourceType;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradeValidationClientTest {

    @Test
    void validate_shouldReturnResponseFromValidatorService() {
        WebClient.Builder builder = mock(WebClient.Builder.class);
        WebClient webClient = mock(WebClient.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        TradeValidationResponse expected = new TradeValidationResponse(true, List.of());

        when(builder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any(TradeValidationRequest.class)))
                .thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TradeValidationResponse.class)).thenReturn(Mono.just(expected));

        CircuitBreakerFactory<?, ?> circuitBreakerFactory = mock(CircuitBreakerFactory.class);
        CircuitBreaker circuitBreaker = mock(CircuitBreaker.class);
        when(circuitBreakerFactory.create("validatorService")).thenReturn(circuitBreaker);
        when(circuitBreaker.run(
                (Supplier<TradeValidationResponse>) any(),
                (Function<Throwable, TradeValidationResponse>) any()))
                .thenAnswer(invocation -> {
                    Supplier<TradeValidationResponse> supplier = invocation.getArgument(0);
                    return supplier.get();
                });

        TradeValidationClient client = new TradeValidationClient(builder, circuitBreakerFactory);
        Trade trade = Trade.pendingValidation(UUID.randomUUID(), "BATS", 123L, SourceType.EOD);
        TradeInstrumentation instrumentation = TradeInstrumentation.forTrade(
                trade, "IBM", 100L, new BigDecimal("10.00"), OffsetDateTime.now());

        TradeValidationResponse result = client.validate(UUID.randomUUID(), trade, instrumentation);

        assertTrue(result.valid());
    }
}
