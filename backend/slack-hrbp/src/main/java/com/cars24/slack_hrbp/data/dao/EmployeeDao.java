package com.cars24.slack_hrbp.data.dao;

import com.cars24.slack_hrbp.data.request.PasswordUpdateRequest;

public interface EmployeeDao {
    String updatePassword(PasswordUpdateRequest passwordUpdateRequest);
}
