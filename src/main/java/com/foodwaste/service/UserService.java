package com.foodwaste.service;

import org.springframework.stereotype.Service;

import com.foodwaste.entity.User;
import com.foodwaste.repository.UserRepository;
import com.foodwaste.util.PasswordUtil;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

public String registerUser(User user) {

if(userRepository.existsByEmail(user.getEmail())) {
return "Email already exists";
}

user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
userRepository.save(user);

return "Registration Successful";
}

public User findByEmail(String email){

return userRepository.findByEmail(email);

}



public User loginUser(String email,String password) {

User user=userRepository.findByEmail(email);

if(user!=null && PasswordUtil.checkPassword(password, user.getPassword())) {
return user;
}

return null;

}


    public void updatePassword(String email, String newPassword){
        User user = userRepository.findByEmail(email);
        if(user != null){
            user.setPassword(PasswordUtil.hashPassword(newPassword));
            user.setFailedLoginAttempts(0);
            user.setLockoutLevel(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }

}