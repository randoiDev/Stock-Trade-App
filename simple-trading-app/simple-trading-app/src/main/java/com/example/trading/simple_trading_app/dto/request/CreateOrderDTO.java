package com.example.trading.simple_trading_app.dto.request;

import com.example.trading.simple_trading_app.enums.OrderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderDTO(
        @NotBlank(message = "Stock symbol is required!") String stockSymbol,
        @NotNull(message = "Order type (BUY/SELL) is required!") OrderType type,
        @NotNull(message = "Quantity is required!") @Min(value = 1, message = "Quantity must be one or greater!") int quantity
) {
}
