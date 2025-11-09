package com.notification.quota;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@EnableCaching
@EnableJpaAuditing
public class QuotaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuotaServiceApplication.class, args);
    }

}