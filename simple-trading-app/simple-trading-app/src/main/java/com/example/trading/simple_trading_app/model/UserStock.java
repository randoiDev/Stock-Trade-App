package com.example.trading.simple_trading_app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_stocks")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_symbol", referencedColumnName = "symbol", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private int quantity;
}

