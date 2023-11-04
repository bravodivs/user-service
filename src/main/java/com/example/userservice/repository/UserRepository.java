package com.example.userservice.repository;

import com.example.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("SELECT u FROM User u WHERE u.username = :username")
    User findTopByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByMobileNumber(String mobileNumber);

    Boolean existsByEmail(String email);
}
