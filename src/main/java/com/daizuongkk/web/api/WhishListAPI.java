package com.daizuongkk.web.api;

import com.daizuongkk.web.dto.response.ProductResponse;
import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.service.WishlistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "WhiteListAPI", value = "/api/whishlist/*")
public class WhishListAPI extends HttpServlet {

	private final WishlistService wishlistService = new WishlistService();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();

		UserResponse user = getAuthenticatedUser(request, response, out);
		if (user == null) {
			return;
		}

		try {
			List<ProductResponse> wishlist = wishlistService.getWishlistProducts(user.getId());
			request.getSession(false).setAttribute("wishlist", wishlist);
			out.write(new ObjectMapper().writeValueAsString(wishlist));
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.write("{\"error\":\"Server error\"}");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();

		UserResponse user = getAuthenticatedUser(request, response, out);
		if (user == null) {
			return;
		}

		Long productId = parseProductId(request, response, out);
		if (productId == null) {
			return;
		}

		try {
			boolean liked = wishlistService.toggleWishlist(user.getId(), productId);
			List<ProductResponse> wishlist = wishlistService.getWishlistProducts(user.getId());
			request.getSession(false).setAttribute("wishlist", wishlist);
			response.setStatus(HttpServletResponse.SC_OK);
			out.write("{\"message\":\"Updated wishlist\",\"liked\":" + liked + ",\"productId\":" + productId + "}");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.write("{\"error\":\"Server error\"}");
		}
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();

		UserResponse user = getAuthenticatedUser(request, response, out);
		if (user == null) {
			return;
		}

		Long productId = parseProductId(request, response, out);
		if (productId == null) {
			return;
		}

		try {
			wishlistService.removeFromWishlist(user.getId(), productId);
			List<ProductResponse> wishlist = wishlistService.getWishlistProducts(user.getId());
			request.getSession(false).setAttribute("wishlist", wishlist);
			response.setStatus(HttpServletResponse.SC_OK);
			out.write("{\"message\":\"Removed from wishlist\"}");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.write("{\"error\":\"Server error\"}");
		}
	}

	private UserResponse getAuthenticatedUser(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			out.write("{\"error\":\"Unauthorized\"}");
			return null;
		}

		UserResponse user = (UserResponse) session.getAttribute("account");
		if (user == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			out.write("{\"error\":\"Unauthorized\"}");
			return null;
		}

		return user;
	}

	private Long parseProductId(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
		String pathInfo = request.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.write("{\"error\":\"Product ID is required\"}");
			return null;
		}

		try {
			Long productId = Long.parseLong(pathInfo.substring(1));
			if (productId <= 0) {
				throw new NumberFormatException();
			}
			return productId;
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.write("{\"error\":\"Invalid product ID\"}");
			return null;
		}
	}
}
