package com.cars24.slack_hrbp.advice;

import com.cars24.slack_hrbp.data.response.ApiResponse;
import com.cars24.slack_hrbp.excpetion.UserDoesntExistException;
import com.cars24.slack_hrbp.excpetion.UserServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // Handle validation exceptions (for @Valid annotations in controllers)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException exception) {
        log.info("[handleUserDoesntExistException] Caught Exception: {}", exception.getMessage(), exception);

        // Create an error map to capture validation errors
        Map<String, String> errorMap = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> {
            errorMap.put(error.getField(), error.getDefaultMessage());
        });

        // Create a response for validation errors
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatuscode(400);
        apiResponse.setSuccess(false);
        apiResponse.setMessage("Validation failed");
        apiResponse.setService("AppValidation " + HttpStatus.BAD_REQUEST.value());
        apiResponse.setData(errorMap);

        return ResponseEntity.badRequest().body(apiResponse);
    }

    // Handle UserServiceExceptions
    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<ApiResponse> handleUserServiceExceptions(UserServiceException exception) {
        log.info("[handleUserServiceExceptions]");

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatuscode(400);
        apiResponse.setSuccess(false);
        apiResponse.setMessage(exception.getMessage());
        apiResponse.setService("AppUsr " + HttpStatus.BAD_REQUEST.value());
        apiResponse.setData(null);

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(UserDoesntExistException.class)
    public ResponseEntity<ApiResponse> handleUserDoesntExistException(UserDoesntExistException exception){
        log.info("[handleUserDoesntExistException]");

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatuscode(400);
        apiResponse.setSuccess(false);
        apiResponse.setMessage(exception.getMessage());
        apiResponse.setService("HR Update Service " + HttpStatus.BAD_REQUEST.value());
        apiResponse.setData(null);

        return ResponseEntity.badRequest().body(apiResponse);

    }
}