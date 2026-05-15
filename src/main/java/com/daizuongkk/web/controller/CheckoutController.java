package com.daizuongkk.web.controller;

import com.daizuongkk.web.dto.response.CartItemResponse;
import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.service.CartService;
import com.daizuongkk.web.service.OrderService;
import com.daizuongkk.web.util.FlashUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(name = "Checkout", value = "/checkout")
public class CheckoutController extends HttpServlet {
    private CartService cartService;
    private OrderService orderService;

    @Override
    public void init() {
        this.cartService = new CartService();
        this.orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserResponse account = getAccount(request);
        HttpSession session = request.getSession(false);
        Set<Long> selectedProductIds = getSelectedProductIds(session);
        List<CartItemResponse> cartItems = hasCheckoutSelection(session)
                ? cartService.getCartItems(account.getId(), selectedProductIds)
                : cartService.getCartItems(account.getId());
        request.setAttribute("cartItems", cartItems);
        FlashUtils.consume(request,
                "checkoutError",
                "submittedRecipientName",
                "submittedPhone",
                "submittedEmail",
                "submittedProvince",
                "submittedDistrict",
                "submittedWard",
                "submittedAddressLine",
                "submittedNote",
                "submittedPaymentMethod");
        request.getRequestDispatcher("/views/pages/checkout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        UserResponse account = getAccount(request);
        String action = request.getParameter("action");

        if ("prepare".equals(action)) {
            Set<Long> selectedProductIds = parseProductIds(request.getParameterValues("productIds"));
            if (selectedProductIds.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/cart?checkoutError=emptySelection");
                return;
            }
            request.getSession().setAttribute("checkoutProductIds", selectedProductIds);
            response.sendRedirect(request.getContextPath() + "/checkout");
            return;
        }

        Set<Long> selectedProductIds = getSelectedProductIds(request.getSession(false));

        OrderService.CheckoutResult result = orderService.checkout(
                account.getId(),
                request.getParameter("recipientName"),
                request.getParameter("phone"),
                buildAddress(request),
                buildPaymentRequest(request),
                selectedProductIds
        );

        if (result.status() == OrderService.CheckoutStatus.SUCCESS) {
            request.getSession().removeAttribute("checkoutProductIds");
            request.getSession().setAttribute("cart", cartService.getCartItems(account.getId()));
            request.getSession().setAttribute("orderCount", orderService.countOrdersByUserId(account.getId()));
            response.sendRedirect(request.getContextPath() + "/orders?placed=" + result.order().getId());
            return;
        }

        FlashUtils.putAll(request, Map.of(
                "checkoutError", getErrorMessage(result),
                "submittedRecipientName", trim(request.getParameter("recipientName")),
                "submittedPhone", trim(request.getParameter("phone")),
                "submittedEmail", trim(request.getParameter("email")),
                "submittedProvince", trim(request.getParameter("province")),
                "submittedDistrict", trim(request.getParameter("district")),
                "submittedWard", trim(request.getParameter("ward")),
                "submittedAddressLine", trim(request.getParameter("addressLine")),
                "submittedNote", trim(request.getParameter("note")),
                "submittedPaymentMethod", trim(request.getParameter("paymentMethod"))
        ));
        response.sendRedirect(request.getContextPath() + "/checkout");
    }

    private UserResponse getAccount(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (UserResponse) session.getAttribute("account");
    }

    private String getErrorMessage(OrderService.CheckoutResult result) {
        if (result != null && result.message() != null && !result.message().isBlank()) {
            return result.message();
        }

        return switch (result == null ? OrderService.CheckoutStatus.FAILED : result.status()) {
            case EMPTY_CART -> "Giỏ hàng đang trống. Vui lòng thêm sản phẩm trước khi đặt hàng.";
            case INVALID_INPUT -> "Vui lòng nhập đầy đủ thông tin giao hàng và chọn phương thức thanh toán.";
            case PAYMENT_FAILED, PAYMENT_DECLINED -> "Thanh toán không thành công. Vui lòng chọn phương thức khác hoặc thử lại.";
            case PAYMENT_REQUIRES_RETRY -> "Thanh toán cần thử lại. Vui lòng kiểm tra thông tin và xác nhận lại.";
            case FAILED -> "Không thể tạo đơn hàng. Vui lòng thử lại sau.";
            case SUCCESS -> "";
        };
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String buildAddress(HttpServletRequest request) {
        String addressLine = trim(request.getParameter("addressLine"));
        String ward = trim(request.getParameter("ward"));
        String district = trim(request.getParameter("district"));
        String province = trim(request.getParameter("province"));
        String note = trim(request.getParameter("note"));

        StringBuilder address = new StringBuilder(addressLine);
        appendAddressPart(address, ward);
        appendAddressPart(address, district);
        appendAddressPart(address, province);
        if (!note.isBlank()) {
            address.append(" | Ghi chú: ").append(note);
        }
        return address.toString();
    }

    private OrderService.PaymentRequest buildPaymentRequest(HttpServletRequest request) {
        return new OrderService.PaymentRequest(
                request.getParameter("paymentMethod"),
                request.getParameter("cardName"),
                request.getParameter("cardNumber"),
                request.getParameter("cardExpiry"),
                request.getParameter("cardCvv"),
                request.getParameter("bankTransferContent")
        );
    }

    private void appendAddressPart(StringBuilder address, String value) {
        if (!value.isBlank()) {
            if (!address.isEmpty()) {
                address.append(", ");
            }
            address.append(value);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<Long> getSelectedProductIds(HttpSession session) {
        if (session == null) {
            return Set.of();
        }

        Object value = session.getAttribute("checkoutProductIds");
        if (value instanceof Set<?>) {
            return (Set<Long>) value;
        }
        return Set.of();
    }

    private boolean hasCheckoutSelection(HttpSession session) {
        return session != null && session.getAttribute("checkoutProductIds") instanceof Set<?>;
    }

    private Set<Long> parseProductIds(String[] values) {
        if (values == null || values.length == 0) {
            return Set.of();
        }

        return Arrays.stream(values)
                .map(this::parseLong)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
    }

    private Long parseLong(String value) {
        try {
            return value == null ? null : Long.parseLong(value.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
