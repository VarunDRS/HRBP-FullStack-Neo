package com.cars24.slack_hrbp.data.dao;


import com.cars24.slack_hrbp.data.dto.UserDto;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;

public interface UserDao {

    UserDto displayCustomer(String id);

    UserDto deleteUser(String id);
}