package com.example.trading.simple_trading_app.service;

import com.example.trading.simple_trading_app.dto.request.LoginRequestDTO;
import com.example.trading.simple_trading_app.dto.response.LoginResponseDTO;
import com.example.trading.simple_trading_app.dto.response.UserDTO;
import com.example.trading.simple_trading_app.dto.response.UserStockDTO;
import com.example.trading.simple_trading_app.model.User;
import com.example.trading.simple_trading_app.repository.UserRepository;
import com.example.trading.simple_trading_app.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
        return new LoginResponseDTO(jwtUtil.generateToken(loginRequest.username()));
    }

    public UserDTO getUserInfo() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new IllegalStateException("Server error occurred. Please try again.")
        );

        return new UserDTO(
                user.getUsername(),
                user.getBalance(),
                user.getStocks().stream()
                        .map(userStock -> new UserStockDTO(userStock.getStock().getSymbol(), userStock.getQuantity()))
                        .toList());
    }
}
