package com.daizuongkk.web.controller.web;

import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.service.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "Cart", value = "/cart")
public class CartController extends HttpServlet {


    CartService cartService = new CartService();


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {






    }
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("views/pages/cart.jsp").forward(request, response);
    }

}
