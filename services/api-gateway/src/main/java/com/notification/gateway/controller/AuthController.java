package com.notification.gateway.controller;

import com.notification.gateway.dto.AuthRequest;
import com.notification.gateway.dto.AuthResponse;
import com.notification.gateway.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    private static final String DEMO_CLIENT_ID = "demo-client";
    private static final String DEMO_PASSWORD = "demo123";

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        log.info("Login attempt for clientId: {}", request.getClientId());

        if (!DEMO_CLIENT_ID.equals(request.getClientId())) {
            log.warn("Invalid clientId: {}", request.getClientId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .success(false)
                            .message("Invalid credentials")
                            .build());
        }

        if (!DEMO_PASSWORD.equals(request.getPassword())) {
            log.warn("Invalid password for clientId: {}", request.getClientId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .success(false)
                            .message("Invalid credentials")
                            .build());
        }

        String token = jwtTokenProvider.generateToken(
                request.getClientId(),
                new String[]{"notification:send", "quota:read"}
        );

        log.info("Token generated successfully for clientId: {}", request.getClientId());

        return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .token(token)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .message("Authentication successful")
                .build());
    }

    @PostMapping("/validate")
    public ResponseEntity<Object> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");

        String token = authHeader.substring(7);
        boolean isValid = jwtTokenProvider.validateToken(token);

        if (isValid) {
            String clientId = jwtTokenProvider.getClientIdFromToken(token);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "clientId", clientId
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
    }

}