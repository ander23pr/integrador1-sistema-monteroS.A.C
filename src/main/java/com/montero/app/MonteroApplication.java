package com.montero.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MonteroApplication {
    public static void main(String[] args) {
        SpringApplication.run(MonteroApplication.class, args);
    }
}