package com.foodwaste.service;

import com.foodwaste.entity.User;
import com.foodwaste.repository.UserRepository;
import com.foodwaste.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setName("Test Restaurant");
        sampleUser.setEmail("test@restaurant.com");
        sampleUser.setPassword("plainPassword123");
        sampleUser.setRole("Restaurant");
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.existsByEmail("test@restaurant.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        String result = userService.registerUser(sampleUser);

        assertEquals("Registration Successful", result);
        assertTrue(sampleUser.getPassword().startsWith("$2a$") || sampleUser.getPassword().startsWith("$2b$"));
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        when(userRepository.existsByEmail("test@restaurant.com")).thenReturn(true);

        String result = userService.registerUser(sampleUser);

        assertEquals("Email already exists", result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginUser_Success() {
        String hashedPassword = PasswordUtil.hashPassword("plainPassword123");
        sampleUser.setPassword(hashedPassword);

        when(userRepository.findByEmail("test@restaurant.com")).thenReturn(sampleUser);

        User loggedInUser = userService.loginUser("test@restaurant.com", "plainPassword123");

        assertNotNull(loggedInUser);
        assertEquals("test@restaurant.com", loggedInUser.getEmail());
    }

    @Test
    void testLoginUser_InvalidPassword() {
        String hashedPassword = PasswordUtil.hashPassword("plainPassword123");
        sampleUser.setPassword(hashedPassword);

        when(userRepository.findByEmail("test@restaurant.com")).thenReturn(sampleUser);

        User loggedInUser = userService.loginUser("test@restaurant.com", "wrongPassword");

        assertNull(loggedInUser);
    }

    @Test
    void testLoginUser_UserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

        User loggedInUser = userService.loginUser("nonexistent@example.com", "plainPassword123");

        assertNull(loggedInUser);
    }

    @Test
    void testUpdatePassword_Success() {
        when(userRepository.findByEmail("test@restaurant.com")).thenReturn(sampleUser);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        userService.updatePassword("test@restaurant.com", "newSecretPassword");

        assertTrue(PasswordUtil.checkPassword("newSecretPassword", sampleUser.getPassword()));
        verify(userRepository, times(1)).save(sampleUser);
    }
}
