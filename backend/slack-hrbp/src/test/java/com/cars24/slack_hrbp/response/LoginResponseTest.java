package com.cars24.slack_hrbp.response;

import com.cars24.slack_hrbp.data.response.LoginResponse;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LoginResponseTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String expectedToken = "sample_token_123";
        String expectedUserId = "user_456";
        List<String> expectedRoles = Arrays.asList("ADMIN", "USER");

        // Act
        LoginResponse loginResponse = new LoginResponse(expectedToken, expectedUserId, expectedRoles);

        // Assert
        assertEquals(expectedToken, loginResponse.getToken());
        assertEquals(expectedUserId, loginResponse.getUserId());
        assertEquals(expectedRoles, loginResponse.getRoles());
    }

    @Test
    void testEmptyRolesList() {
        // Arrange
        String token = "empty_roles_token";
        String userId = "user_789";
        List<String> emptyRoles = Arrays.asList();

        // Act
        LoginResponse loginResponse = new LoginResponse(token, userId, emptyRoles);

        // Assert
        assertEquals(token, loginResponse.getToken());
        assertEquals(userId, loginResponse.getUserId());
        assertTrue(loginResponse.getRoles().isEmpty());
    }

    @Test
    void testSingleRoleList() {
        // Arrange
        String token = "single_role_token";
        String userId = "user_012";
        List<String> singleRole = Arrays.asList("GUEST");

        // Act
        LoginResponse loginResponse = new LoginResponse(token, userId, singleRole);

        // Assert
        assertEquals(token, loginResponse.getToken());
        assertEquals(userId, loginResponse.getUserId());
        assertEquals(1, loginResponse.getRoles().size());
        assertEquals("GUEST", loginResponse.getRoles().get(0));
    }

    @Test
    void testNullValues() {
        // Arrange
        String nullToken = null;
        String nullUserId = null;
        List<String> nullRoles = null;

        // Act
        LoginResponse loginResponse = new LoginResponse(nullToken, nullUserId, nullRoles);

        // Assert
        assertNull(loginResponse.getToken());
        assertNull(loginResponse.getUserId());
        assertNull(loginResponse.getRoles());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        List<String> roles = Arrays.asList("ADMIN", "USER");
        LoginResponse response1 = new LoginResponse("token1", "user1", roles);
        LoginResponse response2 = new LoginResponse("token1", "user1", roles);
        LoginResponse response3 = new LoginResponse("token2", "user2", Arrays.asList("GUEST"));

        // Assert
        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        String token = "test_token";
        String userId = "test_user";
        List<String> roles = Arrays.asList("ADMIN", "USER");
        LoginResponse loginResponse = new LoginResponse(token, userId, roles);

        // Act
        String toStringResult = loginResponse.toString();

        // Assert
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("token=" + token));
        assertTrue(toStringResult.contains("userId=" + userId));
        assertTrue(toStringResult.contains("roles=" + roles));
    }
}