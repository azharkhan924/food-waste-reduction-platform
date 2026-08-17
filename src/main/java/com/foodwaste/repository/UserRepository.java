package com.foodwaste.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodwaste.entity.User;
import java.time.LocalDateTime;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(String role);

    long countByRole(String role);

    long countByBlocked(boolean blocked);

    List<User> findByLockedUntilAfter(LocalDateTime time);

    long countByLockedUntilAfter(LocalDateTime time);

}