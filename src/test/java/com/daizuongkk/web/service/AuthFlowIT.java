package com.daizuongkk.web.service;

import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.model.User;
import com.daizuongkk.web.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthFlowIT {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(new InMemoryUserRepository());
    }

    @Test
    void registerThenLoginShouldSucceed() {
        AuthService.RegisterStatus registerStatus = authService.register(
                "Tester_01",
                "TESTER@EXAMPLE.COM",
                "Password@123"
        );

        assertEquals(AuthService.RegisterStatus.SUCCESS, registerStatus);

        UserResponse login = authService.login("Tester_01", "Password@123");

        assertNotNull(login);
        assertEquals("Tester_01", login.getUsername());
        assertEquals("tester@example.com", login.getEmail());
    }

    @Test
    void registerShouldRejectDuplicateUsernameAndEmail() {
        assertEquals(
                AuthService.RegisterStatus.SUCCESS,
                authService.register("Tester_01", "tester@example.com", "Password@123")
        );

        assertEquals(
                AuthService.RegisterStatus.USERNAME_EXISTS,
                authService.register("Tester_01", "new@example.com", "Password@123")
        );

        assertEquals(
                AuthService.RegisterStatus.EMAIL_EXISTS,
                authService.register("Tester_02", "tester@example.com", "Password@123")
        );
    }

    private static class InMemoryUserRepository extends UserRepository {
        private final Map<String, User> byUsername = new HashMap<>();
        private final Map<String, User> byEmail = new HashMap<>();
        private long currentId = 0L;

        @Override
        public User findByUsernameOrEmail(String username) {
            if (username == null) {
                return null;
            }
            User found = byUsername.get(username.trim());
            if (found != null) {
                return found;
            }
            return byEmail.get(username.trim().toLowerCase());
        }

        @Override
        public boolean existsByUsername(String username) {
            return username != null && byUsername.containsKey(username.trim());
        }

        @Override
        public boolean existsByEmail(String email) {
            return email != null && byEmail.containsKey(email.trim().toLowerCase());
        }

        @Override
        public boolean create(User user) {
            if (user == null) {
                return false;
            }
            currentId++;
            user.setId(currentId);
            byUsername.put(user.getUsername(), user);
            byEmail.put(user.getEmail(), user);
            return true;
        }
    }
}

