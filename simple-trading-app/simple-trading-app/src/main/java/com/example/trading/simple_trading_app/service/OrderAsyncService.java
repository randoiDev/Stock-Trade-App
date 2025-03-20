package com.example.trading.simple_trading_app.service;

import com.example.trading.simple_trading_app.enums.OrderStatus;
import com.example.trading.simple_trading_app.enums.OrderType;
import com.example.trading.simple_trading_app.model.Order;
import com.example.trading.simple_trading_app.model.User;
import com.example.trading.simple_trading_app.model.UserStock;
import com.example.trading.simple_trading_app.repository.OrderRepository;
import com.example.trading.simple_trading_app.repository.UserRepository;
import com.example.trading.simple_trading_app.repository.UserStockRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderAsyncService {

    private final Logger logger = LoggerFactory.getLogger(OrderAsyncService.class);
    private final TradeNotificationService tradeNotificationService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final UserStockRepository userStockRepository;

    @Async
    @Transactional(timeout = 10)
    public void matchOrderAsync(Long orderId) {
        try {

            // Try to lock `processingOrder` if its status is NEW ( Change it to LOCKED )
            Optional<Order> processingOrder = orderRepository.findById(orderId);

            if (processingOrder.isEmpty() || processingOrder.get().getStatus() != OrderStatus.NEW) {
                logger.info("Order [{}] is already matched or does not exist. Exiting...", orderId);
                return;
            }

            Order newOrder = processingOrder.get();
            newOrder.setStatus(OrderStatus.LOCKED);

            try {
                orderRepository.save(newOrder);
                logger.info("Order [{}] successfully locked.", orderId);
            } catch (OptimisticLockException e) {
                logger.warn("Optimistic lock failed for order [{}]. Rolling back.", orderId);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return;
            }

            // Fetch oldest matching orders in FIFO order ( Omitting pageable since this is a demo app )
            List<Order> matchingOrders = orderRepository.findOldestMatchingOrder(
                    newOrder.getStock().getSymbol(),
                    newOrder.getPrice(),
                    newOrder.getType()
            );

            if (matchingOrders.isEmpty()) {
                logger.info("No matching orders found for order [{}]. Rolling back.", orderId);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return;
            }

            Order matchedOrder = null;

            // Iterate through matching orders and try to lock one
            for (Order order : matchingOrders) {

                order.setStatus(OrderStatus.LOCKED);

                try {
                    orderRepository.save(order);
                    matchedOrder = order;
                    logger.info("Matched order [{}] successfully locked.", matchedOrder.getId());
                    break;
                } catch (OptimisticLockException e) {
                    logger.warn("Optimistic lock failed on order [{}]. Trying next...", order.getId());
                }
            }

            // If no orders were successfully locked, rollback
            if (matchedOrder == null) {
                logger.info("No available matching order could be locked for order [{}]. Rolling back.", orderId);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return;
            }

            // Execute trade
            executeTrade(newOrder, matchedOrder);
            logger.info("Trade executed between orders [{}] and [{}].", newOrder.getId(), matchedOrder.getId());

        } catch (Exception e) {
            logger.error("Unexpected error while matching order [{}]: {}", orderId, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }


    @Transactional(timeout = 10)
    public void executeTrade(Order newOrder, Order matchedOrder) {
        User buyer, seller;
        OrderType buyOrderType = OrderType.BUY;

        if (newOrder.getType() == buyOrderType) {
            buyer = newOrder.getUser();
            seller = matchedOrder.getUser();
        } else {
            buyer = matchedOrder.getUser();
            seller = newOrder.getUser();
        }

        // Lock buyer and seller accounts with PESSIMISTIC_WRITE
        buyer = userRepository.findByUsernameForUpdate(buyer.getUsername())
                .orElseThrow(() -> new IllegalStateException("Buyer not found."));
        seller = userRepository.findByUsernameForUpdate(seller.getUsername())
                .orElseThrow(() -> new IllegalStateException("Seller not found."));

        // Calculate trade details
        int tradeQuantity = Math.min(newOrder.getQuantity(), matchedOrder.getQuantity());
        BigDecimal tradePrice = matchedOrder.getPrice(); // Execute trade at matched price
        BigDecimal totalCost = tradePrice.multiply(BigDecimal.valueOf(tradeQuantity));


        // Deduct money from buyer & add to seller
        buyer.setBalance(buyer.getBalance().subtract(totalCost));
        seller.setBalance(seller.getBalance().add(totalCost));

        // Lock & update buyer's stock portfolio
        UserStock buyerStock = userStockRepository.findUserStockForUpdate(buyer.getId(), newOrder.getStock().getSymbol())
                .orElse(UserStock.builder().stock(newOrder.getStock()).user(buyer).quantity(0).build());
        buyerStock.setQuantity(buyerStock.getQuantity() + tradeQuantity);
        userStockRepository.save(buyerStock);

        // Lock & update seller's stock portfolio
        UserStock sellerStock = userStockRepository.findUserStockForUpdate(seller.getId(), newOrder.getStock().getSymbol())
                .orElseThrow(() -> new RuntimeException("Seller does not own enough stock"));

        if (sellerStock.getQuantity() < tradeQuantity) {
            throw new RuntimeException("Seller does not have enough shares to sell.");
        }
        sellerStock.setQuantity(sellerStock.getQuantity() - tradeQuantity);
        userStockRepository.save(sellerStock);

        // Save updated buyer & seller data
        userRepository.save(buyer);
        userRepository.save(seller);

        // Update and save order status
        newOrder.setStatus(OrderStatus.MATCHED);
        matchedOrder.setStatus(OrderStatus.MATCHED);
        orderRepository.save(newOrder);
        orderRepository.save(matchedOrder);

        tradeNotificationService
                .sendTradeUpdate(
                        String.format("Trade executed: %s shares of %s at %s between buyer [%s] and seller [%s]",
                                tradeQuantity, newOrder.getStock().getSymbol(), tradePrice, buyer.getUsername(), seller.getUsername()));

    }
}
