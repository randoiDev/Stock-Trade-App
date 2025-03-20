package com.example.trading.simple_trading_app.dto.response;

import com.example.trading.simple_trading_app.enums.OrderType;

import java.math.BigDecimal;

import lombok.Builder;

public record OrderDTO(
        String stockSymbol,
        OrderType type,
        BigDecimal price,
        int quantity
) {
    @Builder
    public static OrderDTO of(
            String stockSymbol,
            OrderType type,
            BigDecimal price,
            int quantity
    ) {
        return new OrderDTO(stockSymbol, type, price, quantity);
    }
}

