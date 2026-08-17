package com.foodwaste.security;

import com.foodwaste.entity.User;
import com.foodwaste.repository.UserRepository;
import com.foodwaste.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    private User sampleUser;
    private String rawPassword;

    @BeforeEach
    void setUp() {
        rawPassword = "CorrectPassword123!";
        sampleUser = new User("John", "john@example.com", PasswordUtil.hashPassword(rawPassword), "Restaurant");
        sampleUser.setId(1L);
    }

    @Test
    void testAttemptLogin_NullInputs() {
        LoginAttemptService.LoginResult res1 = loginAttemptService.attemptLogin(null, "pass");
        assertEquals(LoginAttemptService.LoginResult.Status.INVALID_CREDENTIALS, res1.getStatus());

        LoginAttemptService.LoginResult res2 = loginAttemptService.attemptLogin("email", null);
        assertEquals(LoginAttemptService.LoginResult.Status.INVALID_CREDENTIALS, res2.getStatus());
    }

    @Test
    void testAttemptLogin_UserNotFound() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(null);

        LoginAttemptService.LoginResult result = loginAttemptService.attemptLogin("none@test.com", "pass");

        assertEquals(LoginAttemptService.LoginResult.Status.INVALID_CREDENTIALS, result.getStatus());
    }

    @Test
    void testAttemptLogin_BlockedUser() {
        sampleUser.setBlocked(true);
        when(userRepository.findByEmail("john@example.com")).thenReturn(sampleUser);

        LoginAttemptService.LoginResult result = loginAttemptService.attemptLogin("john@example.com", "any");

        assertEquals(LoginAttemptService.LoginResult.Status.BLOCKED, result.getStatus());
    }

    @Test
    void testAttemptLogin_Success_ResetsLockout() {
        sampleUser.setFailedLoginAttempts(2);
        sampleUser.setLockoutLevel(1);
        when(userRepository.findByEmail("john@example.com")).thenReturn(sampleUser);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        LoginAttemptService.LoginResult result = loginAttemptService.attemptLogin("john@example.com", rawPassword);

        assertEquals(LoginAttemptService.LoginResult.Status.SUCCESS, result.getStatus());
        assertNotNull(result.getUser());
        assertEquals(0, sampleUser.getFailedLoginAttempts());
        assertEquals(0, sampleUser.getLockoutLevel());
        assertNull(sampleUser.getLockedUntil());
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    void testAttemptLogin_FailedAttempts_EscalatesToLockout() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(sampleUser);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        // 1st failed attempt
        LoginAttemptService.LoginResult res1 = loginAttemptService.attemptLogin("john@example.com", "wrong1");
        assertEquals(LoginAttemptService.LoginResult.Status.INVALID_CREDENTIALS, res1.getStatus());
        assertEquals(2, res1.getRemainingAttempts());
        assertEquals(1, sampleUser.getFailedLoginAttempts());

        // 2nd failed attempt
        LoginAttemptService.LoginResult res2 = loginAttemptService.attemptLogin("john@example.com", "wrong2");
        assertEquals(LoginAttemptService.LoginResult.Status.INVALID_CREDENTIALS, res2.getStatus());
        assertEquals(1, res2.getRemainingAttempts());
        assertEquals(2, sampleUser.getFailedLoginAttempts());

        // 3rd failed attempt -> Level 1 lockout (30 min)
        LoginAttemptService.LoginResult res3 = loginAttemptService.attemptLogin("john@example.com", "wrong3");
        assertEquals(LoginAttemptService.LoginResult.Status.LOCKED, res3.getStatus());
        assertEquals(1, res3.getLockoutLevel());
        assertNotNull(res3.getLockedUntil());
        assertEquals(1, sampleUser.getLockoutLevel());
        assertEquals(0, sampleUser.getFailedLoginAttempts());
        assertTrue(sampleUser.isCurrentlyLocked());
    }

    @Test
    void testAttemptLogin_LockedAccount_RejectsAttempt() {
        sampleUser.setLockedUntil(LocalDateTime.now().plusMinutes(25));
        sampleUser.setLockoutLevel(1);
        when(userRepository.findByEmail("john@example.com")).thenReturn(sampleUser);

        LoginAttemptService.LoginResult result = loginAttemptService.attemptLogin("john@example.com", rawPassword);

        assertEquals(LoginAttemptService.LoginResult.Status.LOCKED, result.getStatus());
        assertEquals(1, result.getLockoutLevel());
    }

    @Test
    void testAttemptLogin_NaturallyExpiredLock_PreservesLevelAndEscalatesOnNextFailure() {
        sampleUser.setLockedUntil(LocalDateTime.now().minusMinutes(5)); // expired in past
        sampleUser.setLockoutLevel(1);
        when(userRepository.findByEmail("john@example.com")).thenReturn(sampleUser);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        // 3 wrong passwords after natural expiry -> escalates to Level 2 (1 hour)
        loginAttemptService.attemptLogin("john@example.com", "wrong1");
        loginAttemptService.attemptLogin("john@example.com", "wrong2");
        LoginAttemptService.LoginResult res = loginAttemptService.attemptLogin("john@example.com", "wrong3");

        assertEquals(LoginAttemptService.LoginResult.Status.LOCKED, res.getStatus());
        assertEquals(2, res.getLockoutLevel());
        assertEquals(2, sampleUser.getLockoutLevel());
    }

    @Test
    void testAdminUnlockUser() {
        sampleUser.setLockedUntil(LocalDateTime.now().plusHours(2));
        sampleUser.setLockoutLevel(3);
        sampleUser.setFailedLoginAttempts(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        boolean unlocked = loginAttemptService.unlockUser(1L);

        assertTrue(unlocked);
        assertEquals(0, sampleUser.getLockoutLevel());
        assertEquals(0, sampleUser.getFailedLoginAttempts());
        assertNull(sampleUser.getLockedUntil());
    }

    @Test
    void testAdminUnlockUser_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertFalse(loginAttemptService.unlockUser(99L));
        assertFalse(loginAttemptService.unlockUser(null));
    }

    @Test
    void testGetLockedUsers() {
        when(userRepository.findByLockedUntilAfter(any())).thenReturn(List.of(sampleUser));

        List<User> locked = loginAttemptService.getLockedUsers();

        assertEquals(1, locked.size());
    }
}
