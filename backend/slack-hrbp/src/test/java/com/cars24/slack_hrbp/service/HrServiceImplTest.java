package com.cars24.slack_hrbp.service;

import com.cars24.slack_hrbp.data.dao.impl.HrDaoImpl;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.entity.ProfileEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.repository.ProfileRepository;
import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
import com.cars24.slack_hrbp.data.request.CreateEmployeeRequest;
import com.cars24.slack_hrbp.excpetion.UserServiceException;
import com.cars24.slack_hrbp.service.impl.HrServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HrServiceImplTest {

    @Mock
    private HrDaoImpl hrDao;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private Neo4jClient neo4jClient;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private HrServiceImpl hrService;

    @BeforeEach
    void setUp() {
        hrService = new HrServiceImpl(bCryptPasswordEncoder, hrDao, neo4jClient, employeeRepository, null, attendanceRepository, profileRepository);
    }

    @Test
    void testUpdateUser_Success() {
        EmployeeUpdateRequest request = new EmployeeUpdateRequest();
        when(hrDao.updateUser(request)).thenReturn("Update was successful");

        String result = hrService.updateUser(request);

        assertEquals("Update was successful", result);
        verify(hrDao).updateUser(request);
    }

    @Test
    void testGetTotalEmployeesCount() {
        when(hrDao.getTotalEmployeesCount("test")).thenReturn(10L);
        assertEquals(10L, hrService.getTotalEmployeesCount("test"));
    }

    @Test
    void testDeleteEntry_Success() {
        when(attendanceRepository.existsByUseridAndDateStartingWith("123", "2024-03-27")).thenReturn(true);
        when(hrDao.deleteEntry("123", "2024-03-27")).thenReturn("Delete of attendance successful");

        String result = hrService.deleteEntry("123", "2024-03-27");

        assertEquals("Delete of attendance successful", result);
        verify(hrDao).deleteEntry("123", "2024-03-27");
    }

    @Test
    void testDeleteEntry_ThrowsException() {
        when(attendanceRepository.existsByUseridAndDateStartingWith("123", "2024-03-27")).thenReturn(false);

        assertThrows(UserServiceException.class, () -> hrService.deleteEntry("123", "2024-03-27"));
    }

    @Test
    void testAddEntry_Success() {
        when(hrDao.addEntry("123", "2024-03-27", "Sick Leave", "Flu"))
                .thenReturn("Adding of attendance successful");

        String result = hrService.addEntry("123", "2024-03-27", "Sick Leave", "Flu");

        assertEquals("Adding of attendance successful", result);
        verify(hrDao).addEntry("123", "2024-03-27", "Sick Leave", "Flu");
    }
}
