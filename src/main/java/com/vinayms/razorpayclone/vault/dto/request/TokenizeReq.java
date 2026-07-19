package com.vinayms.razorpayclone.vault.dto.request;

import com.vinayms.razorpayclone.common.annotations.CardExpiryValidation;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.LuhnCheck;


import java.util.UUID;



public record TokenizeReq(

        @NotBlank(message = "Card number is required")
        @LuhnCheck(message = "Invalid card number")
        @Pattern(regexp = "^[0-9]{13,19}$", message = "Invalid card number")
        String pan,

        @NotBlank(message = "CVV is required")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "Invalid CVV")
        String cvv,

        @Valid
        @NotBlank
        @Embedded
        @CardExpiryValidation
        CardExpiry expiry,

        UUID customerId,

        @Size(min=3)
        String cardHolderName
) {
}
