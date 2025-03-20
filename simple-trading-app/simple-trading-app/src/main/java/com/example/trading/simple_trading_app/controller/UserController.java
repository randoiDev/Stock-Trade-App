package com.example.trading.simple_trading_app.controller;

import com.example.trading.simple_trading_app.dto.request.LoginRequestDTO;
import com.example.trading.simple_trading_app.dto.response.LoginResponseDTO;
import com.example.trading.simple_trading_app.dto.response.UserDTO;
import com.example.trading.simple_trading_app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        return ResponseEntity.ok(userService.login(loginRequest));
    }

    @GetMapping("/info")
    public ResponseEntity<UserDTO> getUserInfo() {
        return ResponseEntity.ok(userService.getUserInfo());
    }
}
