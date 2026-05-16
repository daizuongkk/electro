package com.daizuongkk.web.controller.web;

import com.daizuongkk.web.dto.response.ProductResponse;
import com.daizuongkk.web.dto.response.ReviewResponse;
import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.model.Category;
import com.daizuongkk.web.service.ProductService;
import com.daizuongkk.web.service.ReviewService;
import com.daizuongkk.web.util.PaginationUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "Products", value = "/products")
public class ProductController extends HttpServlet {
	private ProductService productService;
	private ReviewService reviewService;
	private static final int REVIEWS_PER_PAGE = 3;

	@Override
	public void init() throws ServletException {
		productService = new ProductService();
		reviewService = new ReviewService();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String idParam = request.getParameter("id");
		long productId;

		try {
			if (idParam == null || idParam.isEmpty()) {
				throw new NumberFormatException("Missing ID");
			}
			productId = Long.parseLong(idParam);
			if (productId < 0) throw new NumberFormatException("Negative ID");
		} catch (NumberFormatException e) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		ProductResponse product = productService.getProductById(productId);
		if (product == null) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		long totalReviews = reviewService.countReviewsByProductId(productId);
		int totalPages = (int) Math.ceil((double) totalReviews / REVIEWS_PER_PAGE);
		totalPages = Math.max(totalPages, 1);

		int currentPage = PaginationUtils.parsePositiveInt(request.getParameter("page"), 1);
		if (currentPage > totalPages) currentPage = totalPages;

		List<ReviewResponse> reviews = reviewService.getReviewsByProductId(productId, currentPage, REVIEWS_PER_PAGE);
		List<ProductResponse> relatedProducts = productService.getProductsByCategory(product.getCategory());
		UserResponse account = getAccount(request);
		boolean hasReviewed = account != null && reviewService.hasUserReviewedProduct(productId, account.getId());
		boolean canReview = account != null && !hasReviewed && reviewService.canUserReviewProduct(productId, account.getId());

		request.setAttribute("product", product);
		request.setAttribute("categories", Category.getAlls());
		request.setAttribute("relatedProducts", relatedProducts);
		request.setAttribute("reviews", reviews);
		request.setAttribute("stars", reviewService.countStars(productId));
		request.setAttribute("totalRv", totalReviews);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("hasReviewed", hasReviewed);
		request.setAttribute("canReview", canReview);

		request.getRequestDispatcher("views/pages/product.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.setCharacterEncoding("UTF-8");
		Long productId = parseLong(request.getParameter("id"));
		UserResponse account = getAccount(request);

		if (productId == null || productId <= 0) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}
		if (account == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		Integer score = parseInteger(request.getParameter("rating"));
		String message = request.getParameter("message");
		boolean added = score != null && reviewService.addReview(productId, account.getId(), message, score);
		String result = added ? "reviewed" : "review_error";
		response.sendRedirect(request.getContextPath() + "/products?id=" + productId + "&tab=reviews&" + result + "=1");
	}

	private UserResponse getAccount(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		return session == null ? null : (UserResponse) session.getAttribute("account");
	}

	private Long parseLong(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Integer parseInteger(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
