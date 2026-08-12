package com.foodwaste;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import com.foodwaste.entity.User;
import com.foodwaste.repository.UserRepository;
import com.foodwaste.util.PasswordUtil;

@SpringBootApplication
@EnableAsync
public class FoodwasteApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodwasteApplication.class, args);
	}

	@Bean
	public CommandLineRunner initDatabase(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate, UserRepository userRepo) {
		return args -> {
			try {
				jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users ("
						+ "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
						+ "name VARCHAR(255), "
						+ "email VARCHAR(255) NOT NULL UNIQUE, "
						+ "password VARCHAR(255) NOT NULL, "
						+ "role VARCHAR(255) NOT NULL, "
						+ "blocked BOOLEAN DEFAULT FALSE"
						+ ")");
				jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS donations ("
						+ "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
						+ "food_name VARCHAR(255), "
						+ "qty INT, "
						+ "expiry DATE, "
						+ "pickup_address VARCHAR(555), "
						+ "status VARCHAR(255), "
						+ "restaurant_id BIGINT, "
						+ "restaurant_name VARCHAR(255), "
						+ "ngo_id BIGINT, "
						+ "ngo_name VARCHAR(255), "
						+ "created_at DATETIME, "
						+ "latitude DOUBLE, "
						+ "longitude DOUBLE"
						+ ")");
			} catch (Exception e) {
			}

			try {
				if (!userRepo.existsByEmail("admin@gmail.com")) {
					User admin = new User("Admin", "admin@gmail.com", PasswordUtil.hashPassword("admin123"), "Admin");
					userRepo.save(admin);
				}
			} catch (Exception e) {
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

