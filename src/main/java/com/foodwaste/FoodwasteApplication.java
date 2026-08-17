package com.foodwaste;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import com.foodwaste.entity.User;
import com.foodwaste.repository.UserRepository;
import com.foodwaste.util.PasswordUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
@EnableAsync
public class FoodwasteApplication {

	private static final Logger log = LoggerFactory.getLogger(FoodwasteApplication.class);

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
						+ "blocked BOOLEAN DEFAULT FALSE, "
						+ "failed_login_attempts INT DEFAULT 0, "
						+ "lockout_level INT DEFAULT 0, "
						+ "locked_until DATETIME DEFAULT NULL"
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

				// Safely ensure columns exist if users table was created previously
				tryAddColumn(jdbcTemplate, "ALTER TABLE users ADD COLUMN failed_login_attempts INT DEFAULT 0");
				tryAddColumn(jdbcTemplate, "ALTER TABLE users ADD COLUMN lockout_level INT DEFAULT 0");
				tryAddColumn(jdbcTemplate, "ALTER TABLE users ADD COLUMN locked_until DATETIME DEFAULT NULL");
			} catch (Exception e) {
				log.warn("Notice during table verification: {}", e.getMessage());
			}

			try {
				User admin = userRepo.findByEmail("admin@gmail.com");
				if (admin == null) {
					admin = new User("Admin", "admin@gmail.com", PasswordUtil.hashPassword("admin123"), "Admin");
					userRepo.save(admin);
				} else if (admin.getPassword() == null || (!admin.getPassword().startsWith("$2a$") && !admin.getPassword().startsWith("$2b$"))) {
					admin.setPassword(PasswordUtil.hashPassword("admin123"));
					admin.setFailedLoginAttempts(0);
					admin.setLockoutLevel(0);
					admin.setLockedUntil(null);
					userRepo.save(admin);
				}
			} catch (Exception e) {
				log.warn("Notice during admin user initialization: {}", e.getMessage());
			}
		};
	}

	@Bean
	public org.springframework.mail.javamail.JavaMailSender javaMailSender(
			@Value("${spring.mail.host:smtp.gmail.com}") String host,
			@Value("${spring.mail.port:587}") int port,
			@Value("${spring.mail.username:}") String username,
			@Value("${spring.mail.password:}") String password) {
		org.springframework.mail.javamail.JavaMailSenderImpl mailSender = new org.springframework.mail.javamail.JavaMailSenderImpl();
		mailSender.setHost(host);
		mailSender.setPort(port);
		mailSender.setUsername(username);
		mailSender.setPassword(password);

		java.util.Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.debug", "false");

		return mailSender;
	}

	private void tryAddColumn(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate, String sql) {
		try {
			jdbcTemplate.execute(sql);
		} catch (Exception ignored) {
			// Column already exists or table structure matches
		}
	}

}

