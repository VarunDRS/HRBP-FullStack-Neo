package com.cars24.slack_hrbp.data.dao;

import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
import com.cars24.slack_hrbp.data.request.CreateEmployeeRequest;
import com.cars24.slack_hrbp.data.response.EmployeeDisplayResponse;
import com.cars24.slack_hrbp.data.response.UpdateEmployeeResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface HrDao {
//    String createUser(CreateEmployeeRequest createEmployeeRequest);

    String updateUser(EmployeeUpdateRequest employeeUpdateRequest);

    Page<List<String>> getAllUsers(String userId, int page, int limit);

//    EmployeeEntity getUser(String userid);

    public long getTotalEmployeesCount();
}
