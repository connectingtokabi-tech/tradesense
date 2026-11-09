package com.tradesense.validator.adapters.rest;

import com.tradesense.common.model.dto.TradeValidationRequest;
import com.tradesense.common.model.dto.TradeValidationResponse;
import com.tradesense.validator.application.TradeValidationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/validation/trades")
@RequiredArgsConstructor
public class TradeValidationController {

    private static final Logger log = LoggerFactory.getLogger(TradeValidationController.class);

    private final TradeValidationService tradeValidationService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TradeValidationResponse validate(@RequestBody TradeValidationRequest request) {
        TradeValidationResponse result = tradeValidationService.validate(request);
        log.info("Validated trade record {}: valid={}, violationCount={}",
                request.ingestionRecordId(), result.valid(), result.violations().size());
        return result;
    }
}
