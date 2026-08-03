package com.foodwaste;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.foodwaste.entity.User;
import com.foodwaste.repository.UserRepository;

@SpringBootApplication
public class FoodwasteApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodwasteApplication.class, args);
	}

	@Bean
	public CommandLineRunner initAdmin(UserRepository userRepo) {
		return args -> {
			if (!userRepo.existsByEmail("admin@gmail.com")) {
				User admin = new User("Admin", "admin@gmail.com", "admin123", "Admin");
				userRepo.save(admin);
			}
		};
	}

}

