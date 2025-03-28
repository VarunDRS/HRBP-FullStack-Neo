package com.cars24.slack_hrbp.response;

import com.cars24.slack_hrbp.data.response.SignUpResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SignUpResponseTest {

    @Test
    void testSetterAndGetterMethods() {
        // Arrange
        SignUpResponse signUpResponse = new SignUpResponse();

        // Act & Assert
        signUpResponse.setUserId("user123");
        assertEquals("user123", signUpResponse.getUserId());

        signUpResponse.setFirstName("John");
        assertEquals("John", signUpResponse.getFirstName());

        signUpResponse.setLastName("Doe");
        assertEquals("Doe", signUpResponse.getLastName());

        signUpResponse.setEmail("john.doe@example.com");
        assertEquals("john.doe@example.com", signUpResponse.getEmail());

        signUpResponse.setPhone("1234567890");
        assertEquals("1234567890", signUpResponse.getPhone());

        signUpResponse.setCity("New York");
        assertEquals("New York", signUpResponse.getCity());
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        // Arrange
        SignUpResponse signUpResponse = new SignUpResponse();
        signUpResponse.setUserId("user456");
        signUpResponse.setFirstName("Jane");
        signUpResponse.setLastName("Smith");
        signUpResponse.setEmail("jane.smith@example.com");
        signUpResponse.setPhone("9876543210");
        signUpResponse.setCity("San Francisco");

        // Assert
        assertEquals("user456", signUpResponse.getUserId());
        assertEquals("Jane", signUpResponse.getFirstName());
        assertEquals("Smith", signUpResponse.getLastName());
        assertEquals("jane.smith@example.com", signUpResponse.getEmail());
        assertEquals("9876543210", signUpResponse.getPhone());
        assertEquals("San Francisco", signUpResponse.getCity());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        SignUpResponse response1 = new SignUpResponse();
        response1.setUserId("user1");
        response1.setFirstName("John");
        response1.setLastName("Doe");
        response1.setEmail("john.doe@example.com");
        response1.setPhone("1234567890");
        response1.setCity("New York");

        SignUpResponse response2 = new SignUpResponse();
        response2.setUserId("user1");
        response2.setFirstName("John");
        response2.setLastName("Doe");
        response2.setEmail("john.doe@example.com");
        response2.setPhone("1234567890");
        response2.setCity("New York");

        SignUpResponse response3 = new SignUpResponse();
        response3.setUserId("user2");
        response3.setFirstName("Jane");
        response3.setLastName("Smith");
        response3.setEmail("jane.smith@example.com");
        response3.setPhone("9876543210");
        response3.setCity("San Francisco");

        // Assert
        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        SignUpResponse signUpResponse = new SignUpResponse();
        signUpResponse.setUserId("user789");
        signUpResponse.setFirstName("Alice");
        signUpResponse.setLastName("Johnson");
        signUpResponse.setEmail("alice.johnson@example.com");
        signUpResponse.setPhone("5555555555");
        signUpResponse.setCity("Chicago");

        // Act
        String toStringResult = signUpResponse.toString();

        // Assert
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("userId=user789"));
        assertTrue(toStringResult.contains("firstName=Alice"));
        assertTrue(toStringResult.contains("lastName=Johnson"));
        assertTrue(toStringResult.contains("email=alice.johnson@example.com"));
        assertTrue(toStringResult.contains("phone=5555555555"));
        assertTrue(toStringResult.contains("city=Chicago"));
    }

    @Test
    void testNullValues() {
        // Arrange
        SignUpResponse signUpResponse = new SignUpResponse();

        // Act & Assert
        signUpResponse.setUserId(null);
        assertNull(signUpResponse.getUserId());

        signUpResponse.setFirstName(null);
        assertNull(signUpResponse.getFirstName());

        signUpResponse.setLastName(null);
        assertNull(signUpResponse.getLastName());

        signUpResponse.setEmail(null);
        assertNull(signUpResponse.getEmail());

        signUpResponse.setPhone(null);
        assertNull(signUpResponse.getPhone());

        signUpResponse.setCity(null);
        assertNull(signUpResponse.getCity());
    }
}