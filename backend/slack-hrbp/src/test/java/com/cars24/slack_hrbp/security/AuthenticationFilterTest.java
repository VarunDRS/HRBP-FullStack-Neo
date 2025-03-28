package com.cars24.slack_hrbp.security;

import com.cars24.slack_hrbp.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserService userService;

    @Mock
    private PrintWriter printWriter;

    private AuthenticationFilter authenticationFilter;

    @BeforeEach
    void setUp() {
        authenticationFilter = new AuthenticationFilter(authenticationManager);
    }


    @Test
    void testAttemptAuthentication_InvalidCredentials() {
        // Prepare
        try {
            when(request.getInputStream()).thenThrow(new IOException("Invalid input"));

            // Execute and expect RuntimeException
            assertThrows(RuntimeException.class, () ->
                    authenticationFilter.attemptAuthentication(request, response)
            );
        } catch (IOException e) {
            fail("Unexpected IOException during test setup");
        }
    }
}