package com.vinayms.razorpayclone.merchant.repository;

import com.vinayms.razorpayclone.merchant.entity.Merchant;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {


    boolean existsByEmail(String email);
}