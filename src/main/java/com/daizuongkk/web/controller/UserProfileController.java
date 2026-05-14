package com.daizuongkk.web.controller;

import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.model.User;
import com.daizuongkk.web.service.CloudinaryService;
import com.daizuongkk.web.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;

@WebServlet(name = "UserProfile", value = "/profile")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 8 * 1024 * 1024
)
public class UserProfileController extends HttpServlet {
    private UserService userService;
    private CloudinaryService cloudinaryService;

    @Override
    public void init() {
        this.userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserResponse account = getAuthenticatedAccount(request);
        if (account == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = userService.getUserModelById(account.getId());
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/logout");
            return;
        }

        request.setAttribute("profileUser", user);
        request.getRequestDispatcher("/views/pages/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        UserResponse account = getAuthenticatedAccount(request);
        if (account == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User currentUser = userService.getUserModelById(account.getId());
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/logout");
            return;
        }

        if ("changePassword".equals(request.getParameter("profileAction"))) {
            UserService.UpdateProfileStatus status = userService.changePassword(
                    account.getId(),
                    request.getParameter("currentPassword"),
                    request.getParameter("newPassword"),
                    request.getParameter("confirmPassword")
            );

            if (status == UserService.UpdateProfileStatus.SUCCESS) {
                request.setAttribute("profileSuccess", "Mật khẩu đã được cập nhật.");
            } else {
                request.setAttribute("profileError", getErrorMessage(status));
            }
            request.setAttribute("profileUser", currentUser);
            request.getRequestDispatcher("/views/pages/profile.jsp").forward(request, response);
            return;
        }

        String avatarUrl = currentUser.getAvtUrl();
        Part avatarPart = request.getPart("avatarFile");
        if (hasUpload(avatarPart) && !isImageUpload(avatarPart)) {
            request.setAttribute("profileError", "Vui lòng chọn một file ảnh hợp lệ.");
            request.setAttribute("profileUser", buildSubmittedUser(account.getId(), request, currentUser.getAvtUrl()));
            request.getRequestDispatcher("/views/pages/profile.jsp").forward(request, response);
            return;
        }

        if (isImageUpload(avatarPart)) {
            try {
                if (cloudinaryService == null) {
                    cloudinaryService = new CloudinaryService();
                }
                String uploadedAvatarUrl = cloudinaryService.uploadImage(avatarPart, "electro/avatars");
                if (uploadedAvatarUrl != null) {
                    avatarUrl = uploadedAvatarUrl;
                }
            } catch (IOException | RuntimeException e) {
                request.setAttribute("profileError", "Không thể upload ảnh đại diện. Vui lòng thử ảnh khác.");
                request.setAttribute("profileUser", buildSubmittedUser(account.getId(), request, currentUser.getAvtUrl()));
                request.getRequestDispatcher("/views/pages/profile.jsp").forward(request, response);
                return;
            }
        }

        UserService.UpdateProfileStatus status = userService.updateProfile(
                account.getId(),
                request.getParameter("username"),
                request.getParameter("email"),
                request.getParameter("firstName"),
                request.getParameter("lastName"),
                request.getParameter("phone"),
                avatarUrl
        );

        if (status == UserService.UpdateProfileStatus.SUCCESS) {
            User updatedUser = userService.getUserModelById(account.getId());
            request.getSession().setAttribute("account", userService.toUserResponse(updatedUser));
            request.setAttribute("profileSuccess", "Thông tin cá nhân đã được cập nhật.");
            request.setAttribute("profileUser", updatedUser);
            request.getRequestDispatcher("/views/pages/profile.jsp").forward(request, response);
            return;
        }

        request.setAttribute("profileError", getErrorMessage(status));
        request.setAttribute("profileUser", buildSubmittedUser(account.getId(), request, avatarUrl));
        request.getRequestDispatcher("/views/pages/profile.jsp").forward(request, response);
    }

    private UserResponse getAuthenticatedAccount(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (UserResponse) session.getAttribute("account");
    }

    private User buildSubmittedUser(Long userId, HttpServletRequest request, String avatarUrl) {
        return User.builder()
                .id(userId)
                .username(trim(request.getParameter("username")))
                .email(trim(request.getParameter("email")))
                .firstName(trim(request.getParameter("firstName")))
                .lastName(trim(request.getParameter("lastName")))
                .phone(trim(request.getParameter("phone")))
                .avtUrl(avatarUrl)
                .build();
    }

    private String getErrorMessage(UserService.UpdateProfileStatus status) {
        return switch (status) {
            case INVALID_USERNAME_FORMAT ->
                    "Tên đăng nhập không hợp lệ. Bắt đầu bằng chữ cái, chỉ dùng chữ, số, dấu . hoặc _ (6-32 ký tự).";
            case INVALID_EMAIL_FORMAT -> "Email không hợp lệ.";
            case INVALID_CURRENT_PASSWORD -> "Mật khẩu hiện tại không đúng.";
            case INVALID_NEW_PASSWORD_FORMAT ->
                    "Mật khẩu mới phải có 8-32 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.";
            case PASSWORD_MISMATCH -> "Xác nhận mật khẩu mới không khớp.";
            case USERNAME_EXISTS -> "Tên đăng nhập đã được sử dụng.";
            case EMAIL_EXISTS -> "Email đã được sử dụng.";
            case INVALID_INPUT -> "Vui lòng nhập đầy đủ thông tin bắt buộc.";
            case FAILED -> "Không thể cập nhật thông tin. Vui lòng thử lại sau.";
            case SUCCESS -> "";
        };
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isImageUpload(Part part) {
        if (!hasUpload(part)) {
            return false;
        }

        String contentType = part.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("image/");
    }

    private boolean hasUpload(Part part) {
        return part != null && part.getSize() > 0;
    }
}
