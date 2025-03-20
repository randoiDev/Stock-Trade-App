package com.example.trading.simple_trading_app.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record UserDTO(
        String username,
        BigDecimal balance,
        List<UserStockDTO> stocks
) {
}
