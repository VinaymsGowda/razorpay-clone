package com.vinayms.razorpayclone.merchant.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class WebhookEventsValidator implements ConstraintValidator<ValidWebhookEvents, String> {
    private static final Set<String> ALLOWED = Set.of(
            "ORDER_CREATED", "ORDER_CANCELED", "ORDER_CANCELLED", "PAYMENT_INITIATED", "PAYMENT_CAPTURED", "PAYMENT_FAILED",
            "REFUND_INIT", "REFUND_COMPLETE", "REFUND_SUCCESS", "REFUND_CREATED", "REFUND_PROCESSED", "REFUND_FAILED",
            "SETTLE", "SETTLEMENT_CREATED", "SETTLEMENT_PROCESSED", "SETTLEMENT_FAILED");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank() || value.length() > 500) return false;
        String[] events = value.split(",", -1);
        Set<String> normalized = Arrays.stream(events).map(String::trim).collect(Collectors.toSet());
        return normalized.size() == events.length && Arrays.stream(events)
                .map(String::trim).allMatch(event -> ALLOWED.contains(event) && !event.isEmpty());
    }
}
