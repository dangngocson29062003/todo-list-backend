package com.example.weaver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WeaverApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeaverApplication.class, args);
	}

}
