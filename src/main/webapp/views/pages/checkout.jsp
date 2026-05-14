<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <c:set var="pageTitle" value="Electro - Thanh Toán"/>
    <%@ include file="../commons/head.jsp" %>
</head>
<body id="checkout-page">
<fmt:setLocale value="vi_VN"/>
<c:set var="cartItems" value="${empty cartItems ? sessionScope.cart : cartItems}"/>
<c:choose>
    <c:when test="${not empty submittedRecipientName}">
        <c:set var="recipientNameDefault" value="${submittedRecipientName}"/>
    </c:when>
    <c:when test="${not empty sessionScope.account.firstName or not empty sessionScope.account.lastName}">
        <c:set var="recipientNameDefault" value="${sessionScope.account.firstName} ${sessionScope.account.lastName}"/>
    </c:when>
    <c:otherwise>
        <c:set var="recipientNameDefault" value="${sessionScope.account.username}"/>
    </c:otherwise>
</c:choose>
<c:set var="phoneDefault" value="${not empty submittedPhone ? submittedPhone : sessionScope.account.phone}"/>
<c:set var="emailDefault" value="${not empty submittedEmail ? submittedEmail : sessionScope.account.email}"/>
<c:set var="paymentDefault" value="${not empty submittedPaymentMethod ? submittedPaymentMethod : 'MOCK_CARD'}"/>

<%@ include file="../commons/header.jsp" %>
<jsp:include page="../commons/navigation.jsp"/>

<div id="breadcrumb" class="section">
    <div class="container">
        <div class="row">
            <div class="col-md-12">
                <h3 class="breadcrumb-header">Thanh Toán</h3>
                <ul class="breadcrumb-tree">
                    <li><a href="home">Trang chủ</a></li>
                    <li><a href="cart">Giỏ hàng</a></li>
                    <li class="active">Thanh toán</li>
                </ul>
            </div>
        </div>
    </div>
</div>

<div class="section checkout-section">
    <div class="container">
        <c:if test="${not empty checkoutError}">
            <div class="js-popup-message hidden" data-type="danger" data-message="${fn:escapeXml(checkoutError)}"></div>
        </c:if>

        <c:choose>
            <c:when test="${empty cartItems}">
                <div class="checkout-empty-state">
                    <i class="fa fa-shopping-cart"></i>
                    <h3>Giỏ hàng đang trống</h3>
                    <p>Thêm sản phẩm vào giỏ trước khi tiến hành thanh toán.</p>
                    <a href="shop" class="primary-btn">Tiếp tục mua sắm</a>
                </div>
            </c:when>
            <c:otherwise>
                <form method="post" action="checkout" class="checkout-form">
                    <div class="row">
                        <div class="col-md-7">
                            <div class="billing-details checkout-panel">
                                <div class="section-title">
                                    <h3 class="title">Thông tin giao hàng</h3>
                                </div>
                                <div class="form-group">
                                    <label for="recipientName">Người nhận</label>
                                    <input class="input" id="recipientName" type="text" name="recipientName"
                                           value="${fn:escapeXml(recipientNameDefault)}" required>
                                </div>
                                <div class="form-group">
                                    <label for="email">Email nhận thông báo</label>
                                    <input class="input" id="email" type="email" name="email"
                                           value="${fn:escapeXml(emailDefault)}" required>
                                </div>
                                <div class="form-group">
                                    <label for="phone">Số điện thoại</label>
                                    <input class="input" id="phone" type="tel" name="phone"
                                           value="${fn:escapeXml(phoneDefault)}" required>
                                </div>
                                <div class="row">
                                    <div class="col-sm-4">
                                        <div class="form-group">
                                            <label for="province">Tỉnh/Thành phố</label>
                                            <input class="input" id="province" name="province" type="text"
                                                   value="${fn:escapeXml(submittedProvince)}" required>
                                        </div>
                                    </div>
                                    <div class="col-sm-4">
                                        <div class="form-group">
                                            <label for="district">Quận/Huyện</label>
                                            <input class="input" id="district" name="district" type="text"
                                                   value="${fn:escapeXml(submittedDistrict)}" required>
                                        </div>
                                    </div>
                                    <div class="col-sm-4">
                                        <div class="form-group">
                                            <label for="ward">Phường/Xã</label>
                                            <input class="input" id="ward" name="ward" type="text"
                                                   value="${fn:escapeXml(submittedWard)}" required>
                                        </div>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="addressLine">Số nhà, tên đường</label>
                                    <input class="input" id="addressLine" name="addressLine" type="text"
                                           value="${fn:escapeXml(submittedAddressLine)}" required>
                                </div>
                                <div class="order-notes">
                                    <textarea class="input" name="note" rows="3" placeholder="Ghi chú cho đơn hàng">${fn:escapeXml(submittedNote)}</textarea>
                                </div>
                            </div>

                            <div class="payment-method checkout-panel">
                                <div class="section-title">
                                    <h3 class="title">Phương thức thanh toán</h3>
                                </div>
                                <div class="input-radio">
                                    <input type="radio" name="paymentMethod" id="payment-mock-card" value="MOCK_CARD"
                                           ${paymentDefault == 'MOCK_CARD' ? 'checked' : ''}>
                                    <label for="payment-mock-card">
                                        <span></span>
                                        Thanh toán mock bằng thẻ
                                    </label>
                                    <div class="caption">
                                        <p>Giao dịch giả lập thành công ngay lập tức. Nhập thông tin thẻ mock để kiểm tra luồng thanh toán.</p>
                                        <div class="row checkout-payment-fields">
                                            <div class="col-sm-12"><input class="input" type="text" name="cardName" placeholder="Tên trên thẻ"></div>
                                            <div class="col-sm-12"><input class="input" type="text" name="cardNumber" placeholder="4242 4242 4242 4242"></div>
                                            <div class="col-sm-6"><input class="input" type="text" name="cardExpiry" placeholder="MM/YY"></div>
                                            <div class="col-sm-6"><input class="input" type="text" name="cardCvv" placeholder="CVV"></div>
                                        </div>
                                    </div>
                                </div>
                                <div class="input-radio">
                                    <input type="radio" name="paymentMethod" id="payment-mock-wallet" value="MOCK_WALLET"
                                           ${paymentDefault == 'MOCK_WALLET' ? 'checked' : ''}>
                                    <label for="payment-mock-wallet">
                                        <span></span>
                                        Ví điện tử mock
                                    </label>
                                    <div class="caption">
                                        <p>Mô phỏng thanh toán ví điện tử trong môi trường phát triển.</p>
                                        <div class="checkout-payment-fields">
                                            <input class="input" type="tel" name="walletPhone" placeholder="Số điện thoại ví điện tử">
                                        </div>
                                    </div>
                                </div>
                                <div class="input-radio">
                                    <input type="radio" name="paymentMethod" id="payment-cod" value="COD"
                                           ${paymentDefault == 'COD' ? 'checked' : ''}>
                                    <label for="payment-cod">
                                        <span></span>
                                        Thanh toán khi nhận hàng
                                    </label>
                                    <div class="caption">
                                        <p>Đơn hàng sẽ được ghi nhận ở trạng thái chờ xử lý.</p>
                                    </div>
                                </div>
                                <div class="input-radio">
                                    <input type="radio" name="paymentMethod" id="payment-mock-fail" value="MOCK_FAIL"
                                           ${paymentDefault == 'MOCK_FAIL' ? 'checked' : ''}>
                                    <label for="payment-mock-fail">
                                        <span></span>
                                        Mock lỗi thanh toán
                                    </label>
                                    <div class="caption">
                                        <p>Dùng để kiểm tra giao diện khi thanh toán thất bại.</p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-5">
                            <div class="order-details checkout-order-panel">
                                <div class="section-title text-center">
                                    <h3 class="title">Đơn hàng của bạn</h3>
                                </div>
                                <div class="order-summary">
                                    <div class="order-col">
                                        <div><strong>Sản phẩm</strong></div>
                                        <div><strong>Tổng</strong></div>
                                    </div>
                                    <div class="order-products">
                                        <c:set var="subtotal" value="0"/>
                                        <c:forEach var="item" items="${cartItems}">
                                            <c:set var="lineTotal" value="${item.product.price * item.quantity}"/>
                                            <c:set var="subtotal" value="${subtotal + lineTotal}"/>
                                            <div class="order-col checkout-order-item">
                                                <div>
                                                    <span class="checkout-item-qty">${item.quantity}x</span>
                                                    ${fn:escapeXml(item.product.name)}
                                                </div>
                                                <div>
                                                    <fmt:formatNumber value="${lineTotal}" type="currency" currencySymbol="₫"/>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                    <div class="order-col">
                                        <div>Vận chuyển</div>
                                        <div><strong>Miễn phí</strong></div>
                                    </div>
                                    <div class="order-col">
                                        <div><strong>Tổng cộng</strong></div>
                                        <div>
                                            <strong class="order-total">
                                                <fmt:formatNumber value="${subtotal}" type="currency" currencySymbol="₫"/>
                                            </strong>
                                        </div>
                                    </div>
                                </div>
                                <div class="input-checkbox">
                                    <input type="checkbox" id="terms" required>
                                    <label for="terms">
                                        <span></span>
                                        Tôi đã kiểm tra thông tin đơn hàng
                                    </label>
                                </div>
                                <button type="submit" class="primary-btn order-submit">
                                    <i class="fa fa-check"></i>
                                    Đặt hàng
                                </button>
                            </div>
                        </div>
                    </div>
                </form>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%@ include file="../commons/footer.jsp" %>
<%@ include file="../commons/script.jsp" %>
</body>
</html>
