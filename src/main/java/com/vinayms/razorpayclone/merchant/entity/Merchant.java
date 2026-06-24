package com.vinayms.razorpayclone.merchant.entity;

import jakarta.persistence.*;
import lombok.*;
import com.vinayms.razorpayclone.common.entity.BaseAuditableEntity;
import com.vinayms.razorpayclone.common.enums.MerchantStatus;
import com.vinayms.razorpayclone.common.enums.BusinessType;
import java.util.UUID;

@Entity
@Table(name = "merchant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merchant extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "gst_id", unique = true)
    private String gstId;

    @Column(name = "pan", unique = true)
    private String pan;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type")
    private BusinessType businessType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MerchantStatus status;

    @Column(name = "settlement_bank_name")
    private String settlementBankName;

    @Column(name = "settlement_bank_account_num")
    private String settlementBankAccountNum;

    @Column(name = "settlement_bank_ifsc_code")
    private String settlementBankIfscCode;

    @Column(name = "settlement_bank_account_holder_name")
    private String settlementBankAccountHolderName;
    

}



