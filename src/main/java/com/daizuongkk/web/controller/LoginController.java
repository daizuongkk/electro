package com.daizuongkk.web.controller;

import java.io.IOException;
import java.util.List;

import com.daizuongkk.web.dto.response.CartItemResponse;
import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.model.Order;
import com.daizuongkk.web.service.AuthService;
import com.daizuongkk.web.service.CartService;
import com.daizuongkk.web.service.OrderService;
import com.daizuongkk.web.util.FlashUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "Login", value = "/login")
public class LoginController extends HttpServlet {

	private AuthService authService;
	private CartService cartService;
	private OrderService orderService;

	@Override
	public void init() {
		this.authService = new AuthService();
		this.cartService = new CartService();
		this.orderService = new OrderService();
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
		FlashUtils.consume(request, "loginError", "submittedUsername", "rememberChecked");

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
			flashLoginError(request, "Vui lòng nhập tên đăng nhập & mật khẩu.", username, rememberMe);
			response.sendRedirect("login");
			return;
		}

		UserResponse res = authService.login(username, password);

		if (res == null) {
			flashLoginError(request, "Tên đăng nhập hoặc mật khẩu không hợp lệ.", username, rememberMe);
			response.sendRedirect("login");
			return;
		}

		List<CartItemResponse> cart = cartService.getCartItems(res.getId());
		List<Order> allOrders = orderService.findOrdersByUserId(res.getId());

		HttpSession session = request.getSession(true);
		session.setAttribute("account", res);
		session.setAttribute("cart", cart);
		session.setAttribute("orderCount", getOrderService().countOrdersByUserId(res.getId()));

		writeRememberCookie(response, username.trim(), rememberMe, request.isSecure(), request.getContextPath());

		response.sendRedirect("home");
	}

	private OrderService getOrderService() {
		if (orderService == null) {
			orderService = new OrderService();
		}
		return orderService;
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

	private void flashLoginError(HttpServletRequest request, String message, String username, boolean rememberMe) {
		FlashUtils.put(request, "loginError", message);
		FlashUtils.put(request, "submittedUsername", username == null ? "" : username.trim());
		FlashUtils.put(request, "rememberChecked", rememberMe);
	}

}
