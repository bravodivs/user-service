package com.example.userservice.repository;

import com.example.userservice.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddressesRepository extends JpaRepository<Address, UUID> {
}
