package com.cars24.slack_hrbp.response;

import com.cars24.slack_hrbp.data.response.GetUserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GetUserResponseTest {
    private GetUserResponse getUserResponse;

    @BeforeEach
    void setUp() {
        getUserResponse = new GetUserResponse();
    }

    @Test
    void testSetAndGetUserId() {
        String testUserId = "user123";
        getUserResponse.setUserId(testUserId);
        assertEquals(testUserId, getUserResponse.getUserId());
    }

    @Test
    void testSetAndGetUsername() {
        String testUsername = "john.doe";
        getUserResponse.setUsername(testUsername);
        assertEquals(testUsername, getUserResponse.getUsername());
    }

    @Test
    void testSetAndGetEmail() {
        String testEmail = "john.doe@example.com";
        getUserResponse.setEmail(testEmail);
        assertEquals(testEmail, getUserResponse.getEmail());
    }

    @Test
    void testFullConstructorAndGetters() {
        // Create a fully populated GetUserResponse
        GetUserResponse fullResponse = new GetUserResponse();
        fullResponse.setUserId("user123");
        fullResponse.setUsername("john.doe");
        fullResponse.setEmail("john.doe@example.com");

        // Verify all getters
        assertEquals("user123", fullResponse.getUserId());
        assertEquals("john.doe", fullResponse.getUsername());
        assertEquals("john.doe@example.com", fullResponse.getEmail());
    }

    @Test
    void testToString() {
        // Verify that toString() is generated by Lombok
        GetUserResponse fullResponse = new GetUserResponse();
        fullResponse.setUserId("user123");
        fullResponse.setUsername("john.doe");
        fullResponse.setEmail("john.doe@example.com");

        String toStringResult = fullResponse.toString();
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("userId=user123"));
        assertTrue(toStringResult.contains("username=john.doe"));
        assertTrue(toStringResult.contains("email=john.doe@example.com"));
    }

    @Test
    void testEqualsAndHashCode() {
        GetUserResponse response1 = new GetUserResponse();
        response1.setUserId("user123");
        response1.setUsername("john.doe");
        response1.setEmail("john.doe@example.com");

        GetUserResponse response2 = new GetUserResponse();
        response2.setUserId("user123");
        response2.setUsername("john.doe");
        response2.setEmail("john.doe@example.com");

        GetUserResponse response3 = new GetUserResponse();
        response3.setUserId("user456");
        response3.setUsername("jane.smith");
        response3.setEmail("jane.smith@example.com");

        // Test equals
        assertEquals(response1, response2);
        assertNotEquals(response1, response3);

        // Test hashCode
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }

    @Test
    void testNullAndEmptyValues() {
        // Test setting null values
        getUserResponse.setUserId(null);
        assertNull(getUserResponse.getUserId());

        getUserResponse.setUsername(null);
        assertNull(getUserResponse.getUsername());

        getUserResponse.setEmail(null);
        assertNull(getUserResponse.getEmail());

        // Test setting empty values
        getUserResponse.setUserId("");
        assertEquals("", getUserResponse.getUserId());

        getUserResponse.setUsername("");
        assertEquals("", getUserResponse.getUsername());

        getUserResponse.setEmail("");
        assertEquals("", getUserResponse.getEmail());
    }
}