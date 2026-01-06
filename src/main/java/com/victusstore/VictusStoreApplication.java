package com.victusstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VictusStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(VictusStoreApplication.class, args);
    }
}
