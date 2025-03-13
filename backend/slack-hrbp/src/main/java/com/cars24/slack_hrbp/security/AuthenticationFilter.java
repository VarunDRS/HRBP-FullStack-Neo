package com.cars24.slack_hrbp.security;


import com.cars24.slack_hrbp.SpringApplicationContext;
import com.cars24.slack_hrbp.data.dto.UserDto;
import com.cars24.slack_hrbp.data.request.LoginRequest;
import com.cars24.slack_hrbp.data.response.LoginResponse;
import com.cars24.slack_hrbp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public AuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    // Modify this method to remove the IOException from the signature
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) {
        try {
            // Parse credentials from request
            LoginRequest creds = new ObjectMapper().readValue(req.getInputStream(), LoginRequest.class);

            // Return the authentication token
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(creds.getEmail(), creds.getPassword(), new ArrayList<>())
            );
        } catch (IOException e) {
            // Handle the exception internally (if you want to log or throw a custom exception)
            throw new RuntimeException("Authentication failed", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
                                            Authentication auth) throws IOException {

        byte[] secretKeyBytes = SecurityConstants.TOKEN_SECRET.getBytes();
        SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
        Instant now = Instant.now();

        // Get logged-in username
        String userName = ((User) auth.getPrincipal()).getUsername();

        // Fetch roles/authorities from authentication object
        List<String> roles = auth.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .toList();

        // Fetch userId from the database
        UserService userService = (UserService) SpringApplicationContext.getBean("userServiceImpl");
        UserDto userDto = userService.getUser(userName);
        String userId = userDto.getUserId(); // Get the userId

        // Debug logs
        System.out.println("Roles assigned to user: " + roles);
        System.out.println("UserId assigned to token: " + userId);

        // Generate JWT with roles and userId
        String token = Jwts.builder()
                .subject(userName)
                .claim("userId", userId)  // Include userId in the token
                .claim("roles", roles)  // Add roles as claims
                .expiration(Date.from(now.plusMillis(SecurityConstants.EXPIRATION_TIME)))
                .issuedAt(Date.from(now))
                .signWith(secretKey)
                .compact();

        // Return token & user info in response body
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(new ObjectMapper().writeValueAsString(new LoginResponse(token, userDto.getUserId(), roles)));

        // Optional: Set headers for backward compatibility
        res.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + token);
        res.addHeader("UserId", userDto.getUserId());

    }

}
