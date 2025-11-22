package com.notification.provider.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableKafka
@EnableRetry
public class ProviderSmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderSmsApplication.class, args);
    }
}

