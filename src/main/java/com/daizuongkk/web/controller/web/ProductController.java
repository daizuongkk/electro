package com.daizuongkk.web.controller.web;

import com.daizuongkk.web.dto.response.ProductResponse;
import com.daizuongkk.web.dto.response.ReviewResponse;
import com.daizuongkk.web.model.Category;
import com.daizuongkk.web.service.ProductService;
import com.daizuongkk.web.service.ReviewService;
import com.daizuongkk.web.util.PaginationUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

		request.setAttribute("product", product);
		request.setAttribute("categories", Category.getAlls());
		request.setAttribute("relatedProducts", relatedProducts);
		request.setAttribute("reviews", reviews);
		request.setAttribute("stars", reviewService.countStars(productId));
		request.setAttribute("totalRv", totalReviews);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);

		request.getRequestDispatcher("views/pages/product.jsp").forward(request, response);
	}
}