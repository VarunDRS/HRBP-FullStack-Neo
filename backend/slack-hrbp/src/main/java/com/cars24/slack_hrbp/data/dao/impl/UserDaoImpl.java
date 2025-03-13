package com.cars24.slack_hrbp.data.dao.impl;


import com.cars24.slack_hrbp.data.dao.UserDao;
import com.cars24.slack_hrbp.data.dto.UserDto;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
import com.cars24.slack_hrbp.util.Utils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j

public class UserDaoImpl implements UserDao {

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    Utils utils;

    public UserDto createUser(EmployeeEntity employeeEntity) {

        log.info("[createUser] UserDaoImpl{}", employeeEntity);

        EmployeeEntity response = employeeRepository.save(employeeEntity);

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

//    @Override
//    public UserDto updateUser(String id, EmployeeUpdateRequest employeeUpdateRequest) {
//
//        log.info("[displayCustomer] UserDaoImpl id, {}, request, {}", id, employeeUpdateRequest);
//
//        Optional<EmployeeEntity> employeeEntity = employeeRepository.findByUserId(id);
//
////        if(employeeUpdateRequest.getFirstName().length() != 0)
////            employeeEntity.setFirstName(employeeUpdateRequest.getFirstName());
////
////        if(employeeUpdateRequest.getLastName().length() != 0)
////            employeeEntity.setLastName(employeeUpdateRequest.getLastName());
////
////        if(employeeUpdateRequest.getPhone().length() != 0)
////            employeeEntity.setPhone(employeeUpdateRequest.getPhone());
////
////        if(employeeUpdateRequest.getCity().length() != 0)
////            employeeEntity.setCity(employeeUpdateRequest.getCity());
//
//        employeeRepository.save(employeeEntity);
//        System.out.println(employeeEntity);
//
//        UserDto response = new UserDto();
//        BeanUtils.copyProperties(employeeEntity, response);
//
//        log.info(response.getLastName(), " ", response.getFirstName());
//
//
//        return response;
//    }

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
