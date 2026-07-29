package com.foodwaste.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.foodwaste.entity.User;
import com.foodwaste.service.EmailService;
import com.foodwaste.service.UserService;

import java.util.HashMap;
import java.util.Random;

@Controller
public class HomeController {

@Autowired
UserService service;

@Autowired
EmailService emailService;

// storing otp temporarily in memory
private HashMap<String, String> otpStorage = new HashMap<>();



@GetMapping("/")
public String home() {
return "login";
}



@GetMapping("/login")
public String login() {
return "login";
}




@PostMapping("/login")
public String loginUser(@RequestParam String email,@RequestParam String password,Model model){
	
User user=service.loginUser(email,password);

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




@GetMapping("/test-mail")
@ResponseBody
public String testMail(){

emailService.sendMail("khanazhar618190@gmail.com");

return "Mail Sent Successfully";

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

// generate 6 digit otp
Random random = new Random();
String otp = String.valueOf(100000 + random.nextInt(900000));

// store otp against email
otpStorage.put(email, otp);

// send otp to email
emailService.sendOtp(email, otp);

model.addAttribute("email", email);
model.addAttribute("success", "OTP sent to your email");

return "verify-otp";

} 


@PostMapping("/verify-otp")
public String verifyOtp(@RequestParam String email, @RequestParam String otp, Model model){

String storedOtp = otpStorage.get(email);

if(storedOtp == null || !storedOtp.equals(otp)){
model.addAttribute("error", "Invalid OTP");
model.addAttribute("email", email);
return "verify-otp";
}

// otp verified, remove it
otpStorage.remove(email);

model.addAttribute("email", email);
return "reset-password";

}


@PostMapping("/reset-password")
public String updatePassword(@RequestParam String email, @RequestParam String password, @RequestParam String confirmPassword, Model model){

if(!password.equals(confirmPassword)){
model.addAttribute("error", "Passwords do not match");
model.addAttribute("email", email);
return "reset-password";
}

service.updatePassword(email, password);

model.addAttribute("success", "Password updated successfully");
return "login";

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