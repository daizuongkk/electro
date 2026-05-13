package com.daizuongkk.web.controller.admin;

import com.daizuongkk.web.dto.request.AdminUserSearchRequest;
import com.daizuongkk.web.model.Role;
import com.daizuongkk.web.model.User;
import com.daizuongkk.web.service.UserService;
import com.daizuongkk.web.util.PaginationUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "AdminUserController", value = {"/admin/users", "/admin/users/form"})
public class AdminUserController extends BaseAdminServlet {

    private final UserService userService = new UserService();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }

        if (request.getServletPath().endsWith("/form")) {
            loadUserForm(request);
            forward(request, response, "user-form.jsp");
            return;
        }

        loadUsers(request);
        forward(request, response, "users.jsp");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }

        request.setCharacterEncoding("UTF-8");
        saveUser(request, response);
    }

    private void loadUsers(HttpServletRequest request) {
        int page = PaginationUtils.parsePositiveInt(request.getParameter("p"), 1);
        int size = PaginationUtils.parsePositiveInt(request.getParameter("size"), 10);
        AdminUserSearchRequest filters = buildFilters(request);

        long totalUsers = userService.countUsers(filters);
        int totalPages = Math.max(1, (int) Math.ceil(totalUsers / (double) size));
        if (page > totalPages) {
            page = totalPages;
        }

        request.setAttribute("users", userService.findUsers(filters, page, size));
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", size);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalUsers", totalUsers);
        setFilterAttributes(request, filters);
    }

    private void loadUserForm(HttpServletRequest request) {
        Long id = parseLong(request.getParameter("id"));
        if (id != null) {
            User user = userService.getUserModelById(id);
            request.setAttribute("userForm", user);
            request.setAttribute("editMode", user != null);
        } else {
            request.setAttribute("editMode", false);
        }
        request.setAttribute("roles", Role.values());
    }

    private void saveUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long id = parseLong(request.getParameter("id"));
        User user = User.builder()
                .id(id)
                .username(trim(request.getParameter("username")))
                .email(trim(request.getParameter("email")))
                .firstName(trim(request.getParameter("firstName")))
                .lastName(trim(request.getParameter("lastName")))
                .phone(trim(request.getParameter("phone")))
                .role(parseRole(request.getParameter("role")))
                .status(parseStatus(request.getParameter("status")))
                .verified("true".equals(request.getParameter("verified")))
                .build();

        String password = request.getParameter("password");
        boolean ok = id == null
                ? userService.createUser(user, password)
                : userService.updateUser(user, password);

        if (!ok) {
            request.setAttribute("error", "Không thể lưu người dùng. Kiểm tra username/email/password và thử lại.");
            request.setAttribute("userForm", user);
            request.setAttribute("editMode", id != null);
            request.setAttribute("roles", Role.values());
            forward(request, response, "user-form.jsp");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private AdminUserSearchRequest buildFilters(HttpServletRequest request) {
        return AdminUserSearchRequest.builder()
                .keyword(trim(request.getParameter("keyword")))
                .role(parseRoleNullable(request.getParameter("role")))
                .status(trim(request.getParameter("status")))
                .verified(parseVerified(request.getParameter("verified")))
                .build();
    }

    private void setFilterAttributes(HttpServletRequest request, AdminUserSearchRequest filters) {
        request.setAttribute("keyword", filters.getKeyword() == null ? "" : filters.getKeyword());
        request.setAttribute("selectedRole", filters.getRole() == null ? "" : filters.getRole().name());
        request.setAttribute("selectedStatus", filters.getStatus() == null ? "" : filters.getStatus());
        request.setAttribute("selectedVerified", filters.getVerified() == null ? "" : filters.getVerified().toString());
        request.setAttribute("roles", Role.values());
    }

    private Role parseRole(String value) {
        Role role = parseRoleNullable(value);
        return role == null ? Role.CUSTOMER : role;
    }

    private Role parseRoleNullable(String value) {
        try {
            return value == null || value.isBlank() ? null : Role.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private String parseStatus(String value) {
        String status = value == null ? "" : value.trim().toUpperCase();
        if (!status.equals("ACTIVE") && !status.equals("INACTIVE") && !status.equals("BANNED")) {
            return "ACTIVE";
        }
        return status;
    }

    private Boolean parseVerified(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }
}
