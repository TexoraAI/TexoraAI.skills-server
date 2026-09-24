package com.lms.live_session;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;
@SpringBootApplication
@EnableScheduling 
@EnableAsync
public class LiveSessionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiveSessionServiceApplication.class, args);
	}

}
