package com.vinayms.razorpayclone.vault.repository;

import com.vinayms.razorpayclone.vault.entity.VaultCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VaultCardRepository extends JpaRepository<VaultCard, UUID> {
}