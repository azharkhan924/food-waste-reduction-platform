package com.foodwaste.controller;

import com.foodwaste.entity.User;
import com.foodwaste.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private Model model;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
    }

    @Test
    void testLoginView() {
        assertEquals("login", authController.login());
    }

    @Test
    void testLoginUser_Invalid() {
        when(userService.loginUser("bad@test.com", "wrong")).thenReturn(null);

        String view = authController.loginUser("bad@test.com", "wrong", model);

        assertEquals("login", view);
        assertEquals("Invalid Email or Password", model.getAttribute("error"));
    }

    @Test
    void testLoginUser_Restaurant() {
        User user = new User("Rest", "r@test.com", "pass", "Restaurant");
        when(userService.loginUser("r@test.com", "pass")).thenReturn(user);

        String view = authController.loginUser("r@test.com", "pass", model);

        assertEquals("redirect:/restaurant/dashboard", view);
    }

    @Test
    void testLoginUser_Ngo() {
        User user = new User("NGO", "n@test.com", "pass", "NGO");
        when(userService.loginUser("n@test.com", "pass")).thenReturn(user);

        String view = authController.loginUser("n@test.com", "pass", model);

        assertEquals("redirect:/ngo/dashboard", view);
    }

    @Test
    void testRegisterView() {
        String view = authController.register(model);
        assertEquals("register", view);
        assertNotNull(model.getAttribute("user"));
    }

    @Test
    void testSaveUser_EmailExists() {
        User user = new User("Name", "exists@test.com", "pass", "NGO");
        when(userService.registerUser(user)).thenReturn("Email already exists");

        String view = authController.saveUser(user, model);

        assertEquals("register", view);
        assertEquals("Email already exists", model.getAttribute("error"));
    }

    @Test
    void testSaveUser_Success() {
        User user = new User("Name", "new@test.com", "pass", "NGO");
        when(userService.registerUser(user)).thenReturn("Registration Successful");

        String view = authController.saveUser(user, model);

        assertEquals("redirect:/login", view);
    }
}
