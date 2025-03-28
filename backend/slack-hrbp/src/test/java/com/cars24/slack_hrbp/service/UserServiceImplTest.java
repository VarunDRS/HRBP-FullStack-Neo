package com.cars24.slack_hrbp.service;

import com.cars24.slack_hrbp.data.dto.UserDto;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.excpetion.UserServiceException;
import com.cars24.slack_hrbp.service.impl.UserServiceImpl;
import com.cars24.slack_hrbp.util.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private Neo4jClient neo4jClient;

    @Mock
    private Utils utils;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto testUserDto;
    private EmployeeEntity testEmployeeEntity;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto();
        testUserDto.setFirstName("John");
        testUserDto.setLastName("Doe");
        testUserDto.setEmail("john.doe@example.com");
        testUserDto.setPassword("password123");

        testEmployeeEntity = new EmployeeEntity();
        testEmployeeEntity.setUserId("user123");
        testEmployeeEntity.setUsername("John Doe");
        testEmployeeEntity.setEmail("john.doe@example.com");
        testEmployeeEntity.setEncryptedPassword("encodedPassword"); // Mocked encoded password
        testEmployeeEntity.setRoles(List.of("ROLE_EMPLOYEE"));

    }

    @Test
    void testCreateUser_Success() {
        // Setup
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
        when(utils.generateUserId(10)).thenReturn("user123");
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(employeeRepository.save(any(EmployeeEntity.class))).thenReturn(testEmployeeEntity);

        // Execute
        UserDto createdUser = userService.createUser(testUserDto);

        // Verify
        assertNotNull(createdUser);
        verify(employeeRepository).save(any(EmployeeEntity.class));
        verify(bCryptPasswordEncoder).encode(testUserDto.getPassword());
    }

    @Test
    void testCreateUser_MissingFields() {
        UserDto incompleteUser = new UserDto();

        assertThrows(UserServiceException.class, () -> {
            userService.createUser(incompleteUser);
        });
    }

    @Test
    void testCreateUser_ExistingEmail() {
        when(employeeRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(UserServiceException.class, () -> {
            userService.createUser(testUserDto);
        });
    }

    @Test
    void testLoadUserByUsername_Success() {
        // Setup
        testEmployeeEntity.setRoles(List.of("ROLE_EMPLOYEE"));
        testEmployeeEntity.setEncryptedPassword("encodedPassword"); // Properly mock password
        when(employeeRepository.findByEmail("john.doe@example.com")).thenReturn(testEmployeeEntity);

        // Execute
        UserDetails userDetails = userService.loadUserByUsername("john.doe@example.com");

        // Verify
        assertNotNull(userDetails);
        assertEquals("john.doe@example.com", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE")));
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(null);

        assertThrows(UserServiceException.class, () -> {
            userService.loadUserByUsername("nonexistent@example.com");
        });
    }

    @Test
    void testGetUser_Success() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(testEmployeeEntity);

        UserDto retrievedUser = userService.getUser("john.doe@example.com");

        assertNotNull(retrievedUser);
        assertEquals(testEmployeeEntity.getEmail(), retrievedUser.getEmail());
    }

    @Test
    void testGetUser_NotFound() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(null);

        assertThrows(UserServiceException.class, () -> {
            userService.getUser("nonexistent@example.com");
        });
    }

    @Test
    void testGetAllUsers_Pagination() {
        // Prepare mock page of employee entities
        List<EmployeeEntity> employeeEntities = Arrays.asList(
                createMockEmployeeEntity("user1", "email1@example.com"),
                createMockEmployeeEntity("user2", "email2@example.com")
        );
        Page<EmployeeEntity> mockPage = new PageImpl<>(employeeEntities);

        // Setup mock behavior
        when(employeeRepository.findAll(any(PageRequest.class))).thenReturn(mockPage);

        // Execute
        List<UserDto> users = userService.getAllUsers(0, 2);

        // Verify
        assertNotNull(users);
        assertEquals(2, users.size());
        verify(employeeRepository).findAll(any(PageRequest.class));
    }

    // Helper method to create mock EmployeeEntity
    private EmployeeEntity createMockEmployeeEntity(String userId, String email) {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setUserId(userId);
        entity.setEmail(email);
        entity.setUsername("Test User");
        return entity;
    }
}