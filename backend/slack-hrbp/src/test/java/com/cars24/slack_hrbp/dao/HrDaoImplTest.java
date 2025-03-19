package com.cars24.slack_hrbp.dao;

import com.cars24.slack_hrbp.data.dao.impl.HrDaoImpl;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.request.CreateEmployeeRequest;
import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
import com.cars24.slack_hrbp.excpetion.UserServiceException;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HrDaoImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private Neo4jClient neo4jClient;

    @InjectMocks
    private HrDaoImpl hrDao;

    private EmployeeUpdateRequest employeeUpdateRequest;
    private CreateEmployeeRequest createEmployeeRequest;

    @BeforeEach
    void setUp() {
        createEmployeeRequest = new CreateEmployeeRequest();
        createEmployeeRequest.setUserId("user123");
        createEmployeeRequest.setUsername("John Doe");
        createEmployeeRequest.setPassword("Password@123");
        createEmployeeRequest.setEmail("johndoe@example.com");
        createEmployeeRequest.setRoles(List.of("USER"));
        createEmployeeRequest.setManagerId("manager123");


        employeeUpdateRequest = new EmployeeUpdateRequest();
        employeeUpdateRequest.setUserId("user123");
        employeeUpdateRequest.setRoles(List.of("ADMIN", "USER"));

    }

//    @Test
//    void testUpdateUser_Success() {
//        EmployeeUpdateRequest request = new EmployeeUpdateRequest();
//
//        EmployeeEntity employee = new EmployeeEntity();
//        when(employeeRepository.findByUserId("testUser")).thenReturn(Optional.of(employee));
//        doAnswer(invocation -> null).when(employeeRepository).updateEmployeeRoles("testUser", request.getRoles());
//
//        String result = hrDao.updateUser(request);
//        assertEquals("Update was successful", result);
//        verify(employeeRepository, times(1)).updateEmployeeRoles("testUser", request.getRoles());
//    }

//    @Test
//    void testUpdateUser_EmployeeNotFound() {
//        EmployeeUpdateRequest request = new EmployeeUpdateRequest();
//
//        when(employeeRepository.findByUserId("nonExistingUser")).thenReturn(Optional.empty());
//        assertThrows(UserServiceException.class, () -> hrDao.updateUser(request));
//    }

//    @Test
//    void testGetAllUsers_WithSearchTag() {
//        String userId = "testUser";
//        int page = 0;
//        int limit = 10;
//        String searchTag = "Test";
//        List<List<String>> mockResults = List.of(List.of("User1", "email@example.com", "Username"));
//        Page<List<String>> mockPage = new PageImpl<>(mockResults, PageRequest.of(page, limit), mockResults.size());
//
//        when(neo4jClient.query(anyString())).thenReturn(mock(Neo4jClient.OngoingBind.class));
//        when(neo4jClient.query(anyString()).fetch().all()).thenReturn(mockResults);
//
//        Page<List<String>> result = hrDao.getAllUsers(userId, page, limit, searchTag);
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//    }

    @Test
    void testGetTotalEmployeesCount_WithSearchTag() {
        String searchTag = "Test";
        when(employeeRepository.countBySearchtag(searchTag)).thenReturn(50L);

        long count = hrDao.getTotalEmployeesCount(searchTag);
        assertEquals(50L, count);
    }

    @Test
    void testGetTotalEmployeesCount_WithoutSearchTag() {
        when(employeeRepository.count()).thenReturn(200L);

        long count = hrDao.getTotalEmployeesCount(null);
        assertEquals(200L, count);
    }
}
