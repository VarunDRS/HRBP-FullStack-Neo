package com.cars24.slack_hrbp.data.response;

import lombok.Data;

@Data

public class GetUserResponse {
    private String userId;
    private String username;
    private String email;
}