package com.example.userservice.repository;

import com.example.userservice.model.DeletedUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeletedUserRepository extends JpaRepository<DeletedUser, UUID> {
}
