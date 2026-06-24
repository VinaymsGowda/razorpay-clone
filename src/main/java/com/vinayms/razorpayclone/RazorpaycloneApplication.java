package com.vinayms.razorpayclone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RazorpaycloneApplication {

	public static void main(String[] args) {
		System.out.println("DEBUG DB_USER = [" + System.getenv("DB_USER") + "]");
		SpringApplication.run(RazorpaycloneApplication.class, args);
	}

}
