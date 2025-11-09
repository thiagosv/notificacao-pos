package com.notification.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/authenticated")
    public Mono<ResponseEntity<Map<String, Object>>> testAuthenticated(
            @RequestHeader(value = "X-Client-Id", required = false) String clientId) {

        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> {
                    Authentication auth = securityContext.getAuthentication();

                    log.info("Authenticated user: {}", auth.getName());

                    return ResponseEntity.ok(Map.of(
                            "status", "authenticated",
                            "clientId", auth.getName(),
                            "clientIdFromHeader", clientId != null ? clientId : "not present",
                            "authorities", auth.getAuthorities(),
                            "timestamp", LocalDateTime.now()
                    ));
                })
                .defaultIfEmpty(ResponseEntity.status(401).body(Map.of(
                        "status", "unauthorized",
                        "message", "No authentication found"
                )));
    }

}