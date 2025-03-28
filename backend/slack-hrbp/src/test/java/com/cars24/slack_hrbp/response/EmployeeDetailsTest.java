package com.cars24.slack_hrbp.response;

import com.cars24.slack_hrbp.data.response.EmployeeDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeDetailsTest {
    private EmployeeDetails employeeDetails;

    @BeforeEach
    void setUp() {
        employeeDetails = new EmployeeDetails();
    }

    @Test
    void testSetAndGetDate() {
        String testDate = "2024-03-28";
        employeeDetails.setDate(testDate);
        assertEquals(testDate, employeeDetails.getDate());
    }

    @Test
    void testSetAndGetUsername() {
        String testUsername = "john.doe";
        employeeDetails.setUsername(testUsername);
        assertEquals(testUsername, employeeDetails.getUsername());
    }

    @Test
    void testSetAndGetReason() {
        String testReason = "Performance Review";
        employeeDetails.setReason(testReason);
        assertEquals(testReason, employeeDetails.getReason());
    }

    @Test
    void testSetAndGetType() {
        String testType = "HR Event";
        employeeDetails.setType(testType);
        assertEquals(testType, employeeDetails.getType());
    }

    @Test
    void testFullConstructorAndGetters() {
        // Create a fully populated EmployeeDetails
        EmployeeDetails fullDetails = new EmployeeDetails();
        fullDetails.setDate("2024-03-28");
        fullDetails.setUsername("john.doe");
        fullDetails.setReason("Performance Review");
        fullDetails.setType("HR Event");

        // Verify all getters
        assertEquals("2024-03-28", fullDetails.getDate());
        assertEquals("john.doe", fullDetails.getUsername());
        assertEquals("Performance Review", fullDetails.getReason());
        assertEquals("HR Event", fullDetails.getType());
    }

    @Test
    void testToString() {
        // Verify that toString() is generated by Lombok
        EmployeeDetails fullDetails = new EmployeeDetails();
        fullDetails.setDate("2024-03-28");
        fullDetails.setUsername("john.doe");
        fullDetails.setReason("Performance Review");
        fullDetails.setType("HR Event");

        String toStringResult = fullDetails.toString();
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("date=2024-03-28"));
        assertTrue(toStringResult.contains("username=john.doe"));
        assertTrue(toStringResult.contains("reason=Performance Review"));
        assertTrue(toStringResult.contains("type=HR Event"));
    }

    @Test
    void testEqualsAndHashCode() {
        EmployeeDetails details1 = new EmployeeDetails();
        details1.setDate("2024-03-28");
        details1.setUsername("john.doe");
        details1.setReason("Performance Review");
        details1.setType("HR Event");

        EmployeeDetails details2 = new EmployeeDetails();
        details2.setDate("2024-03-28");
        details2.setUsername("john.doe");
        details2.setReason("Performance Review");
        details2.setType("HR Event");

        EmployeeDetails details3 = new EmployeeDetails();
        details3.setDate("2024-03-29");
        details3.setUsername("jane.smith");
        details3.setReason("Different Reason");
        details3.setType("Different Type");

        // Test equals
        assertEquals(details1, details2);
        assertNotEquals(details1, details3);

        // Test hashCode
        assertEquals(details1.hashCode(), details2.hashCode());
        assertNotEquals(details1.hashCode(), details3.hashCode());
    }

    @Test
    void testNullAndEmptyValues() {
        // Test setting null values
        employeeDetails.setDate(null);
        assertNull(employeeDetails.getDate());

        employeeDetails.setUsername(null);
        assertNull(employeeDetails.getUsername());

        employeeDetails.setReason(null);
        assertNull(employeeDetails.getReason());

        employeeDetails.setType(null);
        assertNull(employeeDetails.getType());

        // Test setting empty values
        employeeDetails.setDate("");
        assertEquals("", employeeDetails.getDate());

        employeeDetails.setUsername("");
        assertEquals("", employeeDetails.getUsername());

        employeeDetails.setReason("");
        assertEquals("", employeeDetails.getReason());

        employeeDetails.setType("");
        assertEquals("", employeeDetails.getType());
    }
}