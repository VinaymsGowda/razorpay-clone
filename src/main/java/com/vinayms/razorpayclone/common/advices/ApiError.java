package com.vinayms.razorpayclone.common.advices;

import lombok.Builder;
import lombok.Data;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ApiError {

    private String message;

    private List<SubErrors> subErrors;


}
