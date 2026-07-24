package com.foodwaste.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodwaste.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{

User findByEmail(String email);

boolean existsByEmail(String email);

}