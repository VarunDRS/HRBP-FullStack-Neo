package com.cars24.slack_hrbp.data.request;

import lombok.Data;

import java.util.List;

@Data
public class SignUpRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String city;
    private String phone;
    private String password;
    private List<String> roles;  // Add roles field here
}
