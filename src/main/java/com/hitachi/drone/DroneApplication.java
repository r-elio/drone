package com.hitachi.drone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DroneApplication {

    public static void main(String[] args) {
        SpringApplication.run(DroneApplication.class, args);
    }

}
