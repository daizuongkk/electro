package com.daizuongkk.web.controller;

import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.service.AuthService;
import com.daizuongkk.web.service.CartService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.Mockito.*;

class LoginControllerTest {

    private LoginController controller;
    private AuthService authService;
    private CartService cartService;

    @BeforeEach
    void setUp() throws Exception {
        controller = new LoginController();
        authService = mock(AuthService.class);
        cartService = mock(CartService.class);
        setField(controller, "authService", authService);
        setField(controller, "cartService", cartService);
    }

    @Test
    void doPostShouldForwardWithErrorWhenMissingCredentials() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getParameter("username")).thenReturn(" ");
        when(request.getParameter("password")).thenReturn("");
        when(request.getParameter("remember")).thenReturn(null);
        when(request.getRequestDispatcher("/views/pages/login.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        verify(request).setAttribute(eq("loginError"), anyString());
        verify(dispatcher).forward(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void doPostShouldSetSessionAndRedirectWhenLoginSuccess() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        UserResponse userResponse = UserResponse.builder().id(1L).username("tester").build();

        when(request.getParameter("username")).thenReturn("tester");
        when(request.getParameter("password")).thenReturn("Password@123");
        when(request.getParameter("remember")).thenReturn("on");
        when(authService.login("tester", "Password@123")).thenReturn(userResponse);
        when(cartService.getCartItems(1L)).thenReturn(List.of());
        when(request.getSession(true)).thenReturn(session);
        when(request.isSecure()).thenReturn(false);
        when(request.getContextPath()).thenReturn("");

        controller.doPost(request, response);

        verify(session).setAttribute("account", userResponse);
        verify(response).addCookie(any());
        verify(response).sendRedirect("home");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

