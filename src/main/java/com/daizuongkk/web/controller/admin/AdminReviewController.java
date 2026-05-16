package com.daizuongkk.web.controller.admin;

import com.daizuongkk.web.dto.request.AdminReviewSearchRequest;
import com.daizuongkk.web.dto.response.ReviewResponse;
import com.daizuongkk.web.service.ReviewService;
import com.daizuongkk.web.util.PaginationUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet(name = "AdminReviewController", value = "/admin/reviews")
public class AdminReviewController extends BaseAdminServlet {
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }

        int page = PaginationUtils.parsePositiveInt(request.getParameter("p"), 1);
        int size = PaginationUtils.parsePositiveInt(request.getParameter("size"), 10);
        AdminReviewSearchRequest filters = buildFilters(request);

        long totalReviews = reviewService.countAdminReviews(filters);
        int totalPages = Math.max(1, (int) Math.ceil(totalReviews / (double) size));
        if (page > totalPages) {
            page = totalPages;
        }

        List<ReviewResponse> reviews = reviewService.getAdminReviews(filters, page, size);
        request.setAttribute("reviews", reviews);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", size);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalReviews", totalReviews);
        setFilterAttributes(request, filters);
        forward(request, response, "admin-reviews.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAdmin(request, response)) {
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            reviewService.deleteReview(parseLong(request.getParameter("id")));
        }
        redirectBackToReviews(request, response);
    }

    private AdminReviewSearchRequest buildFilters(HttpServletRequest request) {
        return AdminReviewSearchRequest.builder()
                .keyword(trim(request.getParameter("keyword")))
                .productId(parsePositiveLong(request.getParameter("productId")))
                .score(parseScore(request.getParameter("score")))
                .fromDate(parseDate(request.getParameter("fromDate")))
                .toDate(parseDate(request.getParameter("toDate")))
                .sortBy(normalizeSort(request.getParameter("sortBy")))
                .build();
    }

    private void setFilterAttributes(HttpServletRequest request, AdminReviewSearchRequest filters) {
        request.setAttribute("keyword", filters.getKeyword() == null ? "" : filters.getKeyword());
        request.setAttribute("productId", filters.getProductId() == null ? "" : filters.getProductId().toString());
        request.setAttribute("selectedScore", filters.getScore() == null ? "" : filters.getScore().toString());
        request.setAttribute("fromDate", filters.getFromDate() == null ? "" : filters.getFromDate().toString());
        request.setAttribute("toDate", filters.getToDate() == null ? "" : filters.getToDate().toString());
        request.setAttribute("selectedSortBy", filters.getSortBy() == null ? "created_desc" : filters.getSortBy());
    }

    private void redirectBackToReviews(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String returnUrl = request.getParameter("returnUrl");
        String adminReviewsPath = request.getContextPath() + "/admin/reviews";
        if (returnUrl != null && returnUrl.startsWith(adminReviewsPath)) {
            response.sendRedirect(returnUrl);
            return;
        }
        response.sendRedirect(adminReviewsPath);
    }

    private Integer parseScore(String value) {
        Integer score = null;
        try {
            score = value == null || value.isBlank() ? null : Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
        return score != null && score >= 1 && score <= 5 ? score : null;
    }

    private Long parsePositiveLong(String value) {
        Long parsed = parseLong(value);
        return parsed != null && parsed > 0 ? parsed : null;
    }

    private String normalizeSort(String value) {
        String sortBy = trim(value);
        if (sortBy == null || sortBy.isBlank()) {
            return "created_desc";
        }
        return switch (sortBy.toLowerCase()) {
            case "created_asc", "created_desc", "score_asc", "score_desc" -> sortBy.toLowerCase();
            default -> "created_desc";
        };
    }

    private LocalDate parseDate(String value) {
        try {
            return value == null || value.isBlank() ? null : LocalDate.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
