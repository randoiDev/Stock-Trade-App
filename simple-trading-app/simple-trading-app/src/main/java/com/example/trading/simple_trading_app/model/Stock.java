package com.example.trading.simple_trading_app.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "stocks")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @Column(length = 10, unique = true, nullable = false) // Length 10 should be enough for this demo project
    private String symbol; // Stock ticker (AAPL, TSLA)

    @Column(nullable = false)
    private String companyName;

    @Column(precision = 7, scale = 2, nullable = false)
    private BigDecimal price; // Last traded price
}

