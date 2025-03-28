package com.cars24.slack_hrbp.response;

import com.cars24.slack_hrbp.data.response.UpdateEmployeeResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UpdateEmployeeResponseTest {

    @Test
    void testSetterAndGetterMethods() {
        // Arrange
        UpdateEmployeeResponse response = new UpdateEmployeeResponse();

        // Act & Assert
        response.setUserid("user123");
        assertEquals("user123", response.getUserid());

        response.setUsername("John Doe");
        assertEquals("John Doe", response.getUsername());

        response.setManagerid("manager456");
        assertEquals("manager456", response.getManagerid());

        response.setManagername("Jane Smith");
        assertEquals("Jane Smith", response.getManagername());

        response.setRole("ADMIN");
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void testAllFieldsPopulation() {
        // Arrange
        UpdateEmployeeResponse response = new UpdateEmployeeResponse();
        response.setUserid("user789");
        response.setUsername("Alice Johnson");
        response.setManagerid("manager012");
        response.setManagername("Bob Williams");
        response.setRole("USER");

        // Assert
        assertEquals("user789", response.getUserid());
        assertEquals("Alice Johnson", response.getUsername());
        assertEquals("manager012", response.getManagerid());
        assertEquals("Bob Williams", response.getManagername());
        assertEquals("USER", response.getRole());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        UpdateEmployeeResponse response1 = new UpdateEmployeeResponse();
        response1.setUserid("user1");
        response1.setUsername("John Doe");
        response1.setManagerid("manager1");
        response1.setManagername("Jane Smith");
        response1.setRole("ADMIN");

        UpdateEmployeeResponse response2 = new UpdateEmployeeResponse();
        response2.setUserid("user1");
        response2.setUsername("John Doe");
        response2.setManagerid("manager1");
        response2.setManagername("Jane Smith");
        response2.setRole("ADMIN");

        UpdateEmployeeResponse response3 = new UpdateEmployeeResponse();
        response3.setUserid("user2");
        response3.setUsername("Alice Johnson");
        response3.setManagerid("manager2");
        response3.setManagername("Bob Williams");
        response3.setRole("USER");

        // Assert
        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        UpdateEmployeeResponse response = new UpdateEmployeeResponse();
        response.setUserid("user456");
        response.setUsername("Emma Brown");
        response.setManagerid("manager789");
        response.setManagername("Michael Davis");
        response.setRole("MANAGER");

        // Act
        String toStringResult = response.toString();

        // Assert
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("userid=user456"));
        assertTrue(toStringResult.contains("username=Emma Brown"));
        assertTrue(toStringResult.contains("managerid=manager789"));
        assertTrue(toStringResult.contains("managername=Michael Davis"));
        assertTrue(toStringResult.contains("role=MANAGER"));
    }

    @Test
    void testNullValues() {
        // Arrange
        UpdateEmployeeResponse response = new UpdateEmployeeResponse();

        // Act & Assert
        response.setUserid(null);
        assertNull(response.getUserid());

        response.setUsername(null);
        assertNull(response.getUsername());

        response.setManagerid(null);
        assertNull(response.getManagerid());

        response.setManagername(null);
        assertNull(response.getManagername());

        response.setRole(null);
        assertNull(response.getRole());
    }

    @Test
    void testEmptyStringValues() {
        // Arrange
        UpdateEmployeeResponse response = new UpdateEmployeeResponse();

        // Act & Assert
        response.setUserid("");
        assertEquals("", response.getUserid());

        response.setUsername("");
        assertEquals("", response.getUsername());

        response.setManagerid("");
        assertEquals("", response.getManagerid());

        response.setManagername("");
        assertEquals("", response.getManagername());

        response.setRole("");
        assertEquals("", response.getRole());
    }
}