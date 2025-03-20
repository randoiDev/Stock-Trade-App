package com.example.trading.simple_trading_app.service;

import com.example.trading.simple_trading_app.dto.request.CreateOrderDTO;
import com.example.trading.simple_trading_app.dto.response.MessageDTO;
import com.example.trading.simple_trading_app.dto.response.OrderDTO;
import com.example.trading.simple_trading_app.enums.OrderStatus;
import com.example.trading.simple_trading_app.enums.OrderType;
import com.example.trading.simple_trading_app.exception.NotFoundException;
import com.example.trading.simple_trading_app.model.Order;
import com.example.trading.simple_trading_app.model.Stock;
import com.example.trading.simple_trading_app.model.User;
import com.example.trading.simple_trading_app.model.UserStock;
import com.example.trading.simple_trading_app.repository.OrderRepository;
import com.example.trading.simple_trading_app.repository.StockRepository;
import com.example.trading.simple_trading_app.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderAsyncService orderAsyncService;
    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final java.util.Random random = new java.util.Random();

    public MessageDTO placeOrder(CreateOrderDTO createOrderDTO) throws IllegalAccessException {

        // First we check if a stock exists for passed stock symbol
        Stock stock = stockRepository.findBySymbol(createOrderDTO.stockSymbol())
                .orElseThrow(() -> new NotFoundException("You cannot place order for stock that doesn't exist."));

        // Then we gather info on user placing order
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new IllegalStateException("Server error occurred. Please try again.")
        );

        final BigDecimal lockedPrice = stock.getPrice();

        // We also must check that user has sufficient funds if he placed BUY order
        if(createOrderDTO.type().equals(OrderType.BUY)
                && lockedPrice.multiply(BigDecimal.valueOf(createOrderDTO.quantity())).compareTo(user.getBalance()) > 0)
            throw new IllegalAccessException("Insufficient balance for buying stocks of mentioned quantity and type.");

        // If the order is of type SELL then we must check if user has mentioned stocks in proposed quantity
        if(createOrderDTO.type().equals(OrderType.SELL) &&
                user.getStocks().stream()
                        .filter(innerStock -> innerStock.getStock().getSymbol().equals(createOrderDTO.stockSymbol()))
                        .mapToInt(UserStock::getQuantity)
                        .sum() < createOrderDTO.quantity())
            throw new IllegalAccessException("Insufficient quantity for selling stocks of mentioned quantity.");

        // Create order entity and save it
        Order newOrder = Order.builder()
                .stock(stock)
                .user(user)
                .type(createOrderDTO.type())
                .price(lockedPrice)
                .quantity(createOrderDTO.quantity())
                .status(OrderStatus.NEW)
                .timestamp(Instant.now())
                .build();
        orderRepository.save(newOrder);

        // Invoke async method for matching orders
        orderAsyncService.matchOrderAsync(newOrder.getId());

        return new MessageDTO("Order placed successfully.");
    }

    @Transactional(timeout = 10, rollbackFor = IllegalAccessException.class)
    public MessageDTO revokeOrder(Long orderId) throws IllegalAccessException {

        // Fetch the order from DB
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order with specified id doesn't exist."));

        // Verify that the logged-in user owns the order
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!order.getUser().getUsername().equals(userDetails.getUsername())) {
            throw new IllegalAccessException("You are not the owner of the order you are trying to revoke.");
        }

        // Ensure order is still in "NEW" state
        if (order.getStatus() != OrderStatus.NEW) {
            return new MessageDTO("Order cannot be revoked as it is already being processed or matched.");
        }

        // Try to update the status to CANCELLED (Optimistic Locking will be applied)
        order.setStatus(OrderStatus.CANCELLED);

        try {
            orderRepository.save(order);
            return new MessageDTO("Order successfully revoked.");
        } catch (OptimisticLockException e) {

            // If an Optimistic Lock failure occurs, re-fetch the latest order status
            Order latestOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new NotFoundException("Order no longer exists."));

            if (latestOrder.getStatus() == OrderStatus.LOCKED) {
                return new MessageDTO("Order is being processed, try again later...");
            } else if (latestOrder.getStatus() == OrderStatus.MATCHED) {
                return new MessageDTO("Order has already been matched and cannot be revoked.");
            } else {
                return new MessageDTO("Failed to revoke due to some error, please try again later...");
            }
        }
    }

    public List<OrderDTO> getTop10BuyOrders(String stockSymbol) {
        List<Order> orders =  orderRepository.findTop10BuyOrders(stockSymbol, PageRequest.of(0, 10));

        return orders.stream().map(order ->
                OrderDTO.builder()
                        .stockSymbol(order.getStock().getSymbol())
                        .price(order.getPrice())
                        .type(order.getType())
                        .quantity(order.getQuantity()).build())
                .toList();
    }

    public List<OrderDTO> getTop10SellOrders(String stockSymbol) {
        List<Order> orders =  orderRepository.findTop10SellOrders(stockSymbol, PageRequest.of(0, 10));

        return orders.stream().map(order ->
                        OrderDTO.builder()
                                .stockSymbol(order.getStock().getSymbol())
                                .price(order.getPrice())
                                .type(order.getType())
                                .quantity(order.getQuantity()).build())
                .toList();
    }

    @Scheduled(fixedRate = 20000) // Run every 20 seconds
    @Transactional
    public void updateStockPrices() {
        List<Stock> stocks = stockRepository.findAll();

        if (stocks.isEmpty()) {
            logger.warn("No stocks found in the database.");
            return;
        }

        for (Stock stock : stocks) {
            BigDecimal newPrice = BigDecimal.valueOf(random.nextDouble() * 1000); // Random price up to 1000
            stock.setPrice(newPrice);
        }

        stockRepository.saveAll(stocks); // Save updated prices
        logger.info("Updated stock prices at: {}",Instant.now());
    }

}
