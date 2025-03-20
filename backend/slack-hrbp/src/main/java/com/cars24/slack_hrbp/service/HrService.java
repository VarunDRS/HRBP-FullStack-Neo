package com.cars24.slack_hrbp.service;

import com.cars24.slack_hrbp.data.dto.UserDto;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
import com.cars24.slack_hrbp.data.request.CreateEmployeeRequest;
import com.cars24.slack_hrbp.data.response.EmployeeDisplayResponse;
import com.cars24.slack_hrbp.data.response.UpdateEmployeeResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface HrService {

    String updateUser(EmployeeUpdateRequest employeeUpdateRequest);

    Page<List<String>> getAllUsers(String userId, int page, int limit,String searchtag);

//    EmployeeEntity getUser(String userid);

    public long getTotalEmployeesCount(String searchtag);

    public String deleteEntry(String userId,String date);

//    List<UserDto> paginatedUsers(int page, int limit);
}
