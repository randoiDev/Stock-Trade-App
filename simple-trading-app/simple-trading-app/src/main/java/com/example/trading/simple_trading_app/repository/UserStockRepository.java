package com.example.trading.simple_trading_app.repository;

import com.example.trading.simple_trading_app.model.UserStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStockRepository extends JpaRepository<UserStock, Long> {

    // Fetch and LOCK a specific stock holding for a user (PESSIMISTIC_WRITE)
    @Query("SELECT us FROM UserStock us WHERE us.user.id = :userId AND us.stock.symbol = :stockSymbol")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserStock> findUserStockForUpdate(@Param("userId") Long userId, @Param("stockSymbol") String stockSymbol);
}
