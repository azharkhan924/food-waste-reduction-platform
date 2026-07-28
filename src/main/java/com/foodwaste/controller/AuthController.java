package com.foodwaste.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.foodwaste.entity.User;
import com.foodwaste.service.UserService;

public class AuthController {

	@Autowired
	UserService service;

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




}
