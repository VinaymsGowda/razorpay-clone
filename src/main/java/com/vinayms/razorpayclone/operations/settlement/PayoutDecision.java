package com.vinayms.razorpayclone.operations.settlement;

public record PayoutDecision(boolean success, boolean retryable, String failureCode, String failureReason) {

    public static PayoutDecision approved() {
        return new PayoutDecision(true, false, null, null);
    }

    public static PayoutDecision retryable(String failureCode, String failureReason) {
        return new PayoutDecision(false, true, failureCode, failureReason);
    }

    public static PayoutDecision permanent(String failureCode, String failureReason) {
        return new PayoutDecision(false, false, failureCode, failureReason);
    }
}
