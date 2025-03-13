package com.cars24.slack_hrbp.service.impl;

import com.cars24.slack_hrbp.data.dao.impl.EmployeeDaoImpl;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.request.PasswordUpdateRequest;
import com.cars24.slack_hrbp.excpetion.UserServiceException;
import com.cars24.slack_hrbp.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeDaoImpl employeeDao;
    private final EmployeeRepository employeeRepository;

    @Override
    public String updatePassword(PasswordUpdateRequest passwordUpdateRequest) {
        System.out.println(passwordUpdateRequest);
        if(!employeeRepository.existsByUserId(passwordUpdateRequest.getUserId()))
            throw new UserServiceException("User not valid");
        return employeeDao.updatePassword(passwordUpdateRequest);
    }

}
