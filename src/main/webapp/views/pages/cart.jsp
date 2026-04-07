<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Electro - Giỏ Hàng"/>
    <%@ include file="../commons/head.jsp" %>
</head>
<body id="cart-page">
<fmt:setLocale value="vi_VN"/>
<c:set var="cartItems" value="${sessionScope.cart}"/>
<c:set var="cartCount" value="${empty cartItems ? 0 : cartItems.size()}"/>
<c:url var="fallbackProductImage" value="/assets/img/product01.png"/>

<%@ include file="../commons/header.jsp" %>

<div id="breadcrumb" class="section">
    <div class="container">
        <div class="row">
            <div class="col-md-12">
                <ul class="breadcrumb-tree">
                    <li><a href="home">Trang chủ</a></li>
                    <li class="active">Giỏ hàng</li>
                </ul>
            </div>
        </div>
    </div>
</div>

<div class="section">
    <div class="container">
        <div class="row">
            <div class="col-md-8">
                <div class="cart-panel">
                    <div class="cart-panel-header clearfix">
                        <h3 class="cart-title">Giỏ Hàng</h3>
                        <span id="cart-count-display" class="cart-count">${cartCount} sản phẩm</span>
                    </div>

                    <div id="cart-items-container">
                    <c:choose>
                        <c:when test="${empty cartItems}">
                            <div class="cart-empty-state text-center">
                                <i class="fa fa-shopping-cart"></i>
                                <h4>Giỏ hàng đang trống</h4>
                                <p>Hãy thêm sản phẩm để tiếp tục mua sắm.</p>
                                <a href="shop" class="primary-btn">Mua sắm ngay</a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table cart-table">
                                    <thead>
                                    <tr>
                                        <th class="text-center cart-select-col">
                                            <input id="toggle-select-all" type="checkbox" class="cart-item-check" onclick="selectAll(this)" aria-label="Chọn tất cả sản phẩm" checked="checked"/>
                                        </th>
                                        <th>Sản phẩm</th>
                                        <th class="text-center">Đơn giá</th>
                                        <th class="text-center">Số lượng</th>
                                        <th class="text-right">Tạm tính</th>
                                        <th class="text-center cart-action-col">Xóa</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:set var="subtotal" value="0"/>
                                    <c:forEach var="item" items="${cartItems}">
                                        <c:set var="lineTotal" value="${item.product.price * item.quantity}"/>
                                        <c:set var="subtotal" value="${subtotal + lineTotal}"/>

                                        <tr class="cart-row" data-unit-price="${item.product.price}">
                                            <td class="text-center cart-select-col">
                                                <input type="checkbox" class="cart-item-check" checked aria-label="Chọn sản phẩm ${item.product.name}">
                                            </td>
                                            <td>
                                                <div class="cart-product">
                                                    <a class="cart-product-image" href="products?id=${item.product.id}">
                                                        <img src="${not empty item.product.imageUrl ? item.product.imageUrl[0] : fallbackProductImage}" alt="${item.product.name}">
                                                    </a>
                                                    <div class="cart-product-info">
                                                        <h4><a href="products?id=${item.product.id}">${item.product.name}</a></h4>
                                                    </div>
                                                </div>
                                            </td>
                                            <td class="text-center cart-unit-price">
                                                <fmt:formatNumber value="${item.product.price}" type="currency" currencySymbol="₫"/>
                                            </td>
                                            <td class="text-center">
                                                <div class="cart-qty-control">
                                                    <button type="button" class="cart-qty-btn cart-qty-down" aria-label="Giảm số lượng">-</button>
                                                    <input type="number" class="cart-qty-input" min="1" value="${item.quantity}">
                                                    <button type="button" class="cart-qty-btn cart-qty-up" aria-label="Tăng số lượng">+</button>
                                                </div>
                                            </td>
                                            <td class="text-right cart-line-total">
                                                <strong>
                                                    <fmt:formatNumber value="${lineTotal}" type="currency" currencySymbol="₫"/>
                                                </strong>
                                            </td>
                                            <td class="text-center cart-action-col">
                                                <button type="button" class="cart-remove-btn" aria-label="Xóa sản phẩm ${item.product.name}">
                                                    <i class="fa fa-trash"></i>
                                                </button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>

                            <div id="cart-actions-panel" class="cart-actions clearfix">
                                <div class="pull-left cart-coupon">
                                    <input type="text" class="input" placeholder="Nhập mã giảm giá">
                                    <button type="button" class="primary-btn">Áp dụng</button>
                                </div>
                                <a href="shop" class="primary-btn pull-right">Tiếp tục mua sắm</a>
                            </div>
                        </c:otherwise>
                    </c:choose>
                    </div>
                </div>
            </div>

            <div class="col-md-4">
                <div class="cart-panel cart-summary-panel">
                    <h3 class="cart-title">Tổng Đơn Hàng</h3>

                    <c:set var="subtotal" value="0"/>
                    <c:forEach var="item" items="${cartItems}">
                        <c:set var="subtotal" value="${subtotal + (item.product.price * item.quantity)}"/>
                    </c:forEach>

                    <div class="cart-summary-line">
                        <span>Tạm tính</span>
                        <strong id="cart-subtotal-display">
                            <fmt:formatNumber value="${subtotal}" type="currency" currencySymbol="₫"/>
                        </strong>
                    </div>
                    <div class="cart-summary-line">
                        <span>Vận chuyển</span>
                        <strong>Miễn phí</strong>
                    </div>
                    <div class="cart-summary-line total">
                        <span>Tổng cộng</span>
                        <strong id="cart-total-display">
                            <fmt:formatNumber value="${subtotal}" type="currency" currencySymbol="₫"/>
                        </strong>
                    </div>

                    <button id="checkout-btn" type="button" class="primary-btn order-submit btn-block" ${empty cartItems ? 'disabled' : ''}>
                        Tiến hành thanh toán
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="newsletter" class="section">
    <div class="container">
        <div class="row">
            <div class="col-md-12">
                <div class="newsletter">
                    <p>Sign Up for the <strong>NEWSLETTER</strong></p>
                    <form>
                        <input class="input" type="email" placeholder="Enter Your Email">
                        <button class="newsletter-btn"><i class="fa fa-envelope"></i> Subscribe</button>
                    </form>
                    <ul class="newsletter-follow">
                        <li><a href="#"><i class="fa fa-facebook"></i></a></li>
                        <li><a href="#"><i class="fa fa-twitter"></i></a></li>
                        <li><a href="#"><i class="fa fa-instagram"></i></a></li>
                        <li><a href="#"><i class="fa fa-pinterest"></i></a></li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="../commons/footer.jsp" %>
<%@ include file="../commons/script.jsp" %>

<script>

    const toggle = document.getElementById('toggle-select-all');

    // Click "select all"
    toggle.addEventListener('change', function () {
        const checkboxes = document.querySelectorAll('#cart-page .cart-item-check');
        checkboxes.forEach(cb => cb.checked = this.checked);
    });

    // Click từng checkbox con
    document.addEventListener('change', function (e) {
        if (e.target.matches('#cart-page .cart-item-check')) {
            const checkboxes = document.querySelectorAll(
                '#cart-page .cart-item-check:not(#toggle-select-all)'
            );            toggle.checked = Array.from(checkboxes).every(cb => cb.checked);
        }
    });

    function formatCurrencyVND(value) {
        return new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'}).format(value || 0);
    }

    function updateCartSummaryFromUI() {
        var rows = document.querySelectorAll('#cart-page .cart-row');
        var subtotal = 0;

        rows.forEach(function (row) {
            var qtyInput = row.querySelector('.cart-qty-input');
            var lineTotalCell = row.querySelector('.cart-line-total strong');
            var unitPrice = parseFloat(row.getAttribute('data-unit-price') || '0');
            var quantity = parseInt(qtyInput.value, 10);

            if (isNaN(quantity) || quantity < 1) {
                quantity = 1;
                qtyInput.value = '1';
            }

            var lineTotal = unitPrice * quantity;
            subtotal += lineTotal;

            if (lineTotalCell) {
                lineTotalCell.textContent = formatCurrencyVND(lineTotal);
            }
        });

        var subtotalEl = document.getElementById('cart-subtotal-display');
        var totalEl = document.getElementById('cart-total-display');
        if (subtotalEl) {
            subtotalEl.textContent = formatCurrencyVND(subtotal);
        }
        if (totalEl) {
            totalEl.textContent = formatCurrencyVND(subtotal);
        }
    }

    function updateCartCountDisplay() {
        var rows = document.querySelectorAll('#cart-page .cart-row');
        var countEl = document.getElementById('cart-count-display');
        if (countEl) {
            countEl.textContent = rows.length + ' sản phẩm';
        }
    }

    function renderEmptyCartState() {
        return '' +
            '<div class="cart-empty-state text-center">' +
            '<i class="fa fa-shopping-cart"></i>' +
            '<h4>Giỏ hàng đang trống</h4>' +
            '<p>Hãy thêm sản phẩm để tiếp tục mua sắm.</p>' +
            '<a href="shop" class="primary-btn">Mua sắm ngay</a>' +
            '</div>';
    }

    function syncCartUIState() {
        var rows = document.querySelectorAll('#cart-page .cart-row');
        var itemsContainer = document.getElementById('cart-items-container');
        var actionsPanel = document.getElementById('cart-actions-panel');
        var checkoutBtn = document.getElementById('checkout-btn');

        updateCartCountDisplay();

        if (rows.length > 0) {
            if (actionsPanel) {
                actionsPanel.style.display = '';
            }
            if (checkoutBtn) {
                checkoutBtn.disabled = false;
            }
            return;
        }

        if (actionsPanel) {
            actionsPanel.style.display = 'none';
        }
        if (checkoutBtn) {
            checkoutBtn.disabled = true;
        }
        if (itemsContainer) {
            itemsContainer.innerHTML = renderEmptyCartState();
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        var page = document.getElementById('cart-page');
        if (!page) {
            return;
        }

        page.addEventListener('click', function (event) {
            var target = event.target;
            if (!target.classList.contains('cart-qty-btn')) {
                return;
            }

            var row = target.closest('.cart-row');
            if (!row) {
                return;
            }

            var input = row.querySelector('.cart-qty-input');
            var current = parseInt(input.value, 10);
            if (isNaN(current) || current < 1) {
                current = 1;
            }

            if (target.classList.contains('cart-qty-up')) {
                input.value = current + 1;
            } else {
                input.value = Math.max(1, current - 1);
            }

            updateCartSummaryFromUI();
        });

        page.addEventListener('click', function (event) {
            var removeButton = event.target.closest('.cart-remove-btn');
            if (!removeButton) {
                return;
            }

            var row = removeButton.closest('.cart-row');
            if (!row) {
                return;
            }

            row.remove();
            updateCartSummaryFromUI();
            syncCartUIState();
        });

        page.addEventListener('input', function (event) {
            if (event.target.classList.contains('cart-qty-input')) {
                updateCartSummaryFromUI();
            }
        });

        updateCartSummaryFromUI();
        syncCartUIState();
    });
</script>

</body>
</html>
