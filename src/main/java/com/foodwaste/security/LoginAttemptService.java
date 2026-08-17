package com.foodwaste.security;

import com.foodwaste.entity.User;
import com.foodwaste.repository.UserRepository;
import com.foodwaste.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);
    private static final int MAX_FAILED_ATTEMPTS = 3;

    private static final Map<Integer, Duration> LOCKOUT_DURATIONS = Map.of(
            1, Duration.ofMinutes(30),
            2, Duration.ofHours(1),
            3, Duration.ofHours(2),
            4, Duration.ofHours(4),
            5, Duration.ofHours(8)
    );

    private final UserRepository userRepository;

    public LoginAttemptService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResult attemptLogin(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            return LoginResult.invalidCredentials(0);
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            return LoginResult.invalidCredentials(0);
        }

        if (user.isBlocked()) {
            return LoginResult.blocked();
        }

        LocalDateTime now = LocalDateTime.now();

        // Check if currently locked
        if (user.getLockedUntil() != null) {
            if (user.getLockedUntil().isAfter(now)) {
                log.warn("Login attempt on locked account {} (locked until {})", email, user.getLockedUntil());
                return LoginResult.locked(user.getLockedUntil(), user.getLockoutLevel());
            } else {
                // Lock expired naturally -> clear lockedUntil but preserve lockoutLevel for escalation
                user.setLockedUntil(null);
                userRepository.save(user);
            }
        }

        boolean passwordMatches = PasswordUtil.checkPassword(rawPassword, user.getPassword());

        if (passwordMatches) {
            // Successful login -> full reset of attempts and lockout level
            user.setFailedLoginAttempts(0);
            user.setLockoutLevel(0);
            user.setLockedUntil(null);
            userRepository.save(user);
            log.info("Successful login for user {}", email);
            return LoginResult.success(user);
        } else {
            // Failed attempt
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                int nextLevel = Math.min(user.getLockoutLevel() + 1, 5);
                Duration lockoutDuration = LOCKOUT_DURATIONS.getOrDefault(nextLevel, Duration.ofMinutes(30));
                LocalDateTime lockExpiration = now.plus(lockoutDuration);

                user.setLockoutLevel(nextLevel);
                user.setLockedUntil(lockExpiration);
                user.setFailedLoginAttempts(0);
                userRepository.save(user);

                log.warn("Account {} locked at level {} until {}", email, nextLevel, lockExpiration);
                return LoginResult.locked(lockExpiration, nextLevel);
            } else {
                userRepository.save(user);
                int remaining = MAX_FAILED_ATTEMPTS - attempts;
                log.info("Failed login for user {} ({} attempts remaining)", email, remaining);
                return LoginResult.invalidCredentials(remaining);
            }
        }
    }

    public void resetLockout(User user) {
        if (user != null) {
            user.setFailedLoginAttempts(0);
            user.setLockoutLevel(0);
            user.setLockedUntil(null);
            userRepository.save(user);
            log.info("Lockout reset for user {}", user.getEmail());
        }
    }

    public boolean unlockUser(Long userId) {
        if (userId == null) {
            return false;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            resetLockout(user);
            return true;
        }
        return false;
    }

    public List<User> getLockedUsers() {
        return userRepository.findByLockedUntilAfter(LocalDateTime.now());
    }

    public static class LoginResult {
        public enum Status {
            SUCCESS, LOCKED, BLOCKED, INVALID_CREDENTIALS
        }

        private final Status status;
        private final User user;
        private final LocalDateTime lockedUntil;
        private final int lockoutLevel;
        private final int remainingAttempts;

        private LoginResult(Status status, User user, LocalDateTime lockedUntil, int lockoutLevel, int remainingAttempts) {
            this.status = status;
            this.user = user;
            this.lockedUntil = lockedUntil;
            this.lockoutLevel = lockoutLevel;
            this.remainingAttempts = remainingAttempts;
        }

        public static LoginResult success(User user) {
            return new LoginResult(Status.SUCCESS, user, null, 0, 0);
        }

        public static LoginResult locked(LocalDateTime lockedUntil, int lockoutLevel) {
            return new LoginResult(Status.LOCKED, null, lockedUntil, lockoutLevel, 0);
        }

        public static LoginResult blocked() {
            return new LoginResult(Status.BLOCKED, null, null, 0, 0);
        }

        public static LoginResult invalidCredentials(int remainingAttempts) {
            return new LoginResult(Status.INVALID_CREDENTIALS, null, null, 0, remainingAttempts);
        }

        public Status getStatus() {
            return status;
        }

        public User getUser() {
            return user;
        }

        public LocalDateTime getLockedUntil() {
            return lockedUntil;
        }

        public int getLockoutLevel() {
            return lockoutLevel;
        }

        public int getRemainingAttempts() {
            return remainingAttempts;
        }
    }
}
