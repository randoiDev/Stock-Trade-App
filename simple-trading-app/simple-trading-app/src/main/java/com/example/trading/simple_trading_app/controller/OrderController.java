package com.example.trading.simple_trading_app.controller;

import com.example.trading.simple_trading_app.dto.request.CreateOrderDTO;
import com.example.trading.simple_trading_app.dto.response.MessageDTO;
import com.example.trading.simple_trading_app.dto.response.OrderDTO;
import com.example.trading.simple_trading_app.model.Order;
import com.example.trading.simple_trading_app.service.OrderService;
import com.example.trading.simple_trading_app.service.TradeNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final TradeNotificationService tradeNotificationService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTrades() {
        return tradeNotificationService.createEmitter();
    }

    @PostMapping(value = "/place")
    public ResponseEntity<MessageDTO> placeOrder(@Valid @RequestBody CreateOrderDTO createOrderDTO) throws IllegalAccessException {
        return ResponseEntity.ok(orderService.placeOrder(createOrderDTO));
    }

    @DeleteMapping(value = "/revoke/{orderId}")
    public ResponseEntity<MessageDTO> revokeOrder(@PathVariable Long orderId) throws IllegalAccessException {
        MessageDTO response = orderService.revokeOrder(orderId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/top-buy/{stockSymbol}")
    public ResponseEntity<List<OrderDTO>> getTopBuyOrders(@PathVariable String stockSymbol) {
        return ResponseEntity.ok(orderService.getTop10BuyOrders(stockSymbol));
    }

    @GetMapping("/top-sell/{stockSymbol}")
    public ResponseEntity<List<OrderDTO>> getTopSellOrders(@PathVariable String stockSymbol) {
        return ResponseEntity.ok(orderService.getTop10SellOrders(stockSymbol));
    }

}
