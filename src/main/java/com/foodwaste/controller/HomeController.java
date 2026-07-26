package com.foodwaste.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.foodwaste.entity.User;
import com.foodwaste.service.EmailService;
import com.foodwaste.service.UserService;

@Controller
public class HomeController {

@Autowired
UserService service;

@Autowired
private EmailService emailService;
@GetMapping("/test-mail")
@ResponseBody
public String testMail(){

emailService.sendMail("khanazhar618190@gmail.com");

return "Mail Sent Successfully";

}

@GetMapping("/")
public String home() {
return "login";
}

@GetMapping("/login")
public String login() {
return "login";
}
@PostMapping("/login")
public String loginUser(@RequestParam String email,
                        @RequestParam String password,
                        Model model){

User user = service.loginUser(email,password);

if(user==null){

model.addAttribute("error","Invalid Email or Password");

return "login";

}

else if(user.getRole().equals("Restaurant")){
return "redirect:/restaurant/dashboard";
}
else
return "redirect:/ngo/dashboard";

}
@GetMapping("/logout")
public String logout() {

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
model.addAttribute("error","Email not found");
return "forgot-password";
}

model.addAttribute("email", email);

return "reset-password";

} 
@GetMapping("/register")
public String register(Model model) {
model.addAttribute("user", new User());
return "register";
}

@PostMapping("/register")
public String saveUser(@ModelAttribute User user, Model model){

String msg = service.registerUser(user);

if(msg.equals("Email already exists")){
    model.addAttribute("error", msg);
    model.addAttribute("user", user);
    return "register";
}

return "redirect:/login";
}
@GetMapping("/restaurant/dashboard")
public String restaurantDashboard(){
return "restaurant/dashboard";
}

@GetMapping("/ngo/dashboard")
public String ngoDashboard(){
return "ngo/dashboard";
}

}