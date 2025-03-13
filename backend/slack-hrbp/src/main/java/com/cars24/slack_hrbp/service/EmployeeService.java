package com.cars24.slack_hrbp.service;

import com.cars24.slack_hrbp.data.request.PasswordUpdateRequest;

public interface EmployeeService {
    String updatePassword(PasswordUpdateRequest passwordUpdateRequest);
}
