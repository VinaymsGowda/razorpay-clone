package com.vinayms.razorpayclone.payment.dto.order.response;

import com.vinayms.razorpayclone.common.entity.Money;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;

public record OrderCreateRequest(

        @NotNull
        Money amount,

        @Size(max=50)
        String receipt,

        
        Map<String,String> notes,

        LocalDateTime expiresAt
) {
}
