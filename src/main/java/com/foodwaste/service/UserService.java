package com.foodwaste.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.foodwaste.entity.User;
import com.foodwaste.repository.UserRepository;

@Service
public class UserService {

@Autowired
private UserRepository userRepository;

public String registerUser(User user) {

if(userRepository.existsByEmail(user.getEmail())) {
return "Email already exists";
}

userRepository.save(user);

return "Registration Successful";
}

public User findByEmail(String email){

return userRepository.findByEmail(email);

}



public User loginUser(String email,String password) {

User user=userRepository.findByEmail(email);

if(user!=null && user.getPassword().equals(password)) {
return user;
}

return null;

}


public void updatePassword(String email, String newPassword){

User user = userRepository.findByEmail(email);

if(user != null){
user.setPassword(newPassword);
userRepository.save(user);
}

}

}