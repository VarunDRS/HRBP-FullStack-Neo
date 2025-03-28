package com.cars24.slack_hrbp.request;

import com.cars24.slack_hrbp.data.request.SignUpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SignUpRequestTest {
    private SignUpRequest signUpRequest;

    @BeforeEach
    void setUp() {
        signUpRequest = new SignUpRequest();
    }

    @Test
    void testSetAndGetFirstName() {
        String testFirstName = "John";
        signUpRequest.setFirstName(testFirstName);
        assertEquals(testFirstName, signUpRequest.getFirstName());
    }

    @Test
    void testSetAndGetLastName() {
        String testLastName = "Doe";
        signUpRequest.setLastName(testLastName);
        assertEquals(testLastName, signUpRequest.getLastName());
    }

    @Test
    void testSetAndGetEmail() {
        String testEmail = "john.doe@example.com";
        signUpRequest.setEmail(testEmail);
        assertEquals(testEmail, signUpRequest.getEmail());
    }

    @Test
    void testSetAndGetCity() {
        String testCity = "New York";
        signUpRequest.setCity(testCity);
        assertEquals(testCity, signUpRequest.getCity());
    }

    @Test
    void testSetAndGetPhone() {
        String testPhone = "+1234567890";
        signUpRequest.setPhone(testPhone);
        assertEquals(testPhone, signUpRequest.getPhone());
    }

    @Test
    void testSetAndGetPassword() {
        String testPassword = "securePassword123";
        signUpRequest.setPassword(testPassword);
        assertEquals(testPassword, signUpRequest.getPassword());
    }

    @Test
    void testSetAndGetRoles() {
        List<String> testRoles = Arrays.asList("USER", "ADMIN");
        signUpRequest.setRoles(testRoles);
        assertEquals(testRoles, signUpRequest.getRoles());
    }

    @Test
    void testFullConstructorAndGetters() {
        // Create a fully populated SignUpRequest
        SignUpRequest fullRequest = new SignUpRequest();
        fullRequest.setFirstName("John");
        fullRequest.setLastName("Doe");
        fullRequest.setEmail("john.doe@example.com");
        fullRequest.setCity("New York");
        fullRequest.setPhone("+1234567890");
        fullRequest.setPassword("securePassword123");
        fullRequest.setRoles(Arrays.asList("USER", "ADMIN"));

        // Verify all getters
        assertEquals("John", fullRequest.getFirstName());
        assertEquals("Doe", fullRequest.getLastName());
        assertEquals("john.doe@example.com", fullRequest.getEmail());
        assertEquals("New York", fullRequest.getCity());
        assertEquals("+1234567890", fullRequest.getPhone());
        assertEquals("securePassword123", fullRequest.getPassword());
        assertEquals(Arrays.asList("USER", "ADMIN"), fullRequest.getRoles());
    }

    @Test
    void testToString() {
        // Verify that toString() is generated by Lombok
        SignUpRequest fullRequest = new SignUpRequest();
        fullRequest.setFirstName("John");
        fullRequest.setLastName("Doe");
        fullRequest.setEmail("john.doe@example.com");
        fullRequest.setCity("New York");
        fullRequest.setPhone("+1234567890");
        fullRequest.setPassword("securePassword123");
        fullRequest.setRoles(Arrays.asList("USER", "ADMIN"));

        String toStringResult = fullRequest.toString();
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("firstName=John"));
        assertTrue(toStringResult.contains("lastName=Doe"));
        assertTrue(toStringResult.contains("email=john.doe@example.com"));
        assertTrue(toStringResult.contains("city=New York"));
        assertTrue(toStringResult.contains("phone=+1234567890"));
        assertTrue(toStringResult.contains("password=securePassword123"));
        assertTrue(toStringResult.contains("roles=[USER, ADMIN]"));
    }

    @Test
    void testEqualsAndHashCode() {
        SignUpRequest request1 = new SignUpRequest();
        request1.setFirstName("John");
        request1.setLastName("Doe");
        request1.setEmail("john.doe@example.com");
        request1.setCity("New York");
        request1.setPhone("+1234567890");
        request1.setPassword("securePassword123");
        request1.setRoles(Arrays.asList("USER", "ADMIN"));

        SignUpRequest request2 = new SignUpRequest();
        request2.setFirstName("John");
        request2.setLastName("Doe");
        request2.setEmail("john.doe@example.com");
        request2.setCity("New York");
        request2.setPhone("+1234567890");
        request2.setPassword("securePassword123");
        request2.setRoles(Arrays.asList("USER", "ADMIN"));

        SignUpRequest request3 = new SignUpRequest();
        request3.setFirstName("Jane");
        request3.setEmail("jane.doe@example.com");

        // Test equals
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);

        // Test hashCode
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1.hashCode(), request3.hashCode());
    }


    @Test
    void testNullAndEmptyValues() {
        // Test setting null values
        signUpRequest.setFirstName(null);
        assertNull(signUpRequest.getFirstName());

        signUpRequest.setLastName(null);
        assertNull(signUpRequest.getLastName());

        signUpRequest.setEmail(null);
        assertNull(signUpRequest.getEmail());

        signUpRequest.setCity(null);
        assertNull(signUpRequest.getCity());

        signUpRequest.setPhone(null);
        assertNull(signUpRequest.getPhone());

        signUpRequest.setPassword(null);
        assertNull(signUpRequest.getPassword());

        signUpRequest.setRoles(null);
        assertNull(signUpRequest.getRoles());

        // Test setting empty values
        signUpRequest.setFirstName("");
        assertEquals("", signUpRequest.getFirstName());

        signUpRequest.setLastName("");
        assertEquals("", signUpRequest.getLastName());

        signUpRequest.setEmail("");
        assertEquals("", signUpRequest.getEmail());

        signUpRequest.setCity("");
        assertEquals("", signUpRequest.getCity());

        signUpRequest.setPhone("");
        assertEquals("", signUpRequest.getPhone());

        signUpRequest.setPassword("");
        assertEquals("", signUpRequest.getPassword());

        signUpRequest.setRoles(List.of());
        assertTrue(signUpRequest.getRoles().isEmpty());
    }
}