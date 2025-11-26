package com.notification.template.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TemplateRenderServiceTest {

    @Autowired
    private TemplateRenderService renderService;

    @Test
    void shouldRenderTemplateSuccessfully() {
        // Given
        String template = "Olá {{name}}, bem-vindo ao sistema!";
        Map<String, String> variables = new HashMap<>();
        variables.put("name", "João Silva");

        // When
        String result = renderService.render(template, variables);

        // Then
        assertEquals("Olá João Silva, bem-vindo ao sistema!", result);
    }

    @Test
    void shouldRenderTemplateWithMultipleVariables() {
        // Given
        String template = "Pedido #{{orderId}} confirmado! Total: R$ {{amount}}";
        Map<String, String> variables = new HashMap<>();
        variables.put("orderId", "12345");
        variables.put("amount", "150.00");

        // When
        String result = renderService.render(template, variables);

        // Then
        assertEquals("Pedido #12345 confirmado! Total: R$ 150.00", result);
    }

    @Test
    void shouldHandleMissingVariableByLeavingEmpty() {
        // Given
        String template = "Olá {{name}}, seu pedido {{orderId}} foi processado.";
        Map<String, String> variables = new HashMap<>();
        variables.put("name", "João");
        // orderId is missing intentionally

        // When
        String result = renderService.render(template, variables);

        // Then - Mustache leaves empty when variable is missing
        assertEquals("Olá João, seu pedido  foi processado.", result);
    }

    @Test
    void shouldRenderEmptyTemplate() {
        // Given
        String template = "";
        Map<String, String> variables = new HashMap<>();

        // When
        String result = renderService.render(template, variables);

        // Then
        assertEquals("", result);
    }

    @Test
    void shouldRenderTemplateWithoutVariables() {
        // Given
        String template = "Bem-vindo ao sistema!";
        Map<String, String> variables = new HashMap<>();

        // When
        String result = renderService.render(template, variables);

        // Then
        assertEquals("Bem-vindo ao sistema!", result);
    }
}

