package com.cars24.slack_hrbp.data.dao.impl;

import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.neo4j.core.Neo4jClient;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j

public class ListAllEmployeesUnderManagerDaoImpl{

    private final EmployeeRepository employeeRepository;
    private final Neo4jClient neo4jClient;

    public Page<List<String>> getAllEmployeesUnderManager(String userId, int page, int limit, String searchtag) {
        String query;

        if (searchtag == null || searchtag.trim().isEmpty()) {
            query = "MATCH (:Employee {userId: $userId})<-[:REPORTED_BY*]-(e:Employee) " +
                    "RETURN e.userId, e.email, e.username SKIP $skip LIMIT $limit";
        } else {
            query = "MATCH (:Employee {userId: $userId})<-[:REPORTED_BY*]-(e:Employee) " +
                    "WHERE TOLOWER(e.username) STARTS WITH TOLOWER($searchtag) " +
                    "   OR TOLOWER(e.username) CONTAINS TOLOWER($searchtag) " +
                    "RETURN e.userId, e.email, e.username SKIP $skip LIMIT $limit";
        }

        var queryBuilder = neo4jClient.query(query)
                .bind(userId).to("userId")
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

    public long getTotalEmployeesCount(String userId,String searchtag) {
        if(searchtag==null || searchtag.trim().isEmpty()) {
            return employeeRepository.countByManagerId(userId);
        }
        else{
            return employeeRepository.countByManagerIdAndSearchtag(userId, searchtag);
        }

    }

}
