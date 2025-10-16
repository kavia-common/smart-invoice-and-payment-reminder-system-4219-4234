package com.example.invoicebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot entry point with scheduling enabled.
 */
@SpringBootApplication
@EnableScheduling
public class invoicebackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(invoicebackendApplication.class, args);
	}

}
