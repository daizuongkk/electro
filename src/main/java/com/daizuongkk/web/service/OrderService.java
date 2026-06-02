package com.daizuongkk.web.service;

import com.daizuongkk.web.dto.request.AdminOrderSearchRequest;
import com.daizuongkk.web.dto.response.CartItemResponse;
import com.daizuongkk.web.model.Order;
import com.daizuongkk.web.model.OrderItem;
import com.daizuongkk.web.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;

public class OrderService {
    private final OrderRepository orderRepository = new OrderRepository();
    private final CartService cartService = new CartService();

    public enum CheckoutStatus {
        SUCCESS,
        EMPTY_CART,
        INVALID_INPUT,
        PAYMENT_FAILED,
        PAYMENT_DECLINED,
        PAYMENT_REQUIRES_RETRY,
        FAILED
    }

    public record CheckoutResult(CheckoutStatus status, Order order, String message, String transactionId) {
        public CheckoutResult(CheckoutStatus status, Order order) {
            this(status, order, null, null);
        }
    }

    public record PaymentRequest(
            String method,
            String cardName,
            String cardNumber,
            String cardExpiry,
            String cardCvv,
            String bankTransferContent) {
    }

    private record PaymentResult(boolean paid, CheckoutStatus status, String message, String transactionId) {
    }

    public List<Order> findOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> findAdminOrders(AdminOrderSearchRequest filters, int page, int size) {
        return orderRepository.findAdminPage(filters, page, size);
    }

    public List<Order> findAdminOrdersForExport(AdminOrderSearchRequest filters) {
        return orderRepository.findAdminForExport(filters, 10000);
    }

    public long countAdminOrders(AdminOrderSearchRequest filters) {
        return orderRepository.countAdmin(filters);
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
        return checkout(userId, recipientName, phone, address,
                new PaymentRequest(paymentMethod, null, null, null, null, null),
                selectedProductIds);
    }

    public CheckoutResult checkout(Long userId,
            String recipientName,
            String phone,
            String address,
            PaymentRequest paymentRequest,
            Set<Long> selectedProductIds) {
        if (userId == null || userId <= 0 || isBlank(recipientName) || isBlank(phone) || isBlank(address)) {
            return new CheckoutResult(CheckoutStatus.INVALID_INPUT, null);
        }

        String normalizedPayment = normalizePayment(paymentRequest == null ? null : paymentRequest.method());
        if (normalizedPayment == null) {
            return new CheckoutResult(CheckoutStatus.INVALID_INPUT, null);
        }

        List<CartItemResponse> cartItems = selectedProductIds == null || selectedProductIds.isEmpty()
                ? cartService.getCartItems(userId)
                : cartService.getCartItems(userId, selectedProductIds);
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

        PaymentResult paymentResult = processPayment(normalizedPayment, paymentRequest, totalPrice);
        if (paymentResult.status() != CheckoutStatus.SUCCESS) {
            return new CheckoutResult(paymentResult.status(), null, paymentResult.message(), null);
        }

        String status = paymentResult.paid() ? "PAID" : "PENDING";
        Order order = Order.builder()
                .userId(userId)
                .totalPrice(totalPrice)
                .status(status)
                .recipientName(recipientName.trim())
                .phone(phone.trim())
                .address(appendPaymentSummary(address.trim(), normalizedPayment, paymentResult.transactionId()))
                .build();

        Order createdOrder = orderRepository.create(order, orderItems);
        if (createdOrder == null) {
            return new CheckoutResult(CheckoutStatus.FAILED, null);
        }

        return new CheckoutResult(CheckoutStatus.SUCCESS, createdOrder, paymentResult.message(), paymentResult.transactionId());
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
            case "COD", "MOCK_CARD", "BANK_TRANSFER" -> normalized;
            default -> null;
        };
    }

    private PaymentResult processPayment(String method, PaymentRequest request, double amount) {
        if ("COD".equals(method)) {
            return new PaymentResult(false, CheckoutStatus.SUCCESS,
                    "Đơn hàng COD đã được ghi nhận. Nhân viên sẽ xác nhận trước khi giao.", null);
        }
        if ("MOCK_CARD".equals(method)) {
            return processCardPayment(request, amount);
        }
        if ("BANK_TRANSFER".equals(method)) {
            return processBankTransfer(request);
        }
        return new PaymentResult(false, CheckoutStatus.INVALID_INPUT, "Phương thức thanh toán không hợp lệ.", null);
    }

    private PaymentResult processCardPayment(PaymentRequest request, double amount) {
        if (request == null || isBlank(request.cardName()) || isBlank(request.cardNumber())
                || isBlank(request.cardExpiry()) || isBlank(request.cardCvv())) {
            return new PaymentResult(false, CheckoutStatus.INVALID_INPUT, "Vui lòng nhập đầy đủ thông tin thẻ.", null);
        }

        String cardNumber = digitsOnly(request.cardNumber());
        String cvv = digitsOnly(request.cardCvv());
        if (cardNumber.length() < 12 || cardNumber.length() > 19 || !luhnValid(cardNumber)) {
            return new PaymentResult(false, CheckoutStatus.PAYMENT_DECLINED, "Số thẻ không hợp lệ.", null);
        }
        if (cvv.length() < 3 || cvv.length() > 4) {
            return new PaymentResult(false, CheckoutStatus.INVALID_INPUT, "CVV không hợp lệ.", null);
        }
        if (!expiryValid(request.cardExpiry())) {
            return new PaymentResult(false, CheckoutStatus.PAYMENT_DECLINED, "Thẻ đã hết hạn hoặc ngày hết hạn không hợp lệ.", null);
        }

        return switch (cardNumber) {
            case "4000000000000002" -> new PaymentResult(false, CheckoutStatus.PAYMENT_DECLINED,
                    "Ngân hàng từ chối giao dịch. Vui lòng dùng thẻ khác.", null);
            case "4000000000009995" -> new PaymentResult(false, CheckoutStatus.PAYMENT_REQUIRES_RETRY,
                    "Thẻ không đủ hạn mức cho giao dịch này.", null);
            case "4000000000000069" -> new PaymentResult(false, CheckoutStatus.PAYMENT_DECLINED,
                    "Thẻ đã hết hạn.", null);
            default -> new PaymentResult(true, CheckoutStatus.SUCCESS,
                    "Thanh toán thẻ thành công.", buildTransactionId("CARD", amount));
        };
    }

    private PaymentResult processBankTransfer(PaymentRequest request) {
        if (request == null || isBlank(request.bankTransferContent())) {
            return new PaymentResult(false, CheckoutStatus.INVALID_INPUT, "Vui lòng xác nhận thông tin chuyển khoản.", null);
        }
        return new PaymentResult(false, CheckoutStatus.SUCCESS,
                "Đơn hàng chuyển khoản đã được ghi nhận và đang chờ đối soát.",
                request.bankTransferContent().trim());
    }

    private String appendPaymentSummary(String address, String method, String transactionId) {
        String label = switch (method) {
            case "MOCK_CARD" -> "Thẻ thanh toán";
            case "BANK_TRANSFER" -> "Chuyển khoản ngân hàng";
            case "COD" -> "Thanh toán khi nhận hàng";
            default -> method;
        };
        StringBuilder summary = new StringBuilder(address);
        summary.append(" | Thanh toán: ").append(label);
        if (transactionId != null && !transactionId.isBlank()) {
            if ("BANK_TRANSFER".equals(method)) {
                summary.append(" | Nội dung CK: ").append(transactionId);
            } else {
                summary.append(" | Mã GD: ").append(transactionId);
            }
        }
        return summary.toString();
    }

    private String buildTransactionId(String prefix, double amount) {
        String amountPart = String.valueOf(Math.round(Math.max(amount, 0D)));
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
        return "PAY-" + prefix + "-" + amountPart + "-" + randomPart;
    }

    private String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private boolean expiryValid(String expiry) {
        String normalized = expiry == null ? "" : expiry.trim();
        if (!normalized.matches("^(0[1-9]|1[0-2])/[0-9]{2}$")) {
            return false;
        }
        try {
            YearMonth cardMonth = YearMonth.parse(normalized, DateTimeFormatter.ofPattern("MM/yy"));
            return !cardMonth.isBefore(YearMonth.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean luhnValid(String number) {
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.digit(number.charAt(i), 10);
            if (digit < 0) {
                return false;
            }
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
