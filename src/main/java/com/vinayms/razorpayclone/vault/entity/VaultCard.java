package com.vinayms.razorpayclone.vault.entity;

import jakarta.persistence.*;
import lombok.*;
import com.vinayms.razorpayclone.common.entity.BaseAuditableEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vault_card")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaultCard extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Lob
    @Column(name = "encrypted_pan", nullable = false)
    private byte[] encryptedPan;  // dek encrypts the pan that is card number

    @Lob
    @Column(name = "encrypted_dek", nullable = false)
    private byte[] encryptedDek;  // dek is encrypted via our env variable.

    @Column(nullable = false,length = 4)
    private String lastFourDigits;

    @Column(name = "brand",nullable = false)
    private String brand;

    @Column(name = "bin",nullable = false,length = 6)
    private String bin;  // 1st 6 digits of a card

    @Column(name = "exp_month")
    private Integer expMonth;

    @Column(name = "exp_year",nullable = false)
    private Integer expYear;

    @Column(nullable = false)
    private String cardHolderName;

    private LocalDateTime deletedAt;
}
