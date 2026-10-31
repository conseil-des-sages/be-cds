package com.takima.backskeleton.services;

import com.takima.backskeleton.models.Role;
import com.takima.backskeleton.models.User;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {
    private static final String SECRET = Base64.getEncoder().encodeToString(
            "01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));

    @Test
    void generatesAccessAndRefreshTokensOfTheCorrectType() {
        JwtService jwtService = new JwtService(SECRET, 60_000, 60_000);
        User user = userWithRole();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        assertTrue(jwtService.validateAccessToken(accessToken));
        assertFalse(jwtService.validateRefreshToken(accessToken));
        assertTrue(jwtService.validateRefreshToken(refreshToken));
        assertFalse(jwtService.validateAccessToken(refreshToken));
    }

    @Test
    void twoTokensIssuedInTheSameSecondAreDifferent() {
        JwtService jwtService = new JwtService(SECRET, 60_000, 60_000);
        User user = new User();
        user.setId(1);
        user.setEmailUser("gab@example.com");
        Role role = new Role();
        role.setRoleName("USER");
        user.setRole(role);

        assertNotEquals(jwtService.generateRefreshToken(user), jwtService.generateRefreshToken(user));
    }

    @Test
    void rejectsAnExpiredToken() throws InterruptedException {
        JwtService jwtService = new JwtService(SECRET, 1, 1);
        String accessToken = jwtService.generateAccessToken(userWithRole());

        Thread.sleep(1_100);

        assertFalse(jwtService.validateAccessToken(accessToken));
    }

    private User userWithRole() {
        Role role = new Role();
        role.setRoleName("USER");

        User user = new User();
        user.setId(42);
        user.setRole(role);
        return user;
    }
}
