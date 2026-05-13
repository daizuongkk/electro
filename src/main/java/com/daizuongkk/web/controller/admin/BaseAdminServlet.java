package com.daizuongkk.web.controller.admin;

import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.model.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public abstract class BaseAdminServlet extends HttpServlet {

    protected boolean requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserResponse account = (UserResponse) request.getSession().getAttribute("account");
        if (account == null || account.getRole() != Role.ADMIN) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        return true;
    }

    protected void forward(HttpServletRequest request, HttpServletResponse response, String jsp) throws ServletException, IOException {
        request.getRequestDispatcher("/views/pages/" + jsp).forward(request, response);
    }

    protected Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    protected Long parseLongOrDefault(String value, Long defaultValue) {
        Long parsed = parseLong(value);
        return parsed == null ? defaultValue : parsed;
    }

    protected Double parseDouble(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    protected String trim(String value) {
        return value == null ? null : value.trim();
    }
}
