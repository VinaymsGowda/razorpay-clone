package com.vinayms.razorpayclone.vault.dto.response;

import com.vinayms.razorpayclone.vault.dto.request.CardExpiry;

public record TokenizeResp(
        String token,
        CardExpiry cardExpiry,
        String lastFourDigits
) {
}
