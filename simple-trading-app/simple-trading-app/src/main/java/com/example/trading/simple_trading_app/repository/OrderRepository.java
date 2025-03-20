package com.example.trading.simple_trading_app.repository;

import com.example.trading.simple_trading_app.enums.OrderType;
import com.example.trading.simple_trading_app.model.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Fetch Top 10 Buy Orders for a Stock (Sorted by Price Descending)
    @Query("""
        SELECT o FROM Order o 
        WHERE o.stock.symbol = :stockSymbol 
        AND o.type = 'BUY' 
        AND o.status = 'NEW'
        ORDER BY o.price DESC, o.timestamp ASC
    """)
    List<Order> findTop10BuyOrders(@Param("stockSymbol") String stockSymbol, Pageable pageable);

    // Fetch Top 10 Sell Orders for a Stock (Sorted by Price Ascending)
    @Query("""
        SELECT o FROM Order o 
        WHERE o.stock.symbol = :stockSymbol 
        AND o.type = 'SELL' 
        AND o.status = 'NEW'
        ORDER BY o.price ASC, o.timestamp ASC
    """)
    List<Order> findTop10SellOrders(@Param("stockSymbol") String stockSymbol, Pageable pageable);

    // FIFO finding of all orders for specified order type
    @Query("""
                SELECT o FROM Order o 
                WHERE o.stock.symbol = :stockSymbol 
                AND o.type <> :orderType 
                AND o.status = 'NEW'
                AND (
                    (:orderType = 'BUY' AND o.price <= :orderPrice) OR
                    (:orderType = 'SELL' AND o.price >= :orderPrice)
                )
                ORDER BY o.timestamp ASC
            """)
    List<Order> findOldestMatchingOrder(@Param("stockSymbol") String stockSymbol,
                                        @Param("orderPrice") BigDecimal orderPrice,
                                        @Param("orderType") OrderType orderType);


    Optional<Order> findOrderById(Long id);
}
