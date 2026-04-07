package com.daizuongkk.web.api;


import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.service.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "CartAPI", value = "/api/carts/*")
public class CartAPI  extends HttpServlet {

    CartService cartService = new CartService();

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
           Long productId = Long.parseLong(request.getPathInfo().substring(1));


         UserResponse user = (UserResponse)   request.getSession().getAttribute("account");

              cartService.addToCart(user.getId(), productId);
    }
}
