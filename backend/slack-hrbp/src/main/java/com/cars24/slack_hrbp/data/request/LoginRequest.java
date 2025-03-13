package com.cars24.slack_hrbp.data.request;

import lombok.Data;

@Data
public class LoginRequest {

    private String email;
    private String password;

}
