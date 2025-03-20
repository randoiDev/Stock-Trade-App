package com.example.trading.simple_trading_app.filter;

import com.example.trading.simple_trading_app.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        // First we check if jwt is included
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String username;

        // Then we check if username can be extracted from jwt
        try {
            username = jwtUtil.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            handleJwtException(response, "JWT token has expired. Please log in again.");
            return;
        } catch (MalformedJwtException e) {
            handleJwtException(response, "Invalid JWT format. Ensure you're sending a valid token.");
            return;
        }

        // We also check if username has null for value
        if (username == null) {
            handleJwtException(response, "Could not extract username from JWT.");
            return;
        }

        // Validate token before loading user details to avoid unnecessary DB lookup
        if (!jwtUtil.isTokenValid(jwt, username)) {
            handleJwtException(response, "JWT token is invalid. Authentication failed.");
            return;
        }

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }

    private void handleJwtException(HttpServletResponse response, String errorMessage) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        Map<String, Object> json = Map.of(
                "timestamp", Instant.now().toString(),
                "error", errorMessage
        );

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(json));
        response.getWriter().flush();
    }
}


