package com.daizuongkk.web.controller;

import com.daizuongkk.web.service.AuthService;
import com.daizuongkk.web.util.FlashUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "Register", value = "/register")
public class RegisterController extends HttpServlet {
	private AuthService authService;

	@Override
	public void init() {
		this.authService = new AuthService();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		FlashUtils.consume(request, "registerError", "submittedUsername", "submittedEmail");
		request.getRequestDispatcher("/views/pages/register.jsp").forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String username = trim(request.getParameter("username"));
		String email = trim(request.getParameter("email"));
		String password = request.getParameter("password");
		String passwordConfirm = request.getParameter("passwordConfirm");

		if (username.isEmpty() || email.isEmpty() || password == null || password.isEmpty()
				|| passwordConfirm == null || passwordConfirm.isEmpty()) {
			flashRegisterError(request, "Vui lòng nhập đầy đủ thông tin đăng kí.", username, email);
			response.sendRedirect("register");
			return;
		}
		if (!password.equals(passwordConfirm)) {
			flashRegisterError(request, "Xác nhận mật khẩu không khớp.", username, email);
			response.sendRedirect("register");
			return;
		}
		AuthService.RegisterStatus status = authService.register(username, email, password);
		String error = null;
		switch (status) {
			case INVALID_USERNAME_FORMAT ->
				error = "Tên đăng nhập không hợp lệ. Bắt đầu bằng chữ cái, chỉ dùng chữ, số, dấu . hoặc _ (6-32 ký tự).";
			case INVALID_EMAIL_FORMAT -> error = "Email không hơp lệ.";
			case INVALID_PASSWORD_FORMAT ->
				error = "Mật khẩu phải 8–32 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.";
			case USERNAME_EXISTS -> error = "Tên đăng nhập đã được sử dụng.";
			case EMAIL_EXISTS -> error = "Email đã được sử dụng.";
			case INVALID_INPUT -> error = "Dữ liệu không hợp lệ.";
			case FAILED -> error = "Đăng kí thất bại, vui lòng thử lại sau.";
			case SUCCESS -> {
				response.sendRedirect("login");
				return;
			}
		}

		flashRegisterError(request, error, username, email);
		response.sendRedirect("register");
	}

	private String trim(String value) {
		return value == null ? "" : value.trim();
	}

	private void flashRegisterError(HttpServletRequest request, String message, String username, String email) {
		FlashUtils.put(request, "registerError", message);
		FlashUtils.put(request, "submittedUsername", username);
		FlashUtils.put(request, "submittedEmail", email);
	}

}
