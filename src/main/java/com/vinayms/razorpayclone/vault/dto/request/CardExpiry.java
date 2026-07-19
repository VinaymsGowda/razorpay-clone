package com.vinayms.razorpayclone.vault.dto.request;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class CardExpiry{
    @NotBlank
    @Min(value = 1, message = "Expiry month must be between 1 and 12")
    @Max(value = 12, message = "Expiry month must be between 1 and 12")
    Integer expiryMonth;

    @NotBlank
    Integer expiryYear;
}
