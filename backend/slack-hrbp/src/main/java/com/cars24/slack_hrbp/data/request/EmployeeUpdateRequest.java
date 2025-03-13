package com.cars24.slack_hrbp.data.request;

import lombok.Data;

import java.util.List;

@Data

public class EmployeeUpdateRequest {
    String userId;
    private List<String> roles; // Instead of String role

}
