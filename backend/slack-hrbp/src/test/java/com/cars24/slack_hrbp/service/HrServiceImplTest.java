//package com.cars24.slack_hrbp.service;
//
//import com.cars24.slack_hrbp.data.dao.HrDao;
//import com.cars24.slack_hrbp.data.dao.impl.HrDaoImpl;
//import com.cars24.slack_hrbp.data.dto.UserDto;
//import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
//import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
//import com.cars24.slack_hrbp.data.request.CreateEmployeeRequest;
//import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
//import com.cars24.slack_hrbp.excpetion.UserServiceException;
//import com.cars24.slack_hrbp.service.impl.HrServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.neo4j.core.Neo4jClient;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//        import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class HrServiceImplTest {
//
//    @Mock
//    private EmployeeRepository employeeRepository;
//
//    @Mock
//    private HrDaoImpl hrDao;
//
//    @Mock
//    private BCryptPasswordEncoder bCryptPasswordEncoder;
//
//
//    @InjectMocks
//    private HrServiceImpl hrService;
//
//    private EmployeeUpdateRequest employeeUpdateRequest;
//    private CreateEmployeeRequest createEmployeeRequest;
//
//    @BeforeEach
//    void setUp() {
//        createEmployeeRequest = new CreateEmployeeRequest();
//        createEmployeeRequest.setUserId("user123");
//        createEmployeeRequest.setUsername("John Doe");
//        createEmployeeRequest.setPassword("Password@123");
//        createEmployeeRequest.setEmail("johndoe@example.com");
//        createEmployeeRequest.setRoles(List.of("USER"));
//        createEmployeeRequest.setManagerId("manager123");
//
//
//        employeeUpdateRequest = new EmployeeUpdateRequest();
//        employeeUpdateRequest.setUserId("user123");
//        employeeUpdateRequest.setRoles(List.of("ADMIN", "USER"));
//
//
//    }
//
//    @Test
//    void createEmployee_Success() {
//        // Given: User doesn't exist, Manager exists
//        when(employeeRepository.findByUserId("user123")).thenReturn(Optional.empty());
//        when(employeeRepository.findByUserId("manager123")).thenReturn(Optional.of(new EmployeeEntity()));
//        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");
//
//        EmployeeEntity expectedEmployee = new EmployeeEntity();
//        expectedEmployee.setUserId("user123");
//        expectedEmployee.setUsername("John Doe");
//        expectedEmployee.setEmail("johndoe@example.com");
//
//        when(employeeRepository.createEmployeeWithManager(any(), any(), any(), any(), any(), any(), any()))
//                .thenReturn(expectedEmployee);
//
//        // When: Creating employee
//        EmployeeEntity createdEmployee = hrService.createEmployee(createEmployeeRequest);
//
//        // Then: Employee should be created successfully
//        assertNotNull(createdEmployee);
//        assertEquals("user123", createdEmployee.getUserId());
//        assertEquals("John Doe", createdEmployee.getUsername());
//        assertEquals("johndoe@example.com", createdEmployee.getEmail());
//
//        // Verify method calls
//        verify(employeeRepository, times(1)).findByUserId("user123");
//        verify(employeeRepository, times(1)).findByUserId("manager123");
//        verify(employeeRepository, times(1)).createEmployeeWithManager(
//                any(), any(), any(), any(), any(), any(), any()
//        );
//    }
//
//    @Test
//    void createEmployee_UserAlreadyExists_ShouldThrowException() {
//        // Given: User already exists
//        when(employeeRepository.findByUserId("user123")).thenReturn(Optional.of(new EmployeeEntity()));
//
//        // When & Then: Expect exception
//        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
//            hrService.createEmployee(createEmployeeRequest);
//        });
//
//        assertEquals("User already exists", exception.getMessage());
//
//        // Verify method calls
//        verify(employeeRepository, times(1)).findByUserId("user123");
//        verify(employeeRepository, never()).findByUserId("manager123");
//        verify(employeeRepository, never()).createEmployeeWithManager(any(), any(), any(), any(), any(), any(), any());
//    }
//
//    @Test
//    void createEmployee_ManagerNotFound_ShouldThrowException() {
//        // Given: User does not exist but manager does not exist either
//        when(employeeRepository.findByUserId("user123")).thenReturn(Optional.empty());
//        when(employeeRepository.findByUserId("manager123")).thenReturn(Optional.empty());
//
//        // When & Then: Expect exception
//        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
//            hrService.createEmployee(createEmployeeRequest);
//        });
//
//        assertEquals("Manager not found", exception.getMessage());
//
//        // Verify method calls
//        verify(employeeRepository, times(1)).findByUserId("user123");
//        verify(employeeRepository, times(1)).findByUserId("manager123");
//        verify(employeeRepository, never()).createEmployeeWithManager(any(), any(), any(), any(), any(), any(), any());
//    }
//
//    @Test
//    void updateUser_Success() {
//        when(hrDao.updateUser(employeeUpdateRequest)).thenReturn("User updated successfully");
//        String result = hrService.updateUser(employeeUpdateRequest);
//        assertEquals("User updated successfully", result);
//        verify(hrDao, times(1)).updateUser(employeeUpdateRequest);
//    }
//
//
//    @Test
//    void testGetAllUsers() {
//        String userId = "testUser";
//        int page = 0;
//        int limit = 10;
//        String searchTag = "Test";
//        Page<List<String>> mockPage = new PageImpl<>(Collections.singletonList(Collections.singletonList("User1")));
//
//        when(hrDao.getAllUsers(userId, page, limit, searchTag)).thenReturn(mockPage);
//        Page<List<String>> result = hrService.getAllUsers(userId, page, limit, searchTag);
//
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        verify(hrDao, times(1)).getAllUsers(userId, page, limit, searchTag);
//    }
//
//    @Test
//    void testGetTotalEmployeesCount() {
//        String searchTag = "Test";
//        when(hrDao.getTotalEmployeesCount(searchTag)).thenReturn(100L);
//
//        long count = hrService.getTotalEmployeesCount(searchTag);
//        assertEquals(100L, count);
//        verify(hrDao, times(1)).getTotalEmployeesCount(searchTag);
//    }
//
//
//
//}
