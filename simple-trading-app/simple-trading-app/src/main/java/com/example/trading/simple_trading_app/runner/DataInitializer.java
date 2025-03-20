package com.example.trading.simple_trading_app.runner;

import com.example.trading.simple_trading_app.model.Stock;
import com.example.trading.simple_trading_app.model.User;
import com.example.trading.simple_trading_app.model.UserStock;
import com.example.trading.simple_trading_app.repository.StockRepository;
import com.example.trading.simple_trading_app.repository.UserRepository;
import com.example.trading.simple_trading_app.repository.UserStockRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final UserStockRepository userStockRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        logger.info("Starting data initialization...");

        // Create Users
        User user1 = createUser("johndoe01", "password1", new BigDecimal("100000"));
        User user2 = createUser("johndoe02", "password2", new BigDecimal("100000"));

        // Create Stocks
        Stock stock1 = createStock("AAPL", "Apple Inc.", new BigDecimal("150.50"));
        Stock stock2 = createStock("TSLA", "Tesla Inc.", new BigDecimal("780.20"));
        Stock stock3 = createStock("GOOGL", "Alphabet Inc.", new BigDecimal("2750.30"));
        Stock stock4 = createStock("AMZN", "Amazon.com Inc.", new BigDecimal("3450.00"));

        // Assign Stocks to Users (10 shares each)
        assignStockToUser(user1, stock1);
        assignStockToUser(user1, stock2);
        assignStockToUser(user1, stock3);
        assignStockToUser(user1, stock4);

        assignStockToUser(user2, stock1);
        assignStockToUser(user2, stock2);
        assignStockToUser(user2, stock3);
        assignStockToUser(user2, stock4);

        logger.info("Data initialization completed successfully!");
    }

    private User createUser(String username, String password, BigDecimal balance) {
        return userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .balance(balance)
                .build());
    }

    private Stock createStock(String symbol, String companyName, BigDecimal price) {
        return stockRepository.save(new Stock(symbol, companyName, price));
    }

    private void assignStockToUser(User user, Stock stock) {
        UserStock userStock = new UserStock();
        userStock.setUser(user);
        userStock.setStock(stock);
        userStock.setQuantity(10);
        userStockRepository.save(userStock);
    }
}

