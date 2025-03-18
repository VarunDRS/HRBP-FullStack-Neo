package com.cars24.slack_hrbp.service;

import com.cars24.slack_hrbp.data.dto.UserDto;
import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    public UserDto createUser(UserDto user);
    public UserDto getUser(String email);
}