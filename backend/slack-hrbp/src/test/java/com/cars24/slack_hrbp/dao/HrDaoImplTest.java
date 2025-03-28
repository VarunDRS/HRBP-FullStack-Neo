package com.cars24.slack_hrbp.dao;

import com.cars24.slack_hrbp.data.dao.impl.HrDaoImpl;
import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
import com.cars24.slack_hrbp.excpetion.UserServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HrDaoImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private Neo4jClient neo4jClient;

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private HrDaoImpl hrDao;

    @BeforeEach
    void setUp() {
        hrDao = new HrDaoImpl(employeeRepository, bCryptPasswordEncoder, neo4jClient, attendanceRepository);
    }

    @Test
    void testUpdateUser_Success() {
        EmployeeUpdateRequest request = new EmployeeUpdateRequest();
        request.setUserId("123");
        request.setRoles(List.of("ROLE_USER"));

        EmployeeEntity employee = new EmployeeEntity();
        when(employeeRepository.findByUserId("123")).thenReturn(Optional.of(employee));

        String result = hrDao.updateUser(request);

        assertEquals("Update was successful", result);
        verify(employeeRepository).updateEmployeeRoles("123", List.of("ROLE_USER"));
    }

    @Test
    void testUpdateUser_ThrowsException() {
        EmployeeUpdateRequest request = new EmployeeUpdateRequest();
        request.setUserId("123");

        when(employeeRepository.findByUserId("123")).thenReturn(Optional.empty());

        assertThrows(UserServiceException.class, () -> hrDao.updateUser(request));
    }

    @Test
    void testGetTotalEmployeesCount_NoSearchTag() {
        when(employeeRepository.count()).thenReturn(10L);
        assertEquals(10L, hrDao.getTotalEmployeesCount(null));
    }

    @Test
    void testGetTotalEmployeesCount_WithSearchTag() {
        when(employeeRepository.countBySearchtag("test"))
                .thenReturn(5L);
        assertEquals(5L, hrDao.getTotalEmployeesCount("test"));
    }

    @Test
    void testDeleteEntry_Success() {
        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setId(String.valueOf(1L));

        when(attendanceRepository.findByUseridAndDateStartingWith("123", "2024-03-27"))
                .thenReturn(List.of(attendance));

        String result = hrDao.deleteEntry("123", "2024-03-27");

        assertEquals("Delete of attendance successfull", result);
        verify(attendanceRepository).deleteById(String.valueOf(1L));
    }

    @Test
    void testAddEntry_Success() {
        EmployeeEntity employee = new EmployeeEntity();
        employee.setUsername("JohnDoe");
        when(employeeRepository.findByUserId("123"))
                .thenReturn(Optional.of(employee));

        String result = hrDao.addEntry("123", "2024-03-27", "Sick Leave", "Flu");

        assertEquals("Adding of attendance successfull", result);
        verify(attendanceRepository, times(1)).save(any(AttendanceEntity.class));
    }

    @Test
    void testEquals() {
        HrDaoImpl hrDao1 = new HrDaoImpl(employeeRepository, bCryptPasswordEncoder, neo4jClient, attendanceRepository);
        HrDaoImpl hrDao2 = new HrDaoImpl(employeeRepository, bCryptPasswordEncoder, neo4jClient, attendanceRepository);

        assertEquals(hrDao1, hrDao2); // This internally calls equals()
    }


    @Test
    void testToString() {
        HrDaoImpl hrDaoInstance = new HrDaoImpl(employeeRepository, bCryptPasswordEncoder, neo4jClient, attendanceRepository);

        String result = hrDaoInstance.toString();
        assertNotNull(result);
        assertTrue(result.contains("HrDaoImpl"));
    }


}
