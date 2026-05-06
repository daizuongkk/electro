package com.daizuongkk.web.controller;

import com.daizuongkk.web.service.AuthService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

class RegisterControllerTest {

    private RegisterController controller;
    private AuthService authService;

    @BeforeEach
    void setUp() throws Exception {
        controller = new RegisterController();
        authService = mock(AuthService.class);
        setField(controller, "authService", authService);
    }

    @Test
    void doPostShouldForwardWhenPasswordConfirmMismatch() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getParameter("username")).thenReturn("Tester_01");
        when(request.getParameter("email")).thenReturn("tester@example.com");
        when(request.getParameter("password")).thenReturn("Password@123");
        when(request.getParameter("passwordConfirm")).thenReturn("Password@456");
        when(request.getRequestDispatcher("/views/pages/register.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        verify(request).setAttribute(eq("registerError"), contains("khớp"));
        verify(dispatcher).forward(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void doPostShouldRedirectToLoginWhenRegisterSuccess() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getParameter("username")).thenReturn("Tester_01");
        when(request.getParameter("email")).thenReturn("tester@example.com");
        when(request.getParameter("password")).thenReturn("Password@123");
        when(request.getParameter("passwordConfirm")).thenReturn("Password@123");
        when(authService.register("Tester_01", "tester@example.com", "Password@123"))
                .thenReturn(AuthService.RegisterStatus.SUCCESS);

        controller.doPost(request, response);

        verify(response).sendRedirect("login");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

