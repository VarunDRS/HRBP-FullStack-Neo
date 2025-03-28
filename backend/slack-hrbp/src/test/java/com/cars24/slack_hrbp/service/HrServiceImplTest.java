package com.cars24.slack_hrbp.service.impl;

import com.cars24.slack_hrbp.data.dao.impl.HrDaoImpl;
import com.cars24.slack_hrbp.data.dao.impl.ListAllEmployeesUnderManagerDaoImpl;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.entity.ProfileEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.repository.ProfileRepository;
import com.cars24.slack_hrbp.data.request.CreateEmployeeRequest;
import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
import com.cars24.slack_hrbp.excpetion.UserServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class HrServiceImplTest {

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private HrDaoImpl hrDao;

    @Mock
    private Neo4jClient neo4jClient;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ListAllEmployeesUnderManagerDaoImpl listAllEmployeesUnderManagerDao;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private HrServiceImpl hrService;

    private CreateEmployeeRequest createEmployeeRequest;
    private EmployeeEntity managerEntity;
    private ProfileEntity profileEntity;
    private EmployeeEntity createdEmployee;

    @BeforeEach
    void setUp() {
        createEmployeeRequest = new CreateEmployeeRequest();
        createEmployeeRequest.setEmail("test@example.com");
        createEmployeeRequest.setManagerEmail("manager@example.com");
        createEmployeeRequest.setRoles(List.of("ROLE_USER"));

        managerEntity = new EmployeeEntity();
        managerEntity.setUserId("manager123");
        managerEntity.setUsername("Manager Name");

        profileEntity = new ProfileEntity();
        profileEntity.setSlackid("user123");
        profileEntity.setName("Test User");

        createdEmployee = new EmployeeEntity();
        createdEmployee.setUserId("user123");
    }

    @Test
    void createEmployee_Success() {
        when(employeeRepository.existsByEmail(createEmployeeRequest.getEmail())).thenReturn(false);
        when(employeeRepository.existsByEmail(createEmployeeRequest.getManagerEmail())).thenReturn(true);
        when(profileRepository.existsByEmail(createEmployeeRequest.getEmail())).thenReturn(true);
        when(employeeRepository.findByEmail(createEmployeeRequest.getManagerEmail())).thenReturn(managerEntity);
        when(profileRepository.findByEmail(createEmployeeRequest.getEmail())).thenReturn(profileEntity);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(employeeRepository.createEmployeeWithManager(any(), any(), any(), any(), any(), any(), any())).thenReturn(createdEmployee);

        EmployeeEntity result = hrService.createEmployee(createEmployeeRequest);
        assertNotNull(result);
        assertEquals("user123", result.getUserId());
    }

    @Test
    void createEmployee_UserAlreadyExists() {
        when(employeeRepository.existsByEmail(createEmployeeRequest.getEmail())).thenReturn(true);
        assertThrows(UserServiceException.class, () -> hrService.createEmployee(createEmployeeRequest));
    }

    @Test
    void createEmployee_ManagerNotFound() {
        when(employeeRepository.existsByEmail(createEmployeeRequest.getEmail())).thenReturn(false);
        when(employeeRepository.existsByEmail(createEmployeeRequest.getManagerEmail())).thenReturn(false);
        assertThrows(UserServiceException.class, () -> hrService.createEmployee(createEmployeeRequest));
    }


    @Test
    void updateManager_CannotAssignToSubordinate() {
        EmployeeEntity subordinate = new EmployeeEntity();
        subordinate.setUserId("newManager123");
        when(employeeRepository.findByManagerId("user123")).thenReturn(Collections.singletonList(subordinate));
        assertThrows(UserServiceException.class, () -> hrService.updateManager("user123", "newManager123"));
    }

    @Test
    void deleteEntry_EntryExists() {
        when(attendanceRepository.existsByUseridAndDateStartingWith("user123", "2023-01-01")).thenReturn(true);
        when(hrDao.deleteEntry("user123", "2023-01-01")).thenReturn("Deleted");
        assertEquals("Deleted", hrService.deleteEntry("user123", "2023-01-01"));
    }

    @Test
    void deleteEntry_EntryDoesNotExist() {
        when(attendanceRepository.existsByUseridAndDateStartingWith("user123", "2023-01-01")).thenReturn(false);
        assertThrows(UserServiceException.class, () -> hrService.deleteEntry("user123", "2023-01-01"));
    }

    @Test
    void addEntry_Success() {
        when(hrDao.addEntry("user123", "2023-01-01", "Sick Leave", "Not feeling well"))
                .thenReturn("Added");
        assertEquals("Added", hrService.addEntry("user123", "2023-01-01", "Sick Leave", "Not feeling well"));
    }

    @Test
    void updateUser_Success() {
        EmployeeUpdateRequest request = new EmployeeUpdateRequest();
        request.setUserId("user123");
        request.setRoles(List.of("Admin", "Manager"));

        when(hrDao.updateUser(request)).thenReturn("User updated successfully");

        String result = hrService.updateUser(request);

        assertEquals("User updated successfully", result);
        verify(hrDao, times(1)).updateUser(request);
    }

    @Test
    void getTotalEmployeesCount_Success() {
        when(hrDao.getTotalEmployeesCount("searchTag")).thenReturn(10L);

        long count = hrService.getTotalEmployeesCount("searchTag");

        assertEquals(10, count);
        verify(hrDao, times(1)).getTotalEmployeesCount("searchTag");
    }

    @Test
    void getAllUsers_Success() {
        Page<List<String>> mockPage = new PageImpl<>(Collections.singletonList(List.of("user1", "user2")));

        when(hrDao.getAllUsers("user123", 0, 10, "searchTag")).thenReturn(mockPage);

        Page<List<String>> result = hrService.getAllUsers("user123", 0, 10, "searchTag");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(2, result.getContent().get(0).size());
        verify(hrDao, times(1)).getAllUsers("user123", 0, 10, "searchTag");
    }


}
