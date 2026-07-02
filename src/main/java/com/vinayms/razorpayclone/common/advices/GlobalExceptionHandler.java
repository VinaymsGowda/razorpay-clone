package com.vinayms.razorpayclone.common.advices;

import com.vinayms.razorpayclone.common.exceptions.ConflictException;
import com.vinayms.razorpayclone.common.exceptions.DuplicateResourceException;
import com.vinayms.razorpayclone.common.exceptions.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.DuplicateFormatFlagsException;
import java.util.List;


@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class, DuplicateResourceException.class})
    public ResponseEntity<ApiResponse<?>> handleConflictResourceException(Exception e) {
        String message = e.getLocalizedMessage();
        ApiError apiError=ApiError.builder().message(message).build();
        ApiResponse<?> apiResponse=new ApiResponse<>(apiError);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException e) {
        String message = e.getLocalizedMessage();
        ApiError apiError=ApiError.builder().message(message).build();
        ApiResponse<?> apiResponse=new ApiResponse<>(apiError);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = "BAD Request";
        List<SubErrors> fieldErrors = e.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> SubErrors.builder()
                        .field(fieldError.getField())
                        .message(fieldError.getDefaultMessage())
                        .build())
                .toList();
        ApiError apiError=ApiError.builder().message(message).subErrors(fieldErrors).build();
        ApiResponse<?> apiResponse=new ApiResponse<>(apiError);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        String message = e.getLocalizedMessage();
        ApiError apiError=ApiError.builder().message(message).build();
        ApiResponse<?> apiResponse=new ApiResponse<>(apiError);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    public ApiResponse<?> buildApiResponse(ApiError error){
        return new ApiResponse<>(error);
    }
}
