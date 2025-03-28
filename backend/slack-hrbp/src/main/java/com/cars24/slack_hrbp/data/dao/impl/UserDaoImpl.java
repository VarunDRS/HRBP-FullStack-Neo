package com.cars24.slack_hrbp.data.dao.impl;


import com.cars24.slack_hrbp.data.dao.UserDao;
import com.cars24.slack_hrbp.data.dto.UserDto;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
import com.cars24.slack_hrbp.util.Utils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDaoImpl implements UserDao {

    private final EmployeeRepository employeeRepository;
    private final Utils utils;

    public UserDto createUser(EmployeeEntity employeeEntity) {

        log.info("[createUser] UserDaoImpl{}", employeeEntity);

        employeeRepository.save(employeeEntity);

        UserDto signUpResponse = new UserDto();
        BeanUtils.copyProperties(employeeEntity, signUpResponse);

        return signUpResponse;

    }

    @Override
    public UserDto displayCustomer(String id) {

        log.info("[displayCustomer] UserDaoImpl {}", id);

        UserDto userDto = new UserDto();
        Optional<EmployeeEntity> employeeEntity = employeeRepository.findByUserId(id);
        BeanUtils.copyProperties(employeeEntity, userDto);
        return userDto;
    }


    @Transactional
    @Override
    public UserDto deleteUser(String id) {

        log.info("[deleteCustomer] UserDaoImpl {}", id);

        Optional<EmployeeEntity> employeeEntity = employeeRepository.findByUserId(id);
        UserDto response = new UserDto();

        BeanUtils.copyProperties(employeeEntity, response);

        employeeRepository.deleteByUserId(id);
        return response;

    }
}
