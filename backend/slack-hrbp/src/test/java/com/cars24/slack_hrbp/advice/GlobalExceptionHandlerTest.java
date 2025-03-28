package com.cars24.slack_hrbp.advice;

import com.cars24.slack_hrbp.data.response.ApiResponse;
import com.cars24.slack_hrbp.excpetion.UserDoesntExistException;
import com.cars24.slack_hrbp.excpetion.UserServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleValidationExceptions() {
        // Create a mock MethodArgumentNotValidException
        MethodArgumentNotValidException mockException = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        // Setup mock field error
        FieldError fieldError = new FieldError("objectName", "fieldName", "error message");
        when(mockException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        // Call the method
        ResponseEntity<ApiResponse> responseEntity = globalExceptionHandler.handleValidationExceptions(mockException);

        // Assertions
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        ApiResponse apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(400, apiResponse.getStatuscode());
        assertFalse(apiResponse.isSuccess());
        assertEquals("Validation failed", apiResponse.getMessage());
        assertTrue(apiResponse.getService().startsWith("AppValidation"));

        // Check the error map
        Map<String, String> errorMap = (Map<String, String>) apiResponse.getData();
        assertNotNull(errorMap);
        assertEquals("error message", errorMap.get("fieldName"));
    }

    @Test
    void testHandleUserServiceExceptions() {
        // Create a test exception
        UserServiceException testException = new UserServiceException("Test user service error");

        // Call the method
        ResponseEntity<ApiResponse> responseEntity = globalExceptionHandler.handleUserServiceExceptions(testException);

        // Assertions
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        ApiResponse apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(400, apiResponse.getStatuscode());
        assertFalse(apiResponse.isSuccess());
        assertEquals("Test user service error", apiResponse.getMessage());
        assertTrue(apiResponse.getService().startsWith("AppUsr"));
        assertNull(apiResponse.getData());
    }

    @Test
    void testHandleUserDoesntExistException() {
        // Create a test exception
        UserDoesntExistException testException = new UserDoesntExistException("User not found");

        // Call the method
        ResponseEntity<ApiResponse> responseEntity = globalExceptionHandler.handleUserDoesntExistException(testException);

        // Assertions
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        ApiResponse apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(400, apiResponse.getStatuscode());
        assertFalse(apiResponse.isSuccess());
        assertEquals("User not found", apiResponse.getMessage());
        assertTrue(apiResponse.getService().startsWith("HR Update Service"));
        assertNull(apiResponse.getData());
    }

    @Test
    void testAnnotations() {
        // Verify @ControllerAdvice annotation
        assertTrue(GlobalExceptionHandler.class.isAnnotationPresent(org.springframework.web.bind.annotation.ControllerAdvice.class));

        // Verify @Slf4j annotation (via reflection on the log field)
        try {
            assertNotNull(GlobalExceptionHandler.class.getDeclaredField("log"));
        } catch (NoSuchFieldException e) {
            fail("Lombok @Slf4j annotation should create a log field");
        }
    }
}