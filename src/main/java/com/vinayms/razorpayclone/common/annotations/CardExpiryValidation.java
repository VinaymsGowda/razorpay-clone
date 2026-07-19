package com.vinayms.razorpayclone.common.annotations;

import com.vinayms.razorpayclone.common.validations.CardExpiryValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target({
        ElementType.FIELD,
        ElementType.PARAMETER,
        ElementType.RECORD_COMPONENT
})
@Constraint(validatedBy = CardExpiryValidator.class)
public @interface CardExpiryValidation {

    String message() default "Card expiry date is invalid";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
