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

	@Bean
	public org.springframework.mail.javamail.JavaMailSender javaMailSender() {
		org.springframework.mail.javamail.JavaMailSenderImpl mailSender = new org.springframework.mail.javamail.JavaMailSenderImpl();
		mailSender.setHost("smtp.gmail.com");
		mailSender.setPort(587);
		mailSender.setUsername("khanazhar618190@gmail.com");
		mailSender.setPassword("ntjs eoqx qyqy jhbz");

		java.util.Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.debug", "false");

		return mailSender;
	}

}

