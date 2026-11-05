package com.vinayms.razorpayclone.payment.repository;

import java.util.UUID;

public record SettleablePaymentGroup(UUID merchantId, String currency) {
}
