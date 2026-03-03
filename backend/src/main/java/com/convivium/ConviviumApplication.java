package com.convivium;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ConviviumApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConviviumApplication.class, args);
    }
}
