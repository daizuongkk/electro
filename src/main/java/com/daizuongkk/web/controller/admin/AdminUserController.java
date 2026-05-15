package com.daizuongkk.web.controller.admin;

import com.daizuongkk.web.dto.request.AdminUserSearchRequest;
import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.model.Role;
import com.daizuongkk.web.model.User;
import com.daizuongkk.web.service.CloudinaryService;
import com.daizuongkk.web.service.UserService;
import com.daizuongkk.web.util.FlashUtils;
import com.daizuongkk.web.util.PaginationUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;

@WebServlet(name = "AdminUserController", value = {"/admin/users", "/admin/users/form"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 10 * 1024 * 1024, maxRequestSize = 12 * 1024 * 1024)
public class AdminUserController extends BaseAdminServlet {

    private final UserService userService = new UserService();
    private CloudinaryService cloudinaryService;

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
        if ("delete-user".equals(request.getParameter("action"))) {
            deleteUser(request, response);
            return;
        }

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
            User user = userService.getAdminUserModelById(id);
            request.setAttribute("userForm", user);
            request.setAttribute("editMode", user != null);
        } else {
            request.setAttribute("editMode", false);
        }
        request.setAttribute("roles", Role.values());
        FlashUtils.consume(request, "error", "userForm");
    }

    private void saveUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long id = parseLong(request.getParameter("id"));
        User currentUser = id == null ? null : userService.getAdminUserModelById(id);
        String avatarUrl = currentUser == null ? trim(request.getParameter("avatarUrl")) : currentUser.getAvtUrl();
        Part avatarPart = request.getPart("avatarFile");
        User submittedUser = buildSubmittedUser(id, request, avatarUrl);

        if (hasUpload(avatarPart) && !isImageUpload(avatarPart)) {
            forwardUserFormError(request, response, submittedUser, id != null, "Vui lòng chọn một file ảnh hợp lệ.");
            return;
        }

        if (isImageUpload(avatarPart)) {
            try {
                if (cloudinaryService == null) {
                    cloudinaryService = new CloudinaryService();
                }
                String uploadedAvatarUrl = cloudinaryService.uploadImage(avatarPart, "electro/avatars");
                if (uploadedAvatarUrl != null && !uploadedAvatarUrl.isBlank()) {
                    avatarUrl = uploadedAvatarUrl;
                }
            } catch (IOException | RuntimeException e) {
                forwardUserFormError(request, response, submittedUser, id != null,
                        "Không thể upload ảnh đại diện. Vui lòng thử ảnh khác.");
                return;
            }
        }

        User user = User.builder()
                .id(id)
                .username(trim(request.getParameter("username")))
                .email(trim(request.getParameter("email")))
                .firstName(trim(request.getParameter("firstName")))
                .lastName(trim(request.getParameter("lastName")))
                .phone(trim(request.getParameter("phone")))
                .avtUrl(avatarUrl)
                .role(parseRole(request.getParameter("role")))
                .status(parseStatus(request.getParameter("status")))
                .verified("true".equals(request.getParameter("verified")))
                .build();

        String password = request.getParameter("password");
        UserService.AdminUserSaveStatus saveStatus = userService.saveAdminUser(user, password);

        if (saveStatus != UserService.AdminUserSaveStatus.SUCCESS) {
            forwardUserFormError(request, response, user, id != null, getAdminUserSaveMessage(saveStatus));
            return;
        }

        UserResponse account = (UserResponse) request.getSession().getAttribute("account");
        if (account != null && id != null && id.equals(account.getId())) {
            User updatedUser = userService.getAdminUserModelById(id);
            request.getSession().setAttribute("account", userService.toUserResponse(updatedUser));
        }

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private User buildSubmittedUser(Long id, HttpServletRequest request, String avatarUrl) {
        return User.builder()
                .id(id)
                .username(trim(request.getParameter("username")))
                .email(trim(request.getParameter("email")))
                .firstName(trim(request.getParameter("firstName")))
                .lastName(trim(request.getParameter("lastName")))
                .phone(trim(request.getParameter("phone")))
                .avtUrl(avatarUrl)
                .role(parseRole(request.getParameter("role")))
                .status(parseStatus(request.getParameter("status")))
                .verified("true".equals(request.getParameter("verified")))
                .deleted(false)
                .build();
    }

    private void forwardUserFormError(HttpServletRequest request, HttpServletResponse response, User user, boolean editMode, String error)
            throws ServletException, IOException {
        FlashUtils.put(request, "error", error);
        FlashUtils.put(request, "userForm", user);
        String formUrl = request.getContextPath() + "/admin/users/form" + (editMode && user.getId() != null ? "?id=" + user.getId() : "");
        response.sendRedirect(formUrl);
    }

    private boolean hasUpload(Part part) {
        return part != null && part.getSize() > 0;
    }

    private boolean isImageUpload(Part part) {
        String contentType = part == null ? null : part.getContentType();
        return hasUpload(part) && contentType != null && contentType.toLowerCase().startsWith("image/");
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long id = parseLong(request.getParameter("id"));
        UserResponse account = (UserResponse) request.getSession().getAttribute("account");
        if (account == null || id == null || id.equals(account.getId())) {
            redirectBackToUsers(request, response);
            return;
        }

        userService.deleteUser(id);
        redirectBackToUsers(request, response);
    }

    private AdminUserSearchRequest buildFilters(HttpServletRequest request) {
        return AdminUserSearchRequest.builder()
                .keyword(trim(request.getParameter("keyword")))
                .role(parseRoleNullable(request.getParameter("role")))
                .status(trim(request.getParameter("status")))
                .verified(parseVerified(request.getParameter("verified")))
                .deleted(normalizeDeletedFilter(request.getParameter("deleted")))
                .sortBy(normalizeUserSort(request.getParameter("sortBy")))
                .build();
    }

    private void setFilterAttributes(HttpServletRequest request, AdminUserSearchRequest filters) {
        request.setAttribute("keyword", filters.getKeyword() == null ? "" : filters.getKeyword());
        request.setAttribute("selectedRole", filters.getRole() == null ? "" : filters.getRole().name());
        request.setAttribute("selectedStatus", filters.getStatus() == null ? "" : filters.getStatus());
        request.setAttribute("selectedVerified", filters.getVerified() == null ? "" : filters.getVerified().toString());
        request.setAttribute("selectedDeleted", filters.getDeleted() == null ? "" : filters.getDeleted());
        request.setAttribute("selectedSortBy", filters.getSortBy() == null ? "created_desc" : filters.getSortBy());
        request.setAttribute("roles", Role.values());
    }

    private void redirectBackToUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String returnUrl = request.getParameter("returnUrl");
        String adminUsersPath = request.getContextPath() + "/admin/users";
        if (returnUrl != null && returnUrl.startsWith(adminUsersPath)) {
            response.sendRedirect(returnUrl);
            return;
        }
        response.sendRedirect(adminUsersPath);
    }

    private String normalizeDeletedFilter(String value) {
        String normalized = trim(value);
        if ("active".equalsIgnoreCase(normalized) || "deleted".equalsIgnoreCase(normalized)) {
            return normalized.toLowerCase();
        }
        return "";
    }

    private String normalizeUserSort(String value) {
        String normalized = trim(value);
        if (normalized == null || normalized.isBlank()) {
            return "created_desc";
        }
        return switch (normalized.toLowerCase()) {
            case "created_asc", "created_desc", "deleted_asc", "deleted_desc", "status_asc" -> normalized.toLowerCase();
            default -> "created_desc";
        };
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

    private String getAdminUserSaveMessage(UserService.AdminUserSaveStatus status) {
        return switch (status) {
            case INVALID_INPUT -> "Vui lòng nhập đầy đủ username và email.";
            case INVALID_ID -> "Người dùng cần cập nhật không hợp lệ.";
            case INVALID_USERNAME_FORMAT -> "Username phải bắt đầu bằng chữ cái, chỉ gồm chữ, số, dấu . hoặc _ và dài 6-32 ký tự.";
            case INVALID_EMAIL_FORMAT -> "Email không hợp lệ.";
            case INVALID_PHONE_FORMAT -> "Số điện thoại không hợp lệ. Chỉ dùng số, dấu +, khoảng trắng, dấu ngoặc, dấu chấm hoặc gạch ngang.";
            case INVALID_PASSWORD_FORMAT -> "Mật khẩu phải dài 8-32 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.";
            case INVALID_ROLE -> "Vai trò người dùng không hợp lệ.";
            case INVALID_STATUS -> "Trạng thái người dùng không hợp lệ.";
            case USERNAME_EXISTS -> "Username đã được sử dụng.";
            case EMAIL_EXISTS -> "Email đã được sử dụng.";
            case FAILED -> "Không thể lưu người dùng. Vui lòng thử lại sau.";
            case SUCCESS -> "";
        };
    }
}
