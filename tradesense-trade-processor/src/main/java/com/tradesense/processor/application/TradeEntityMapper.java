package com.tradesense.processor.application;

import com.tradesense.common.model.entity.Trade;
import com.tradesense.common.model.entity.TradeInstrumentation;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TradeEntityMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "ingestionRecordId", source = "ingestionRecordId")
    @Mapping(target = "bookingSystem", source = "payload.bookingSystem")
    @Mapping(target = "tradeId", source = "payload.tradeId")
    @Mapping(target = "sourceType", source = "payload.sourceType")
    Trade toTrade(UUID ingestionRecordId, TradePayload payload);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "trade", source = "trade")
    @Mapping(target = "instrumentId", source = "payload.instrumentId")
    @Mapping(target = "quantity", source = "payload.quantity")
    @Mapping(target = "tradePrice", source = "payload.tradePrice")
    @Mapping(target = "tradeTimestamp", source = "payload.tradeTimestamp")
    TradeInstrumentation toInstrumentation(Trade trade, TradePayload payload);
}
