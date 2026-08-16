package com.foodwaste.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.foodwaste.entity.User;
import com.foodwaste.service.EmailService;
import com.foodwaste.service.UserService;

import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class HomeController {

    private static final String VIEW_LOGIN = "login";
    private static final String VIEW_REGISTER = "register";
    private static final String VIEW_VERIFY_OTP = "verify-otp";
    private static final String VIEW_RESET_PASSWORD = "reset-password";
    private static final String ATTR_ERROR = "error";
    private static final String ATTR_EMAIL = "email";
    private static final String ATTR_SUCCESS = "success";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserService service;
    private final EmailService emailService;
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public HomeController(UserService service, EmailService emailService) {
        this.service = service;
        this.emailService = emailService;
    }

    @GetMapping("/")
    public String home() {
        return "landing";
    }

    @GetMapping("/login")
    public String login() {
        return VIEW_LOGIN;
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email, @RequestParam String password, Model model, HttpSession session){

        User user = service.loginUser(email, password);

        if(user == null){
            model.addAttribute(ATTR_ERROR, "Invalid Email or Password");
            return VIEW_LOGIN;
        }

        if(user.isBlocked()){
            model.addAttribute(ATTR_ERROR, "Your account has been blocked. Contact admin.");
            return VIEW_LOGIN;
        }

        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getName());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userRole", user.getRole());

        if("Restaurant".equals(user.getRole())){
            return "redirect:/restaurant/dashboard";
        } else if("Admin".equals(user.getRole())){
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/ngo/dashboard";
        }

    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return VIEW_REGISTER;
    }

    @PostMapping("/register")
    public String saveUser(@ModelAttribute User user, Model model){

        String msg = service.registerUser(user);

        if("Email already exists".equals(msg)){
            model.addAttribute(ATTR_ERROR, msg);
            model.addAttribute("user", user);
            return VIEW_REGISTER;
        }
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String resetPassword(@RequestParam String email, Model model) {

        User user = service.findByEmail(email);

        if(user == null) {
            model.addAttribute(ATTR_ERROR, "Email not found");
            return "forgot-password";
        }

        String otp = String.valueOf(100000 + SECURE_RANDOM.nextInt(900000));

        otpStorage.put(email, otp);

        emailService.sendOtp(email, otp);

        model.addAttribute(ATTR_EMAIL, email);
        model.addAttribute(ATTR_SUCCESS, "OTP sent to your email");

        return VIEW_VERIFY_OTP;

    } 

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email, @RequestParam String otp, Model model){

        String storedOtp = otpStorage.get(email);

        if(storedOtp == null || !storedOtp.equals(otp)){
            model.addAttribute(ATTR_ERROR, "Invalid OTP");
            model.addAttribute(ATTR_EMAIL, email);
            return VIEW_VERIFY_OTP;
        }

        otpStorage.remove(email);

        model.addAttribute(ATTR_EMAIL, email);
        return VIEW_RESET_PASSWORD;

    }

    @PostMapping("/reset-password")
    public String updatePassword(@RequestParam String email, @RequestParam String password, @RequestParam String confirmPassword, Model model){

        if(!password.equals(confirmPassword)){
            model.addAttribute(ATTR_ERROR, "Passwords do not match");
            model.addAttribute(ATTR_EMAIL, email);
            return VIEW_RESET_PASSWORD;
        }

        service.updatePassword(email, password);

        model.addAttribute(ATTR_SUCCESS, "Password updated successfully");
        return VIEW_LOGIN;

    }

}