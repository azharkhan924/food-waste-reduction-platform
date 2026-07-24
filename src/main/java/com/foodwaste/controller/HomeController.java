package com.foodwaste.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.foodwaste.entity.User;
import com.foodwaste.service.UserService;

@Controller
public class HomeController {

@Autowired
UserService service;

@GetMapping("/")
public String home() {
return "login";
}

@GetMapping("/login")
public String login() {
return "login";
}

@GetMapping("/register")
public String register(Model model) {
model.addAttribute("user", new User());
return "register";
}

@PostMapping("/register")
public String saveUser(@ModelAttribute User user) {

service.saveUser(user);

return "redirect:/login";
}

}