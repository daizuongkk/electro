package com.daizuongkk.web.filter;

import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.model.Role;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());

        boolean isPublicPage = path.startsWith("/login") ||
                path.startsWith("/register") ||
                path.startsWith("/home") ||
                path.startsWith("/products") ||
                path.startsWith("/assets") ||
                path.contains("404-page.jsp");

        if (isPublicPage) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        UserResponse account = (session != null) ? (UserResponse) session.getAttribute("account") : null;

        boolean isAdminArea = path.startsWith("/admin");
        if (isAdminArea) {
            if (account == null || account.getRole() != Role.ADMIN) {
                req.getRequestDispatcher("/views/pages/404-page.jsp").forward(req, res);
                return;
            }
        }

        if (account == null) {
            if (path.startsWith("/api/")) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("{\"error\": \"Unauthorized\"}");
            } else {
                res.sendRedirect(contextPath + "/login");
            }
            return;
        }

        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setDateHeader("Expires", 0);

        chain.doFilter(request, response);
    }
}