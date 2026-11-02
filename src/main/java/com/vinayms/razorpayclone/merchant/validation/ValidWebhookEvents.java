package com.vinayms.razorpayclone.merchant.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = WebhookEventsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidWebhookEvents {
    String message() default "contains an unsupported, empty, or duplicate event type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
