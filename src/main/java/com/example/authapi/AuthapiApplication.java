package com.example.authapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableAsync
public class AuthapiApplication {

	public static void main(String[] args) {
		// Force IPv4 to fix Render/Gmail timeout issues
		System.setProperty("java.net.preferIPv4Stack", "true");
		SpringApplication.run(AuthapiApplication.class, args);
	}

}
