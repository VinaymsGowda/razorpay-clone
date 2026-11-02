package com.vinayms.razorpayclone.common.enums;

// PENDING - > DELIVERED
// PENDING -> FAILED -> PENDING
// FAILED -> DEAD
public enum WebhookEventStatus {
    PENDING,
    PROCESSING,
    DELIVERED,
    FAILED,
    DEAD
}
