package com.foodwaste.controller;

import com.foodwaste.entity.User;
import com.foodwaste.service.EmailService;
import com.foodwaste.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private HomeController homeController;

    private MockHttpSession session;
    private Model model;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        model = new ConcurrentModel();
    }

    @Test
    void testHome() {
        assertEquals("landing", homeController.home());
    }

    @Test
    void testLoginView() {
        assertEquals("login", homeController.login());
    }

    @Test
    void testLoginUser_NullUser() {
        when(userService.loginUser("bad@test.com", "wrong")).thenReturn(null);

        String view = homeController.loginUser("bad@test.com", "wrong", model, session);

        assertEquals("login", view);
        assertEquals("Invalid Email or Password", model.getAttribute("error"));
    }

    @Test
    void testLoginUser_BlockedUser() {
        User blocked = new User("Blocked", "blocked@test.com", "pass", "Restaurant");
        blocked.setBlocked(true);
        when(userService.loginUser("blocked@test.com", "pass")).thenReturn(blocked);

        String view = homeController.loginUser("blocked@test.com", "pass", model, session);

        assertEquals("login", view);
        assertEquals("Your account has been blocked. Contact admin.", model.getAttribute("error"));
    }

    @Test
    void testLoginUser_RestaurantSuccess() {
        User user = new User("Rest", "rest@test.com", "pass", "Restaurant");
        user.setId(10L);
        when(userService.loginUser("rest@test.com", "pass")).thenReturn(user);

        String view = homeController.loginUser("rest@test.com", "pass", model, session);

        assertEquals("redirect:/restaurant/dashboard", view);
        assertEquals(10L, session.getAttribute("userId"));
        assertEquals("Rest", session.getAttribute("userName"));
    }

    @Test
    void testLoginUser_AdminSuccess() {
        User user = new User("Admin", "admin@test.com", "pass", "Admin");
        user.setId(1L);
        when(userService.loginUser("admin@test.com", "pass")).thenReturn(user);

        String view = homeController.loginUser("admin@test.com", "pass", model, session);

        assertEquals("redirect:/admin/dashboard", view);
    }

    @Test
    void testLoginUser_NgoSuccess() {
        User user = new User("NGO", "ngo@test.com", "pass", "NGO");
        user.setId(20L);
        when(userService.loginUser("ngo@test.com", "pass")).thenReturn(user);

        String view = homeController.loginUser("ngo@test.com", "pass", model, session);

        assertEquals("redirect:/ngo/dashboard", view);
    }

    @Test
    void testRegisterView() {
        String view = homeController.register(model);
        assertEquals("register", view);
        assertNotNull(model.getAttribute("user"));
    }

    @Test
    void testSaveUser_EmailExists() {
        User user = new User("Name", "exists@test.com", "pass", "NGO");
        when(userService.registerUser(user)).thenReturn("Email already exists");

        String view = homeController.saveUser(user, model);

        assertEquals("register", view);
        assertEquals("Email already exists", model.getAttribute("error"));
    }

    @Test
    void testSaveUser_Success() {
        User user = new User("Name", "new@test.com", "pass", "NGO");
        when(userService.registerUser(user)).thenReturn("Registration Successful");

        String view = homeController.saveUser(user, model);

        assertEquals("redirect:/login", view);
    }

    @Test
    void testLogout() {
        session.setAttribute("userId", 1L);
        String view = homeController.logout(session);

        assertEquals("redirect:/login", view);
        assertTrue(session.isInvalid());
    }

    @Test
    void testForgotPasswordView() {
        assertEquals("forgot-password", homeController.forgotPassword());
    }

    @Test
    void testResetPassword_UserNotFound() {
        when(userService.findByEmail("none@test.com")).thenReturn(null);

        String view = homeController.resetPassword("none@test.com", model);

        assertEquals("forgot-password", view);
        assertEquals("Email not found", model.getAttribute("error"));
    }

    @Test
    void testResetPassword_Success() {
        User user = new User("Test", "user@test.com", "pass", "NGO");
        when(userService.findByEmail("user@test.com")).thenReturn(user);
        doNothing().when(emailService).sendOtp(eq("user@test.com"), anyString());

        String view = homeController.resetPassword("user@test.com", model);

        assertEquals("verify-otp", view);
        assertEquals("user@test.com", model.getAttribute("email"));
        verify(emailService, times(1)).sendOtp(eq("user@test.com"), anyString());
    }

    @Test
    void testVerifyOtp_InvalidOrMissing() {
        String view = homeController.verifyOtp("user@test.com", "999999", model);

        assertEquals("verify-otp", view);
        assertEquals("Invalid OTP", model.getAttribute("error"));
    }

    @Test
    void testUpdatePassword_Mismatch() {
        String view = homeController.updatePassword("user@test.com", "pass1", "pass2", model);

        assertEquals("reset-password", view);
        assertEquals("Passwords do not match", model.getAttribute("error"));
    }

    @Test
    void testUpdatePassword_Success() {
        doNothing().when(userService).updatePassword("user@test.com", "newPass");

        String view = homeController.updatePassword("user@test.com", "newPass", "newPass", model);

        assertEquals("login", view);
        assertEquals("Password updated successfully", model.getAttribute("success"));
        verify(userService, times(1)).updatePassword("user@test.com", "newPass");
    }
}
