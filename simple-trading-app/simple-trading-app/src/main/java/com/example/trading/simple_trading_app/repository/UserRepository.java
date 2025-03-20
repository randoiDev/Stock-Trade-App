package com.example.trading.simple_trading_app.repository;

import com.example.trading.simple_trading_app.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.username = :username")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<User> findByUsernameForUpdate(@Param("username") String username);

}
