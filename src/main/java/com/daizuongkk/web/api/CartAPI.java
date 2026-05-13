package com.daizuongkk.web.api;


import com.daizuongkk.web.dto.response.CartItemResponse;
import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "CartAPI", value = "/api/carts/*")
public class CartAPI  extends HttpServlet {

    CartService cartService = new CartService();


    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Unauthorized\"}");
            return;
        }

        UserResponse user = (UserResponse) session.getAttribute("account");
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Unauthorized\"}");
            return;
        }

        try {
            Long userId = user.getId();
           List<CartItemResponse> cart = cartService.getCartItems(userId);

           ObjectMapper mapper = new ObjectMapper();

           String res =  mapper.writeValueAsString(cart);
           out.write(res);

           session.setAttribute("cart", cart);
           response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Server error\"}");
        }



    }


    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Unauthorized\"}");
            return;
        }

        UserResponse user = (UserResponse) session.getAttribute("account");
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Unauthorized\"}");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Product ID is required\"}");
            return;
        }


        try {
            Long productId = Long.parseLong(pathInfo.substring(1));

            if (productId <= 0) {
                throw new NumberFormatException();
            }

            Long qty = Long.parseLong(request.getParameter("qty"));

            if (qty <= 0) {
                throw new NumberFormatException();
            }

            cartService.addToCart(user.getId(), productId, qty);

            response.setStatus(HttpServletResponse.SC_OK);
            List<CartItemResponse> cart = cartService.getCartItems(user.getId());
            session.setAttribute("cart", cart);
            out.write("{\"message\":\"Added to cart\"}");

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Invalid method argument\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Server error\"}");
        }
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Unauthorized\"}");
            return;
        }

        UserResponse user = (UserResponse) session.getAttribute("account");
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Unauthorized\"}");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Product ID is required\"}");
            return;
        }

        try {
            Long productId = Long.parseLong(pathInfo.substring(1));
            Long qty = Long.parseLong(request.getParameter("qty"));

            if (productId <= 0 || qty <= 0) {
                throw new NumberFormatException();
            }

            boolean updated = cartService.updateQuantity(user.getId(), productId, qty);
            if (!updated) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"error\":\"Cart item not found\"}");
                return;
            }

            List<CartItemResponse> cart = cartService.getCartItems(user.getId());
            session.setAttribute("cart", cart);
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"message\":\"Updated cart\",\"quantity\":" + qty + "}");
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Invalid method argument\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Server error\"}");
        }
    }

    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Unauthorized\"}");
            return;
        }

        UserResponse user = (UserResponse) session.getAttribute("account");
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Unauthorized\"}");
            return;
        }
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Product ID is required\"}");
            return;
        }

        try {
            String productIds = pathInfo.substring(1);

            if (productIds.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            cartService.delete(productIds, user.getId());
            out.write("{\"message\":\"Deleted from cart\"}");
            response.setStatus(HttpServletResponse.SC_OK);
            session.setAttribute("cart", cartService.getCartItems(user.getId()));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }


    }

}
