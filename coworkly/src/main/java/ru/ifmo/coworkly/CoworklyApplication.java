package ru.ifmo.coworkly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoworklyApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoworklyApplication.class, args);
    }
}
