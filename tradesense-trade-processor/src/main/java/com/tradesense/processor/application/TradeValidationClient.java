package com.tradesense.processor.application;

import com.tradesense.common.model.dto.TradeValidationRequest;
import com.tradesense.common.model.dto.TradeValidationResponse;
import com.tradesense.common.model.entity.Trade;
import com.tradesense.common.model.entity.TradeInstrumentation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

/** Creates validation requests and communicates with the validator service. */
@Component
@RequiredArgsConstructor
public class TradeValidationClient {

    private final WebClient.Builder webClientBuilder;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    @Value("${tradesense.validator.base-url:http://localhost:5053}")
    private String validatorBaseUrl;

    public TradeValidationResponse validate(
            UUID ingestionRecordId,
            Trade trade,
            TradeInstrumentation instrumentation) {
        TradeValidationRequest request = new TradeValidationRequest(
                ingestionRecordId,
                trade,
                instrumentation);
        return circuitBreakerFactory.create("validatorService").run(() -> {
            TradeValidationResponse response = webClientBuilder.build()
                .post()
                .uri(validatorBaseUrl + "/api/v1/validation/trades")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TradeValidationResponse.class)
                .block();
            if (response == null) {
                throw new IllegalStateException("Validator service returned an empty response");
            }
            return response;
        }, failure -> propagate(failure));
    }

    private TradeValidationResponse propagate(Throwable failure) {
        if (failure instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new IllegalStateException("Validator service request failed", failure);
    }
}
