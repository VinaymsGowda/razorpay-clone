package com.vinayms.razorpayclone.common.validations;


import com.vinayms.razorpayclone.common.annotations.CardExpiryValidation;
import com.vinayms.razorpayclone.vault.dto.request.CardExpiry;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.YearMonth;
import java.util.regex.Pattern;

public class CardExpiryValidator implements ConstraintValidator<CardExpiryValidation, CardExpiry> {


    @Override
    public boolean isValid(CardExpiry value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        Integer expiryMonth = value.getExpiryMonth();
        Integer expiryYear = value.getExpiryYear();


        if (expiryMonth == null || expiryYear == null) {
            return false;
        }

        Pattern yearRegex=Pattern.compile("^\\d{4}$");
        if (!yearRegex.matcher(String.valueOf(expiryYear)).matches()) {
            return false;
        }
        if (expiryMonth < 1 || expiryMonth > 12) {
            return false;
        }

        // Check if the card is expired
        YearMonth currentYearMonth = java.time.YearMonth.now();
        YearMonth cardYearMonth = java.time.YearMonth.of(expiryYear, expiryMonth);

        return !cardYearMonth.isBefore(currentYearMonth);
    }
}
