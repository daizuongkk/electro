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

        UserResponse user = (UserResponse) session.getAttribute("account");

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

        UserResponse user = (UserResponse) session.getAttribute("account");

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Product ID is required\"}");
            return;
        }

        try {
            Long productId = Long.parseLong(pathInfo.substring(1));

            cartService.addToCart(user.getId(), productId);

            response.setStatus(HttpServletResponse.SC_OK);
            List<CartItemResponse> cart = cartService.getCartItems(user.getId());
            session.setAttribute("cart", cart);
            out.write("{\"message\":\"Added to cart\"}");

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Invalid product ID\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Server error\"}");
        }
    }

    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);

        UserResponse user = (UserResponse) session.getAttribute("account");
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
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }


    }

}
