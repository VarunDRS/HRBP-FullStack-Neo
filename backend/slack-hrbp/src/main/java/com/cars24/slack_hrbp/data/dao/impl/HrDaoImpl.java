package com.cars24.slack_hrbp.data.dao.impl;

import com.cars24.slack_hrbp.data.dao.HrDao;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
import com.cars24.slack_hrbp.data.request.CreateEmployeeRequest;
import com.cars24.slack_hrbp.data.response.EmployeeDisplayResponse;
import com.cars24.slack_hrbp.data.response.UpdateEmployeeResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Data
@Slf4j

public class HrDaoImpl implements HrDao {

    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Neo4jClient neo4jClient;

//    @Override
//    public String createUser(CreateEmployeeRequest createEmployeeRequest) {
//        EmployeeEntity employeeEntity = new EmployeeEntity();
//        BeanUtils.copyProperties(createEmployeeRequest, employeeEntity);
//        employeeEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(createEmployeeRequest.getPassword()));
//        employeeEntity.setRoles(createEmployeeRequest.getRoles());
//        employeeRepository.save(employeeEntity);
//        return "Creation of employee account was successful";
//    }

    @Override
    public String updateUser(EmployeeUpdateRequest employeeUpdateRequest) {
        Optional<EmployeeEntity> employeeOpt = employeeRepository.findByUserId(employeeUpdateRequest.getUserId());

        if (employeeOpt.isEmpty()) {
            throw new RuntimeException("Employee not found");
        }

        employeeRepository.updateEmployeeRoles(
                employeeUpdateRequest.getUserId(),
                employeeUpdateRequest.getRoles() // Pass list directly
        );

        return "Update was successful";
    }


    @Override
    public Page<List<String>> getAllUsers(String userId, int page, int limit, String searchtag) {
        String query;

        if (searchtag == null || searchtag.trim().isEmpty()) {
            // Fetch all employees without filtering
            query = "MATCH (e:Employee) RETURN e.userId, e.email, e.username SKIP $skip LIMIT $limit";
//            query = "MATCH (:Employee {userId: $userId})<-[:REPORTED_BY*]-(e:Employee) " +
//                    "RETURN e.userId, e.email, e.username SKIP $skip LIMIT $limit";
        } else {
            // Fetch employees matching search criteria
            query = "MATCH (e:Employee) " +
                    "WHERE TOLOWER(e.username) STARTS WITH TOLOWER($searchtag) " +
                    "   OR TOLOWER(e.username) CONTAINS TOLOWER($searchtag) " +
                    "RETURN e.userId, e.email, e.username SKIP $skip LIMIT $limit";
        }

        var queryBuilder = neo4jClient.query(query)
                .bind(page * limit).to("skip")
                .bind(limit).to("limit");

        if (searchtag != null && !searchtag.trim().isEmpty()) {
            queryBuilder = queryBuilder.bind(searchtag).to("searchtag");
        }

        List<List<String>> results = queryBuilder.fetch().all()
                .stream()
                .map(record -> List.of(
                        record.get("e.userId").toString(),
                        record.get("e.email").toString(),
                        record.get("e.username").toString()))
                .toList();

        return new PageImpl<>(results, PageRequest.of(page, limit), results.size());
    }

    @Override
    public long getTotalEmployeesCount(String searchtag){
        if(searchtag==null || searchtag.trim().isEmpty()){
            return employeeRepository.count();
        }
        else{
            return employeeRepository.countBySearchtag(searchtag);
        }
    }
}
