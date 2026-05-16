package com.daizuongkk.web.service;

import com.daizuongkk.web.dto.response.CartItemResponse;
import com.daizuongkk.web.dto.response.ProductResponse;
import com.daizuongkk.web.model.Order;
import com.daizuongkk.web.model.OrderItem;
import com.daizuongkk.web.repository.OrderRepository;
import com.daizuongkk.web.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BusinessCriticalFlowsIT {

    @Test
    void checkoutCodShouldCreatePendingOrderWithCartItems() throws Exception {
        OrderRepository orderRepository = mock(OrderRepository.class);
        CartService cartService = mock(CartService.class);
        OrderService orderService = orderService(orderRepository, cartService);

        when(cartService.getCartItems(10L)).thenReturn(List.of(
                cartItem(1L, "Laptop", 12_000_000D, 1L),
                cartItem(2L, "Mouse", 300_000D, 2L)
        ));
        when(orderRepository.create(any(Order.class), anyList())).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(101L);
            return order;
        });

        OrderService.CheckoutResult result = orderService.checkout(
                10L,
                "Nguyen Van A",
                "0900000000",
                "Ha Noi",
                "COD",
                Set.of()
        );

        assertEquals(OrderService.CheckoutStatus.SUCCESS, result.status());
        assertNotNull(result.order());
        assertEquals(101L, result.order().getId());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(orderRepository).create(orderCaptor.capture(), itemsCaptor.capture());

        assertEquals("PENDING", orderCaptor.getValue().getStatus());
        assertEquals(12_600_000D, orderCaptor.getValue().getTotalPrice());
        assertEquals(2, itemsCaptor.getValue().size());
        assertTrue(orderCaptor.getValue().getAddress().contains("Thanh toán khi nhận hàng"));
    }

    @Test
    void checkoutCardShouldCreatePaidOrderWhenPaymentIsValid() throws Exception {
        OrderRepository orderRepository = mock(OrderRepository.class);
        CartService cartService = mock(CartService.class);
        OrderService orderService = orderService(orderRepository, cartService);

        when(cartService.getCartItems(20L)).thenReturn(List.of(cartItem(3L, "Phone", 9_000_000D, 1L)));
        when(orderRepository.create(any(Order.class), anyList())).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(202L);
            return order;
        });

        OrderService.CheckoutResult result = orderService.checkout(
                20L,
                "Tran Thi B",
                "0911111111",
                "Da Nang",
                new OrderService.PaymentRequest("MOCK_CARD", "TRAN THI B", "4242424242424242", "12/99", "123", null),
                Set.of()
        );

        assertEquals(OrderService.CheckoutStatus.SUCCESS, result.status());
        assertNotNull(result.transactionId());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).create(orderCaptor.capture(), anyList());
        assertEquals("PAID", orderCaptor.getValue().getStatus());
        assertTrue(orderCaptor.getValue().getAddress().contains("Thẻ thanh toán"));
        assertTrue(orderCaptor.getValue().getAddress().contains("Mã GD"));
    }

    @Test
    void checkoutSelectedProductsShouldOnlyUseSelectedCartItems() throws Exception {
        OrderRepository orderRepository = mock(OrderRepository.class);
        CartService cartService = mock(CartService.class);
        OrderService orderService = orderService(orderRepository, cartService);

        Set<Long> selectedProductIds = Set.of(7L);
        when(cartService.getCartItems(30L, selectedProductIds)).thenReturn(List.of(
                cartItem(7L, "Keyboard", 700_000D, 1L)
        ));
        when(orderRepository.create(any(Order.class), anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        OrderService.CheckoutResult result = orderService.checkout(
                30L,
                "Le Van C",
                "0922222222",
                "Can Tho",
                "COD",
                selectedProductIds
        );

        assertEquals(OrderService.CheckoutStatus.SUCCESS, result.status());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(orderRepository).create(orderCaptor.capture(), itemsCaptor.capture());
        assertEquals(700_000D, orderCaptor.getValue().getTotalPrice());
        assertEquals(1, itemsCaptor.getValue().size());
        assertEquals(7L, itemsCaptor.getValue().get(0).getProductId());
    }

    @Test
    void customerCanCancelPendingOrderAndStatusBecomesCancelled() throws Exception {
        OrderRepository orderRepository = mock(OrderRepository.class);
        OrderService orderService = orderService(orderRepository, mock(CartService.class));

        when(orderRepository.findByIdAndUserId(301L, 40L)).thenReturn(Order.builder()
                .id(301L)
                .userId(40L)
                .status("PENDING")
                .build());
        when(orderRepository.updateStatus(301L, "CANCELLED")).thenReturn(true);

        assertTrue(orderService.cancelOrder(301L, 40L));
        verify(orderRepository).updateStatus(301L, "CANCELLED");
    }

    @Test
    void adminCompleteActionShouldMoveOrderToCompleted() throws Exception {
        OrderRepository orderRepository = mock(OrderRepository.class);
        OrderService orderService = orderService(orderRepository, mock(CartService.class));

        when(orderRepository.updateStatus(401L, "COMPLETED")).thenReturn(true);

        assertTrue(orderService.adminUpdateStatus(401L, "complete"));
        verify(orderRepository).updateStatus(401L, "COMPLETED");
    }

    @Test
    void completedBuyerCanCreateOneProductReview() throws Exception {
        ReviewRepository reviewRepository = mock(ReviewRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        ReviewService reviewService = reviewService(reviewRepository, orderRepository);

        when(orderRepository.hasCompletedOrderContainingProduct(50L, 8L)).thenReturn(true);
        when(reviewRepository.existsByProductIdAndUserId(8L, 50L)).thenReturn(false);
        when(reviewRepository.create(8L, 50L, "San pham tot", 5)).thenReturn(true);

        assertTrue(reviewService.addReview(8L, 50L, "San pham tot", 5));
        verify(reviewRepository).create(8L, 50L, "San pham tot", 5);
    }

    private OrderService orderService(OrderRepository orderRepository, CartService cartService) throws Exception {
        OrderService orderService = new OrderService();
        setField(orderService, "orderRepository", orderRepository);
        setField(orderService, "cartService", cartService);
        return orderService;
    }

    private ReviewService reviewService(ReviewRepository reviewRepository, OrderRepository orderRepository) throws Exception {
        ReviewService reviewService = new ReviewService(reviewRepository, mock(UserService.class));
        setField(reviewService, "orderRepository", orderRepository);
        return reviewService;
    }

    private CartItemResponse cartItem(Long productId, String productName, Double price, Long quantity) {
        return CartItemResponse.builder()
                .product(ProductResponse.builder()
                        .id(productId)
                        .name(productName)
                        .price(price)
                        .build())
                .quantity(quantity)
                .build();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
