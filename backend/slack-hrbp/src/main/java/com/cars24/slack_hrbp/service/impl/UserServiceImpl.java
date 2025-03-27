package com.cars24.slack_hrbp.service.impl;

import com.cars24.slack_hrbp.data.dao.UserDao;
import com.cars24.slack_hrbp.data.dto.UserDto;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
import com.cars24.slack_hrbp.excpetion.UserServiceException;
import com.cars24.slack_hrbp.service.UserService;
import com.cars24.slack_hrbp.util.Utils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final Neo4jClient neo4jClient;
    private final Utils utils;
    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDto createUser(UserDto user) {
        log.info("[createUser] UserServiceImpl {}", user);

        if (user.getFirstName() == null || user.getLastName() == null || user.getEmail() == null || user.getPassword() == null)
            throw new UserServiceException("Empty fields are not allowed");

        if (employeeRepository.existsByEmail(user.getEmail()))
            throw new UserServiceException("Record already exists");

        EmployeeEntity employeeEntity = new EmployeeEntity();
        BeanUtils.copyProperties(user, employeeEntity);

        employeeEntity.setUserId(utils.generateUserId(10));

        employeeEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            employeeEntity.setRoles(List.of("ROLE_EMPLOYEE"));
        } else {
            List<String> formattedRoles = user.getRoles().stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)  // Add ROLE_ prefix if not present
                    .collect(Collectors.toList());
            employeeEntity.setRoles(formattedRoles);  // Set the formatted roles
        }

        // Save the user entity
        EmployeeEntity savedUser = employeeRepository.save(employeeEntity);

        // Convert saved entity back to DTO to return the response
        UserDto signUpResponse = new UserDto();
        BeanUtils.copyProperties(savedUser, signUpResponse);
        return signUpResponse;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<List<String>> getAllUsers(String userId, int page, int limit) {
        String query = "MATCH (e:Employee) " +
                "RETURN e.userId, e.email, e.username SKIP $skip LIMIT $limit";

        List<List<String>> results = neo4jClient.query(query)
                .bind(page * limit).to("skip")
                .bind(limit).to("limit")
                .fetch().all()
                .stream()
                .map(record -> List.of(
                        record.get("e.userId").toString(),
                        record.get("e.email").toString(),
                        record.get("e.username").toString()))
                .toList();

        return new PageImpl<>(results, PageRequest.of(page, limit), results.size());
    }



    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UserServiceException {
        log.info("[loadUserByUsername] UserServiceImpl {} ", username);

        // Find the user entity by email (which is the username)
        EmployeeEntity employeeEntity = employeeRepository.findByEmail(username);

        // If no user is found, throw an exception
        if (employeeEntity == null)
            throw new UserServiceException("User hasn't signed up");

        // Convert roles to Spring Security authorities
        List<GrantedAuthority> authorities = employeeEntity.getRoles().stream()
                .map(role -> role.startsWith("ROLE_") ? new SimpleGrantedAuthority(role) : new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        // Return a new User object with the username, password, and authorities (roles)
        return new User(username, employeeEntity.getEncryptedPassword(), authorities);
    }

    public UserDto getUser(String email){

        UserDto response = new UserDto();
        EmployeeEntity employeeEntity = employeeRepository.findByEmail(email);

        if(employeeEntity == null)
            throw new UserServiceException("No entry by the given user id");

        BeanUtils.copyProperties(employeeEntity, response);
        return response;
    }


    public List<UserDto> getAllUsers(int page, int limit) {

        Pageable pageable = (Pageable) PageRequest.of(page, limit);

        if(page > 0)
            page -= 1;

        List<UserDto> users = new ArrayList<>();

        Page<EmployeeEntity> userPage = employeeRepository.findAll(pageable);
        List<EmployeeEntity> response = userPage.getContent();

        for(EmployeeEntity res : response){
            UserDto user = new UserDto();
            BeanUtils.copyProperties(res, user);
            users.add(user);
        }

        return users;
    }
}