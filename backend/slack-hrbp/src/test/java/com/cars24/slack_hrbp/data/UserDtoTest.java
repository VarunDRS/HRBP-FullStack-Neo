package com.cars24.slack_hrbp.data;

import com.cars24.slack_hrbp.data.dto.UserDto;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UserDtoTest {

    @Test
    void testSetterAndGetterMethods() {
        // Arrange
        UserDto userDto = new UserDto();

        // Act & Assert
        userDto.setUserId("user123");
        assertEquals("user123", userDto.getUserId());

        userDto.setFirstName("John");
        assertEquals("John", userDto.getFirstName());

        userDto.setLastName("Doe");
        assertEquals("Doe", userDto.getLastName());

        userDto.setEmail("john.doe@example.com");
        assertEquals("john.doe@example.com", userDto.getEmail());

        userDto.setPassword("securePassword123");
        assertEquals("securePassword123", userDto.getPassword());

        userDto.setCity("New York");
        assertEquals("New York", userDto.getCity());

        userDto.setPhone("1234567890");
        assertEquals("1234567890", userDto.getPhone());

        userDto.setEmailVerificationStatus(true);
        assertTrue(userDto.isEmailVerificationStatus());

        List<String> roles = Arrays.asList("ADMIN", "USER");
        userDto.setRoles(roles);
        assertEquals(roles, userDto.getRoles());
    }

    @Test
    void testAllFieldsPopulation() {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setUserId("user456");
        userDto.setFirstName("Jane");
        userDto.setLastName("Smith");
        userDto.setEmail("jane.smith@example.com");
        userDto.setPassword("anotherSecurePassword");
        userDto.setCity("San Francisco");
        userDto.setPhone("9876543210");
        userDto.setEmailVerificationStatus(false);
        List<String> roles = Collections.singletonList("USER");
        userDto.setRoles(roles);

        // Assert
        assertEquals("user456", userDto.getUserId());
        assertEquals("Jane", userDto.getFirstName());
        assertEquals("Smith", userDto.getLastName());
        assertEquals("jane.smith@example.com", userDto.getEmail());
        assertEquals("anotherSecurePassword", userDto.getPassword());
        assertEquals("San Francisco", userDto.getCity());
        assertEquals("9876543210", userDto.getPhone());
        assertFalse(userDto.isEmailVerificationStatus());
        assertEquals(roles, userDto.getRoles());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        UserDto userDto1 = new UserDto();
        userDto1.setUserId("user1");
        userDto1.setFirstName("John");
        userDto1.setLastName("Doe");
        userDto1.setEmail("john.doe@example.com");
        userDto1.setPassword("password1");
        userDto1.setCity("New York");
        userDto1.setPhone("1234567890");
        userDto1.setEmailVerificationStatus(true);
        userDto1.setRoles(Arrays.asList("ADMIN", "USER"));

        UserDto userDto2 = new UserDto();
        userDto2.setUserId("user1");
        userDto2.setFirstName("John");
        userDto2.setLastName("Doe");
        userDto2.setEmail("john.doe@example.com");
        userDto2.setPassword("password1");
        userDto2.setCity("New York");
        userDto2.setPhone("1234567890");
        userDto2.setEmailVerificationStatus(true);
        userDto2.setRoles(Arrays.asList("ADMIN", "USER"));

        UserDto userDto3 = new UserDto();
        userDto3.setUserId("user2");
        userDto3.setFirstName("Jane");
        userDto3.setLastName("Smith");
        userDto3.setEmail("jane.smith@example.com");
        userDto3.setPassword("password2");
        userDto3.setCity("San Francisco");
        userDto3.setPhone("9876543210");
        userDto3.setEmailVerificationStatus(false);
        userDto3.setRoles(Collections.singletonList("USER"));

        // Assert
        assertEquals(userDto1, userDto2);
        assertNotEquals(userDto1, userDto3);
        assertEquals(userDto1.hashCode(), userDto2.hashCode());
        assertNotEquals(userDto1.hashCode(), userDto3.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setUserId("user789");
        userDto.setFirstName("Alice");
        userDto.setLastName("Johnson");
        userDto.setEmail("alice.johnson@example.com");
        userDto.setPassword("securePass");
        userDto.setCity("Chicago");
        userDto.setPhone("5555555555");
        userDto.setEmailVerificationStatus(true);
        userDto.setRoles(List.of("MANAGER"));

        // Act
        String toStringResult = userDto.toString();

        // Assert
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("userId=user789"));
        assertTrue(toStringResult.contains("firstName=Alice"));
        assertTrue(toStringResult.contains("lastName=Johnson"));
        assertTrue(toStringResult.contains("email=alice.johnson@example.com"));
        assertTrue(toStringResult.contains("password=securePass"));
        assertTrue(toStringResult.contains("city=Chicago"));
        assertTrue(toStringResult.contains("phone=5555555555"));
        assertTrue(toStringResult.contains("emailVerificationStatus=true"));
        assertTrue(toStringResult.contains("roles=[MANAGER]"));
    }

    @Test
    void testNullValues() {
        // Arrange
        UserDto userDto = new UserDto();

        // Act & Assert
        userDto.setUserId(null);
        assertNull(userDto.getUserId());

        userDto.setFirstName(null);
        assertNull(userDto.getFirstName());

        userDto.setLastName(null);
        assertNull(userDto.getLastName());

        userDto.setEmail(null);
        assertNull(userDto.getEmail());

        userDto.setPassword(null);
        assertNull(userDto.getPassword());

        userDto.setCity(null);
        assertNull(userDto.getCity());

        userDto.setPhone(null);
        assertNull(userDto.getPhone());

        userDto.setRoles(null);
        assertNull(userDto.getRoles());
    }

    @Test
    void testEmptyListAndDefaultBooleanValues() {
        // Arrange
        UserDto userDto = new UserDto();

        // Act & Assert
        userDto.setRoles(Collections.emptyList());
        assertTrue(userDto.getRoles().isEmpty());

        // Default boolean value
        assertFalse(userDto.isEmailVerificationStatus());
    }
}