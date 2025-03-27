package com.cars24.slack_hrbp.service.impl;

import com.cars24.slack_hrbp.data.dao.impl.HrDaoImpl;
import com.cars24.slack_hrbp.data.dao.impl.ListAllEmployeesUnderManagerDaoImpl;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.entity.ProfileEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.repository.ProfileRepository;
import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
import com.cars24.slack_hrbp.data.request.CreateEmployeeRequest;
import com.cars24.slack_hrbp.excpetion.UserServiceException;
import com.cars24.slack_hrbp.service.HrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import io.github.cdimascio.dotenv.Dotenv;



@Slf4j
@Service
@RequiredArgsConstructor

public class HrServiceImpl implements HrService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final HrDaoImpl hrDao;
    private final Neo4jClient neo4jClient;
    private final EmployeeRepository employeeRepository;
    private final ListAllEmployeesUnderManagerDaoImpl listAllEmployeesUnderManagerDao;
    private final AttendanceRepository attendanceRepository;
    private final ProfileRepository profileRepository;


    @Transactional
    public EmployeeEntity createEmployee(CreateEmployeeRequest request) {
        String managerid = null;
        String managerName = null;

        if(employeeRepository.existsByEmail(request.getEmail()))
            throw new UserServiceException("User already there");

        if(!employeeRepository.existsByEmail(request.getManagerEmail()))
            throw new UserServiceException("Manager not found");

        if(!profileRepository.existsByEmail(request.getEmail()))
            throw new UserServiceException("User doesnt exist in slack");

        if (request.getManagerEmail() != null) {
            EmployeeEntity managerOpt = employeeRepository.findByEmail(request.getManagerEmail());
            managerid = managerOpt.getUserId();
            managerName = managerOpt.getUsername();
        }


        Dotenv dotenv = Dotenv.load();
        String password = dotenv.get("DEFAULT_TEMP_PASSWORD");

        ProfileEntity entity = profileRepository.findByEmail(request.getEmail());

        String userId = entity.getSlackid();
        String userName = entity.getName();

        String encoded = bCryptPasswordEncoder.encode(password);

        return employeeRepository.createEmployeeWithManager(
                userId,
                userName,
                request.getEmail(),
                encoded,
                managerName,
                managerid,
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

    @Override
    public String deleteEntry(String userId,String date){
        if(!attendanceRepository.existsByUseridAndDateStartingWith(userId,date)){
            throw new UserServiceException("Not such entry for the user exists");
        }
        return hrDao.deleteEntry(userId,date);
    }

    @Override
    public String addEntry(String userId,String date,String leaveType,String reason){
//        if(attendanceRepository.existsByUseridAndDateStartingWith(userId,date)){
//            throw new UserServiceException("Not such entry for the user exists");
//        }
        return hrDao.addEntry(userId,date,leaveType,reason);
    }

}
