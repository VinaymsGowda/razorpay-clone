package com.vinayms.razorpayclone.common.enums;

public enum OutboxEventType {
    ORDER_CREATED,
    ORDER_CANCELED,
    PAYMENT_INITIATED,
    PAYMENT_CAPTURED,
    PAYMENT_FAILED
}
