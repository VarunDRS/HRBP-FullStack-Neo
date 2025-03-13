package com.cars24.slack_hrbp.data.request;

import lombok.Data;

@Data
public class PasswordVerificationRequest {
    private String userId;
    private String password;
}
