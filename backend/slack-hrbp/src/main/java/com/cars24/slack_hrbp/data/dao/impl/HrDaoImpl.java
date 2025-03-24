package com.cars24.slack_hrbp.data.dao.impl;

import com.cars24.slack_hrbp.data.dao.HrDao;
import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
import com.cars24.slack_hrbp.excpetion.UserServiceException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@Service
@Data
@Slf4j

public class HrDaoImpl implements HrDao {

    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Neo4jClient neo4jClient;
    private final AttendanceRepository attendanceRepository;


    @Override
    public String updateUser(EmployeeUpdateRequest employeeUpdateRequest) {
        Optional<EmployeeEntity> employeeOpt = employeeRepository.findByUserId(employeeUpdateRequest.getUserId());

        if (employeeOpt.isEmpty()) {
            throw new UserServiceException("Employee not found");
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
            query = "MATCH (e:Employee) RETURN e.userId, e.email, e.username SKIP $skip LIMIT $limit";

        } else {
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

    @Override
    public String deleteEntry(String userId,String date){
        List<AttendanceEntity> attendanceEntity = attendanceRepository.findByUseridAndDateStartingWith(userId,date);

        for(AttendanceEntity entity : attendanceEntity){
            attendanceRepository.deleteById(entity.getId());
        }

        return "Delete of attendance successfull";
    }


    @Override
    public String addEntry(String userId,String date,String leaveType,String reason){

        Optional<EmployeeEntity> employeeEntity = employeeRepository.findByUserId(userId);
        String username = employeeEntity.map(EmployeeEntity::getUsername).orElse("Not Found Username");
        AttendanceEntity attendanceEntity = new AttendanceEntity();
        attendanceEntity.setType(leaveType);
        attendanceEntity.setDate(date);
        attendanceEntity.setReason(reason);
        attendanceEntity.setUserid(userId);
        attendanceEntity.setUsername(username);
        attendanceRepository.save(attendanceEntity);

        return "Adding of attendance successfull";
    }
}
