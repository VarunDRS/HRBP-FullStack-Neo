package com.cars24.slack_hrbp.data.response;

import lombok.Data;

@Data
public class UpdateEmployeeResponse {
    private String userid;
    private String username;
    private String managerid;
    private String managername;
    private String role;
}

