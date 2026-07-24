package com.foodwaste.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.foodwaste.entity.User;
import com.foodwaste.repository.UserRepository;

@Service
public class UserService {

@Autowired
UserRepository repo;

public void saveUser(User user) {
repo.save(user);
}

}