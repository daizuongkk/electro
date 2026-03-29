package com.daizuongkk.web.controller;

import com.daizuongkk.web.dto.response.CartItemResponse;
import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.model.Cart;
import com.daizuongkk.web.model.CartItem;
import com.daizuongkk.web.model.Role;
import com.daizuongkk.web.service.AuthService;
import com.daizuongkk.web.service.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "Login", value = "/login")
public class LoginController extends HttpServlet {

    private AuthService authService;
    private CartService cartService;
    @Override
    public void init() {
        this.authService = new AuthService();
        this.cartService = new CartService();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("account") != null) {
            response.sendRedirect("home");
            return;
        }

        String rememberedUsername = extractRememberedUsername(request);
        if (rememberedUsername != null && !rememberedUsername.isBlank()) {
            request.setAttribute("submittedUsername", rememberedUsername);
            request.setAttribute("rememberChecked", true);
        }

        request.getRequestDispatcher("/views/pages/login.jsp").forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException, ServletException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        boolean rememberMe = request.getParameter("remember") != null;

        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("loginError", "Vui lòng nhập tên đăng nhập & mật khẩu.");
            request.setAttribute("submittedUsername", username == null ? "" : username.trim());
            request.setAttribute("rememberChecked", rememberMe);
            request.getRequestDispatcher("/views/pages/login.jsp").forward(request, response);
            return;
        }

        UserResponse res = authService.login(username, password);
        if (res == null) {
            request.setAttribute("loginError", "Tên đăng nhập hoặc mật khẩu không hợp lệ.");
            request.setAttribute("submittedUsername", username.trim());
            request.setAttribute("rememberChecked", rememberMe);
            request.getRequestDispatcher("/views/pages/login.jsp").forward(request, response);
            return;
        }

        List<CartItemResponse> cart = cartService.getCart(res.getId());

        HttpSession session = request.getSession(true);
        session.setAttribute("account", res);
        session.setAttribute("cart", cart);

        writeRememberCookie(response, username.trim(), rememberMe, request.isSecure(), request.getContextPath());
        response.sendRedirect("home");
    }

    private String extractRememberedUsername(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if ("rememberedUsername".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void writeRememberCookie(HttpServletResponse response,
                                     String username,
                                     boolean rememberMe,
                                     boolean secureRequest,
                                     String contextPath) {
        Cookie cookie = new Cookie("rememberedUsername", rememberMe ? username : "");
        cookie.setPath((contextPath == null || contextPath.isEmpty()) ? "/" : contextPath);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureRequest);

        if (rememberMe) {
            cookie.setMaxAge(7 * 24 * 60 * 60);
        } else {
            cookie.setMaxAge(0);
        }

        response.addCookie(cookie);
    }
}
