package com.example.trading.simple_trading_app.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank String username,
        @NotBlank String password
) {
}
