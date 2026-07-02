package com.vinayms.razorpayclone.common.advices;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubErrors {
    private String field;
    private String message;
}
