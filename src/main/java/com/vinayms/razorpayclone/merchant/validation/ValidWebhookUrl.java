package com.vinayms.razorpayclone.merchant.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = WebhookUrlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidWebhookUrl {
    String message() default "must be an absolute HTTP or HTTPS URL with a host";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
