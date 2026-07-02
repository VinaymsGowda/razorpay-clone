package com.vinayms.razorpayclone.merchant.repository;

import com.vinayms.razorpayclone.merchant.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    boolean existsByEmail(String email);
}