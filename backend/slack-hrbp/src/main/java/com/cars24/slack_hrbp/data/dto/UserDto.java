package com.cars24.slack_hrbp.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserDto {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String city;
    private String phone;
    private boolean emailVerificationStatus;
    private List<String> roles;
}
