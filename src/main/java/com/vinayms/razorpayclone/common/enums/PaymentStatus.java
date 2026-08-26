package com.vinayms.razorpayclone.common.enums;

public enum PaymentStatus {
    CREATED,

    AUTHORIZING,
    AUTHORIZED,
    AUTH_EXPIRED,

    CAPTURED,
    CAPTURING,
    CAPTURE_FAILED,

    SETTLED,

    PARTIAL_REFUND,
    REFUNDED,


    FAILED,
    CANCELLED
}

