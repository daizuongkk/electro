package com.daizuongkk.web.service;

import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.model.Role;
import com.daizuongkk.web.model.User;
import com.daizuongkk.web.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository);
    }

    @Test
    void loginShouldReturnUserResponseWhenCredentialsValid() {
        User storedUser = User.builder()
                .id(10L)
                .username("tester01")
                .email("tester@example.com")
                .password(BCrypt.hashpw("Password@123", BCrypt.gensalt()))
                .role(Role.CUSTOMER)
                .verified(true)
                .status("ACTIVE")
                .build();

        when(userRepository.findByUsernameOrEmail("tester01")).thenReturn(storedUser);

        UserResponse response = authService.login("tester01", "Password@123");

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("tester01", response.getUsername());
        assertEquals("tester@example.com", response.getEmail());
    }

    @Test
    void loginShouldReturnNullWhenPasswordInvalid() {
        User storedUser = User.builder()
                .username("tester01")
                .password(BCrypt.hashpw("Password@123", BCrypt.gensalt()))
                .build();

        when(userRepository.findByUsernameOrEmail("tester01")).thenReturn(storedUser);

        assertNull(authService.login("tester01", "WrongPass@123"));
    }

    @Test
    void registerShouldReturnInvalidEmailFormatWhenEmailInvalid() {
        AuthService.RegisterStatus status = authService.register("Tester_01", "invalid-mail", "Password@123");

        assertEquals(AuthService.RegisterStatus.INVALID_EMAIL_FORMAT, status);
    }

    @Test
    void registerShouldHashPasswordAndCreateUserWhenInputValid() {
        when(userRepository.existsByUsername("Tester_01")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.create(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(true);

        AuthService.RegisterStatus status = authService.register("Tester_01", "TEST@EXAMPLE.COM", "Password@123");

        assertEquals(AuthService.RegisterStatus.SUCCESS, status);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).create(captor.capture());
        User created = captor.getValue();

        assertEquals("Tester_01", created.getUsername());
        assertEquals("test@example.com", created.getEmail());
        assertEquals(Role.CUSTOMER, created.getRole());
        assertNotEquals("Password@123", created.getPassword());
        assertTrue(BCrypt.checkpw("Password@123", created.getPassword()));
    }
}

