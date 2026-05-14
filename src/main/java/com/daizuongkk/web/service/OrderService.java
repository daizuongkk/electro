package com.daizuongkk.web.service;

import com.daizuongkk.web.dto.response.CartItemResponse;
import com.daizuongkk.web.model.Order;
import com.daizuongkk.web.model.OrderItem;
import com.daizuongkk.web.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OrderService {
    private final OrderRepository orderRepository = new OrderRepository();
    private final CartService cartService = new CartService();

    public enum CheckoutStatus {
        SUCCESS,
        EMPTY_CART,
        INVALID_INPUT,
        PAYMENT_FAILED,
        FAILED
    }

    public static class CheckoutResult {
        private final CheckoutStatus status;
        private final Order order;

        public CheckoutResult(CheckoutStatus status, Order order) {
            this.status = status;
            this.order = order;
        }

        public CheckoutStatus getStatus() {
            return status;
        }

        public Order getOrder() {
            return order;
        }
    }

    public List<Order> findOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    public long countOrdersByUserId(Long userId) {
        return orderRepository.countByUserId(userId);
    }

    public Order findOrder(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId);
    }

    public CheckoutResult checkout(Long userId,
            String recipientName,
            String phone,
            String address,
            String paymentMethod,
            Set<Long> selectedProductIds) {
        if (userId == null || userId <= 0 || isBlank(recipientName) || isBlank(phone) || isBlank(address)) {
            return new CheckoutResult(CheckoutStatus.INVALID_INPUT, null);
        }

        String normalizedPayment = normalizePayment(paymentMethod);
        if (normalizedPayment == null) {
            return new CheckoutResult(CheckoutStatus.INVALID_INPUT, null);
        }

        if ("MOCK_FAIL".equals(normalizedPayment)) {
            return new CheckoutResult(CheckoutStatus.PAYMENT_FAILED, null);
        }

        List<CartItemResponse> cartItems = cartService.getCartItems(userId, selectedProductIds);
        if (cartItems.isEmpty()) {
            return new CheckoutResult(CheckoutStatus.EMPTY_CART, null);
        }

        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0D;
        for (CartItemResponse cartItem : cartItems) {
            if (cartItem.getProduct() == null || cartItem.getProduct().getPrice() == null || cartItem.getQuantity() == null) {
                return new CheckoutResult(CheckoutStatus.FAILED, null);
            }

            double price = cartItem.getProduct().getPrice();
            long quantity = cartItem.getQuantity();
            totalPrice += price * quantity;
            orderItems.add(OrderItem.builder()
                    .productId(cartItem.getProduct().getId())
                    .price(price)
                    .quantity(quantity)
                    .build());
        }

        String status = "COD".equals(normalizedPayment) ? "PENDING" : "PAID";
        Order order = Order.builder()
                .userId(userId)
                .totalPrice(totalPrice)
                .status(status)
                .recipientName(recipientName.trim())
                .phone(phone.trim())
                .address(address.trim())
                .build();

        Order createdOrder = orderRepository.create(order, orderItems);
        if (createdOrder == null) {
            return new CheckoutResult(CheckoutStatus.FAILED, null);
        }

        return new CheckoutResult(CheckoutStatus.SUCCESS, createdOrder);
    }

    public boolean cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId);
        if (order == null || !canCustomerCancel(order.getStatus())) {
            return false;
        }
        return orderRepository.updateStatus(orderId, "CANCELLED");
    }

    public boolean adminUpdateStatus(Long orderId, String action) {
        if (orderId == null || action == null) {
            return false;
        }

        String nextStatus = switch (action.trim().toLowerCase()) {
            case "confirm" -> "PAID";
            case "ship" -> "SHIPPED";
            case "complete" -> "COMPLETED";
            case "cancel" -> "CANCELLED";
            default -> null;
        };
        return nextStatus != null && orderRepository.updateStatus(orderId, nextStatus);
    }

    public boolean canCustomerCancel(String status) {
        return "PENDING".equals(status) || "PAID".equals(status);
    }

    public String getStatusLabel(String status) {
        if (status == null) {
            return "Không xác định";
        }

        return switch (status) {
            case "PENDING" -> "Chờ xử lý";
            case "PAID" -> "Đã thanh toán";
            case "SHIPPED" -> "Đang giao";
            case "COMPLETED" -> "Hoàn tất";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }

    private String normalizePayment(String paymentMethod) {
        if (paymentMethod == null) {
            return null;
        }

        String normalized = paymentMethod.trim().toUpperCase();
        return switch (normalized) {
            case "COD", "MOCK_CARD", "MOCK_WALLET", "MOCK_FAIL" -> normalized;
            default -> null;
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
