package com.daizuongkk.web.controller;

import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.model.User;
import com.daizuongkk.web.model.VerificationChannel;
import com.daizuongkk.web.service.CloudinaryService;
import com.daizuongkk.web.service.UserService;
import com.daizuongkk.web.service.VerificationService;
import com.daizuongkk.web.util.FlashUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.Map;

@WebServlet(name = "UserProfile", value = "/profile")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 12 * 1024 * 1024
)
public class UserProfileController extends HttpServlet {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private UserService userService;
    private VerificationService verificationService;
    private CloudinaryService cloudinaryService;

    @Override
    public void init() {
        this.userService = new UserService();
        this.verificationService = new VerificationService();
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
        FlashUtils.consume(request,
                "profileUser",
                "profileSuccess",
                "profileError",
                "activeVerificationChannel",
                "verificationTarget");
        request.getRequestDispatcher("/views/pages/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        UserResponse account = getAuthenticatedAccount(request);
        if (account == null) {
            if (isAjaxRequest(request)) {
                writeJson(response, Map.of("success", false, "message", "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."));
                return;
            }
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User currentUser = userService.getUserModelById(account.getId());
        if (currentUser == null) {
            if (isAjaxRequest(request)) {
                writeJson(response, Map.of("success", false, "message", "Tài khoản không còn hợp lệ. Vui lòng đăng nhập lại."));
                return;
            }
            response.sendRedirect(request.getContextPath() + "/logout");
            return;
        }

        String profileAction = request.getParameter("profileAction");
        if ("sendVerificationOtp".equals(profileAction)) {
            handleSendVerificationOtp(request, response, account, currentUser);
            return;
        }

        if ("verifyOtp".equals(profileAction)) {
            handleVerifyOtp(request, response, account, currentUser);
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
                FlashUtils.put(request, "profileSuccess", "Mật khẩu đã được cập nhật.");
            } else {
                FlashUtils.put(request, "profileError", getErrorMessage(status));
            }
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        String avatarUrl = currentUser.getAvtUrl();
        Part avatarPart = request.getPart("avatarFile");
        if (hasUpload(avatarPart) && !isImageUpload(avatarPart)) {
            flashProfileError(request, "Vui lòng chọn một file ảnh hợp lệ.",
                    buildSubmittedUser(account.getId(), request, currentUser.getAvtUrl()));
            response.sendRedirect(request.getContextPath() + "/profile");
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
                flashProfileError(request, "Không thể upload ảnh đại diện. Vui lòng thử ảnh khác.",
                        buildSubmittedUser(account.getId(), request, currentUser.getAvtUrl()));
                response.sendRedirect(request.getContextPath() + "/profile");
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
            FlashUtils.put(request, "profileSuccess", "Thông tin cá nhân đã được cập nhật.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        flashProfileError(request, getErrorMessage(status), buildSubmittedUser(account.getId(), request, avatarUrl));
        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void handleSendVerificationOtp(HttpServletRequest request,
            HttpServletResponse response,
            UserResponse account,
            User currentUser) throws ServletException, IOException {
        VerificationChannel channel = parseChannel(request.getParameter("channel"));
        String targetValue = request.getParameter("targetValue");
        VerificationService.VerificationStatus status = verificationService.sendOtp(account.getId(), channel, targetValue);

        if (status == VerificationService.VerificationStatus.OTP_SENT) {
            String message = channel == VerificationChannel.EMAIL
                    ? "Mã OTP đã được gửi đến email bạn vừa nhập."
                    : "Mã OTP đã được gửi đến số điện thoại bạn vừa nhập.";
            if (isAjaxRequest(request)) {
                writeJson(response, Map.of(
                        "success", true,
                        "message", message,
                        "channel", channel.name(),
                        "target", targetValue == null ? "" : targetValue.trim()
                ));
                return;
            }
            FlashUtils.put(request, "profileSuccess", message);
            FlashUtils.put(request, "activeVerificationChannel", channel.name());
            FlashUtils.put(request, "verificationTarget", targetValue);
        } else {
            String message = getVerificationMessage(status, channel);
            if (isAjaxRequest(request)) {
                writeJson(response, Map.of("success", false, "message", message));
                return;
            }
            FlashUtils.put(request, "profileError", message);
        }

        FlashUtils.put(request, "profileUser", buildVerificationUser(currentUser, channel, targetValue));
        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void handleVerifyOtp(HttpServletRequest request,
            HttpServletResponse response,
            UserResponse account,
            User currentUser) throws ServletException, IOException {
        VerificationChannel channel = parseChannel(request.getParameter("channel"));
        String targetValue = request.getParameter("targetValue");
        VerificationService.VerificationStatus status = verificationService.verifyOtp(
                account.getId(),
                channel,
                targetValue,
                request.getParameter("otp")
        );

        if (status == VerificationService.VerificationStatus.VERIFIED) {
            User updatedUser = userService.getUserModelById(account.getId());
            request.getSession().setAttribute("account", userService.toUserResponse(updatedUser));
            String message = channel == VerificationChannel.EMAIL
                    ? "Email đã được xác minh."
                    : "Số điện thoại đã được xác minh.";
            if (isAjaxRequest(request)) {
                writeJson(response, Map.of(
                        "success", true,
                        "message", message,
                        "channel", channel.name(),
                        "target", targetValue == null ? "" : targetValue.trim()
                ));
                return;
            }
            FlashUtils.put(request, "profileSuccess", message);
            FlashUtils.put(request, "profileUser", updatedUser);
        } else {
            String message = getVerificationMessage(status, channel);
            if (isAjaxRequest(request)) {
                writeJson(response, Map.of(
                        "success", false,
                        "message", message,
                        "channel", channel == null ? "" : channel.name(),
                        "target", targetValue == null ? "" : targetValue.trim()
                ));
                return;
            }
            FlashUtils.put(request, "profileError", message);
            FlashUtils.put(request, "activeVerificationChannel", channel == null ? "" : channel.name());
            FlashUtils.put(request, "verificationTarget", targetValue);
            FlashUtils.put(request, "profileUser", buildVerificationUser(currentUser, channel, targetValue));
        }

        response.sendRedirect(request.getContextPath() + "/profile");
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

    private void flashProfileError(HttpServletRequest request, String message, User profileUser) {
        FlashUtils.put(request, "profileError", message);
        FlashUtils.put(request, "profileUser", profileUser);
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
            case EMAIL_ALREADY_VERIFIED -> "Email đã được xác minh nên không thể thay đổi.";
            case INVALID_INPUT -> "Vui lòng nhập đầy đủ thông tin bắt buộc.";
            case FAILED -> "Không thể cập nhật thông tin. Vui lòng thử lại sau.";
            case SUCCESS -> "";
        };
    }

    private String getVerificationMessage(VerificationService.VerificationStatus status, VerificationChannel channel) {
        return switch (status) {
            case MISSING_TARGET -> channel == VerificationChannel.EMAIL
                    ? "Vui lòng nhập email trước khi yêu cầu xác minh."
                    : "Vui lòng nhập số điện thoại trước khi yêu cầu xác minh.";
            case SEND_FAILED -> channel == VerificationChannel.EMAIL
                    ? "Không thể gửi OTP qua Gmail SMTP. Vui lòng kiểm tra cấu hình SMTP."
                    : "Không thể gửi OTP qua Twilio SMS. Vui lòng kiểm tra cấu hình Twilio và định dạng số điện thoại.";
            case INVALID_OTP -> "Mã OTP không đúng.";
            case OTP_EXPIRED -> "Mã OTP đã hết hạn hoặc không tồn tại. Vui lòng yêu cầu mã mới.";
            case TOO_MANY_ATTEMPTS -> "Bạn đã nhập sai quá nhiều lần. Vui lòng yêu cầu mã mới.";
            case INVALID_EMAIL -> "Email cần xác minh không hợp lệ.";
            case EMAIL_EXISTS -> "Email này đã được sử dụng bởi tài khoản khác.";
            case INVALID_INPUT -> "Yêu cầu xác minh không hợp lệ.";
            case FAILED -> "Không thể xác minh. Vui lòng thử lại sau.";
            case OTP_SENT, VERIFIED -> "";
        };
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private User buildVerificationUser(User currentUser, VerificationChannel channel, String targetValue) {
        if (currentUser == null || channel == null || targetValue == null || targetValue.trim().isEmpty()) {
            return currentUser;
        }

        User user = User.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .email(currentUser.getEmail())
                .phone(currentUser.getPhone())
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .avtUrl(currentUser.getAvtUrl())
                .role(currentUser.getRole())
                .status(currentUser.getStatus())
                .verified(currentUser.getVerified())
                .phoneVerified(currentUser.getPhoneVerified())
                .build();
        if (channel == VerificationChannel.EMAIL) {
            user.setEmail(targetValue.trim());
            user.setVerified(false);
        } else {
            user.setPhone(targetValue.trim());
            user.setPhoneVerified(false);
        }
        return user;
    }

    private VerificationChannel parseChannel(String channel) {
        if ("EMAIL".equalsIgnoreCase(channel)) {
            return VerificationChannel.EMAIL;
        }
        if ("PHONE".equalsIgnoreCase(channel)) {
            return VerificationChannel.PHONE;
        }
        return null;
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

    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains("application/json"));
    }

    private void writeJson(HttpServletResponse response, Map<String, ?> body) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
