package com.park.ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ParkTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParkTicketApplication.class, args);
    }
}
