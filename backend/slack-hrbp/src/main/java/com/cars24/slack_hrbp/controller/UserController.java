package com.cars24.slack_hrbp.controller;

import com.cars24.slack_hrbp.data.dto.UserDto;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.request.PasswordUpdateRequest;
import com.cars24.slack_hrbp.data.request.PasswordVerificationRequest;
import com.cars24.slack_hrbp.data.request.SignUpRequest;
import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
import com.cars24.slack_hrbp.data.response.GetUserResponse;
import com.cars24.slack_hrbp.service.UserService;
import com.cars24.slack_hrbp.service.impl.EmployeeServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmployeeServiceImpl employeeService;

    @GetMapping("/{userId}")
    public ResponseEntity<String> getUserName(@PathVariable String userId) {
        Optional<EmployeeEntity> entity = employeeRepository.findByUserId(userId);
        if (!entity.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        String userName = entity.get().getUsername();
        return ResponseEntity.ok(userName);
    }

    @PostMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateRequest passwordUpdateRequest){
        String response = employeeService.updatePassword(passwordUpdateRequest);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }


    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody PasswordVerificationRequest request,
                                            @RequestHeader("Authorization") String token) {

        log.info("[Password Verification] Verifying password for user: {}", request.getUserId());

        // Fetch user details from Neo4j
        Optional<EmployeeEntity> optionalEntity = employeeRepository.findByUserId(request.getUserId());

        if (optionalEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "User not found"));
        }

        EmployeeEntity entity = optionalEntity.get();

        // Compare passwords using BCrypt
        boolean isValid = bCryptPasswordEncoder.matches(request.getPassword(), entity.getEncryptedPassword());

        if (isValid) {
            return ResponseEntity.ok(Collections.singletonMap("success", "Password is correct"));
        } else {
            return ResponseEntity.status(400)
                    .body(Collections.singletonMap("error", "Invalid password"));
        }
    }

}