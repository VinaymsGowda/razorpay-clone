package com.vinayms.razorpayclone.common.exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
