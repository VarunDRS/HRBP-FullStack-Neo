package com.cars24.slack_hrbp.data.request;

import lombok.Data;

@Data
public class PasswordUpdateRequest {
    private String newPassword;
    private String userId;
}
