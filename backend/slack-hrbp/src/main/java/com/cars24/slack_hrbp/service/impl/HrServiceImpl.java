package com.cars24.slack_hrbp.service.impl;

import com.cars24.slack_hrbp.data.dao.impl.HrDaoImpl;
import com.cars24.slack_hrbp.data.dao.impl.ListAllEmployeesUnderManagerDaoImpl;
import com.cars24.slack_hrbp.data.dto.UserDto;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
import com.cars24.slack_hrbp.data.request.CreateEmployeeRequest;
import com.cars24.slack_hrbp.data.response.EmployeeDisplayResponse;
import com.cars24.slack_hrbp.data.response.UpdateEmployeeResponse;
import com.cars24.slack_hrbp.excpetion.UserServiceException;
import com.cars24.slack_hrbp.service.HrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor

public class HrServiceImpl implements HrService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final HrDaoImpl hrDao;
    private final Neo4jClient neo4jClient;
    private final EmployeeRepository employeeRepository;
    private final ListAllEmployeesUnderManagerDaoImpl listAllEmployeesUnderManagerDao;


    @Transactional
    public EmployeeEntity createEmployee(CreateEmployeeRequest request) {
        EmployeeEntity manager = null;
        String managerName = null;

        Optional<EmployeeEntity> employee = employeeRepository.findByUserId(request.getUserId());
        if(employee.isPresent())
            throw new UserServiceException("User already exists");

        if (request.getManagerId() != null) {
            Optional<EmployeeEntity> managerOpt = employeeRepository.findByUserId(request.getManagerId());
            if (managerOpt.isPresent()) {
                manager = managerOpt.get();
                managerName = manager.getUsername();
            } else {
                throw new UserServiceException("Manager not found");
            }
        }

        String encoded = bCryptPasswordEncoder.encode(request.getPassword());

        return employeeRepository.createEmployeeWithManager(
                request.getUserId(),
                request.getUsername(),
                request.getEmail(),
                encoded,
                managerName,
                request.getManagerId(),
                request.getRoles()
        );
    }

    @Transactional
    public void updateManager(String userId, String newManagerId) {
        List<EmployeeEntity> subordinates = employeeRepository.findByManagerId(userId);

        boolean isNewManagerASubordinate = subordinates.stream()
                .anyMatch(employee -> employee.getUserId().equals(newManagerId));

        if (isNewManagerASubordinate) {
            throw new UserServiceException("Cannot update manager to a subordinate. We detected a cycle");
        }

        String query = """
            MATCH (e:Employee {userId: $userId})-[r:REPORTED_BY]->(oldManager)
            DELETE r
            WITH e
            SET e.managerId = $newManagerId
            WITH e
            MATCH (newManager:Employee {userId: $newManagerId})
            MERGE (e)-[:REPORTED_BY]->(newManager)
            SET e.managerName = newManager.username
            """;

        neo4jClient.query(query)
                .bind(userId).to("userId")
                .bind(newManagerId).to("newManagerId")
                .run();
    }

    @Override
    public String updateUser(EmployeeUpdateRequest employeeUpdateRequest) {
        System.out.println("Request reached Service for update user");
        return hrDao.updateUser(employeeUpdateRequest);
    }


    @Override
    public Page<List<String>> getAllUsers(String userId, int page, int limit, String searchtag) {
        return hrDao.getAllUsers(userId,page,limit,searchtag);
    }

    @Override
    public long getTotalEmployeesCount(String searchtag){
        return hrDao.getTotalEmployeesCount(searchtag);
    }

}
