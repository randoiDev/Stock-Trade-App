package com.example.trading.simple_trading_app.model;

import com.example.trading.simple_trading_app.enums.OrderStatus;
import com.example.trading.simple_trading_app.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The user who placed the order

    @ManyToOne
    @JoinColumn(name = "stock_symbol", referencedColumnName = "symbol", nullable = false)
    private Stock stock; // The stock being traded (AAPL, TSLA)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type; // BUY or SELL

    @Column(precision = 7, scale = 2, nullable = false)
    private BigDecimal price; // Price per share

    @Column(nullable = false)
    private int quantity; // Number of shares

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; // NEW, MATCHED, CANCELLED

    @Column(nullable = false)
    private Instant timestamp; // Time when order was placed

    @Version // Enables Optimistic Locking
    private int version;
}

