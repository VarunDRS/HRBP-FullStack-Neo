package com.cars24.slack_hrbp.data.response;

import lombok.Data;

import java.util.List;

@Data

public class LoginResponse {
    private String token;
    private String userId;
    private List<String> roles;

    public LoginResponse(String token, String userId, List<String> roles) {
        this.token = token;
        this.userId = userId;
        this.roles = roles;
    }

}
