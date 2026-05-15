<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <c:set var="pageTitle" value="Electro - Thanh Toán"/>
    <%@ include file="../commons/head.jsp" %>
    <style>
        .checkout-processing-overlay {
            position: fixed;
            inset: 0;
            z-index: 3000;
            display: none;
            align-items: center;
            justify-content: center;
            background: rgba(251, 251, 252, 0.82);
            backdrop-filter: blur(2px);
        }

        .checkout-processing-overlay.is-active {
            display: flex;
        }

        .checkout-processing-box {
            width: min(420px, calc(100vw - 32px));
            border-radius: 8px;
            background: #fff;
            border: 1px solid #e4e7ed;
            box-shadow: 0 18px 45px rgba(21, 22, 29, 0.18);
            padding: 26px;
            text-align: left;
        }

        .checkout-processing-spinner {
            width: 38px;
            height: 38px;
            border-radius: 50%;
            border: 3px solid #e4e7ed;
            border-top-color: #d10024;
            animation: checkout-spin 0.75s linear infinite;
            margin: 0 auto 14px;
        }

        @keyframes checkout-spin {
            to {
                transform: rotate(360deg);
            }
        }

        .checkout-processing-title {
            color: #2b2d42;
            font-weight: 700;
            text-align: center;
            margin-bottom: 16px;
        }

        .checkout-processing-step {
            display: flex;
            align-items: center;
            gap: 10px;
            color: #8d99ae;
            font-size: 13px;
            margin-top: 9px;
        }

        .checkout-processing-step.is-active {
            color: #2b2d42;
            font-weight: 600;
        }

        .checkout-processing-step.is-done {
            color: #0a7f45;
        }

        .payment-note {
            border: 1px dashed #d7dbe7;
            border-radius: 6px;
            padding: 12px;
            background: #fbfbfc;
            color: #5f6678;
            font-size: 12px;
            margin-top: 10px;
        }

        .payment-note code {
            color: #2b2d42;
            background: #fff;
            padding: 1px 4px;
            border-radius: 4px;
        }

        .bank-transfer-overlay {
            position: fixed;
            inset: 0;
            z-index: 2990;
            display: none;
            align-items: center;
            justify-content: center;
            padding: 18px;
            background: rgba(15, 23, 42, 0.58);
            backdrop-filter: blur(3px);
        }

        .bank-transfer-overlay.is-active {
            display: flex;
        }

        .bank-transfer-modal {
            width: min(760px, 100%);
            max-height: calc(100vh - 36px);
            overflow-y: auto;
            border-radius: 8px;
            background: #fff;
            box-shadow: 0 24px 70px rgba(15, 23, 42, 0.24);
            border: 1px solid #e5e7eb;
        }

        .bank-transfer-header {
            display: flex;
            align-items: flex-start;
            justify-content: space-between;
            gap: 18px;
            padding: 22px 24px 16px;
            border-bottom: 1px solid #eef0f4;
        }

        .bank-transfer-header h3 {
            margin: 0 0 6px;
            color: #2b2d42;
            font-size: 20px;
        }

        .bank-transfer-header p {
            margin: 0;
            color: #687082;
            font-size: 13px;
        }

        .bank-transfer-close {
            width: 34px;
            height: 34px;
            border: 0;
            border-radius: 50%;
            background: #f3f4f6;
            color: #2b2d42;
        }

        .bank-transfer-body {
            display: grid;
            grid-template-columns: 250px 1fr;
            gap: 22px;
            padding: 22px 24px;
        }

        .bank-transfer-qr {
            text-align: center;
        }

        .bank-transfer-qr img {
            width: 220px;
            max-width: 100%;
            aspect-ratio: 1;
            object-fit: contain;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            padding: 10px;
            background: #fff;
        }

        .bank-transfer-row {
            display: grid;
            grid-template-columns: 150px 1fr auto;
            align-items: center;
            gap: 10px;
            padding: 12px 0;
            border-bottom: 1px solid #eef0f4;
            font-size: 13px;
        }

        .bank-transfer-row span:first-child {
            color: #687082;
        }

        .bank-transfer-value {
            color: #2b2d42;
            font-weight: 700;
            overflow-wrap: anywhere;
        }

        .bank-copy-btn {
            border: 1px solid #d7dbe7;
            border-radius: 4px;
            background: #fff;
            color: #2b2d42;
            font-size: 12px;
            padding: 5px 8px;
        }

        .bank-transfer-footer {
            display: flex;
            justify-content: flex-end;
            gap: 10px;
            padding: 16px 24px 22px;
            border-top: 1px solid #eef0f4;
        }

        .bank-transfer-secondary {
            border: 1px solid #d7dbe7;
            background: #fff;
            color: #2b2d42;
            padding: 10px 18px;
            border-radius: 4px;
            font-weight: 700;
        }

        @media (max-width: 767px) {
            .bank-transfer-body {
                grid-template-columns: 1fr;
            }

            .bank-transfer-row {
                grid-template-columns: 1fr;
            }

            .bank-copy-btn {
                justify-self: start;
            }
        }
    </style>
</head>
<body id="checkout-page">
<fmt:setLocale value="vi_VN"/>
<div id="checkoutProcessingOverlay" class="checkout-processing-overlay">
    <div class="checkout-processing-box">
        <div class="checkout-processing-spinner"></div>
        <div class="checkout-processing-title">Đang xử lý thanh toán</div>
        <div class="checkout-processing-step is-active" data-step="1"><i class="fa fa-circle-o"></i> Kiểm tra thông tin giao hàng</div>
        <div class="checkout-processing-step" data-step="2"><i class="fa fa-circle-o"></i> Kết nối hệ thống thanh toán</div>
        <div class="checkout-processing-step" data-step="3"><i class="fa fa-circle-o"></i> Xác thực giao dịch</div>
        <div class="checkout-processing-step" data-step="4"><i class="fa fa-circle-o"></i> Tạo đơn hàng</div>
    </div>
</div>
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
<c:set var="paymentDefault" value="${not empty submittedPaymentMethod ? submittedPaymentMethod : 'BANK_TRANSFER'}"/>

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
                                    <input type="radio" name="paymentMethod" id="payment-card" value="MOCK_CARD"
                                           ${paymentDefault == 'MOCK_CARD' ? 'checked' : ''}>
                                    <label for="payment-card">
                                        <span></span>
                                        Thanh toán bằng thẻ
                                    </label>
                                    <div class="caption">
                                        <p>Nhập thông tin thẻ để hệ thống xác thực giao dịch trước khi tạo đơn.</p>
                                        <div class="row checkout-payment-fields">
                                            <div class="col-sm-12"><input class="input" type="text" name="cardName" autocomplete="cc-name" placeholder="Tên trên thẻ"></div>
                                            <div class="col-sm-12"><input class="input" type="text" name="cardNumber" inputmode="numeric" autocomplete="cc-number" placeholder="Số thẻ"></div>
                                            <div class="col-sm-6"><input class="input" type="text" name="cardExpiry" inputmode="numeric" autocomplete="cc-exp" placeholder="MM/YY"></div>
                                            <div class="col-sm-6"><input class="input" type="text" name="cardCvv" inputmode="numeric" autocomplete="cc-csc" placeholder="CVV"></div>
                                        </div>
                                        <div class="payment-note">
                                            Thông tin thẻ được kiểm tra bảo mật trước khi đơn hàng được ghi nhận.
                                        </div>
                                    </div>
                                </div>
                                <div class="input-radio">
                                    <input type="radio" name="paymentMethod" id="payment-bank-transfer" value="BANK_TRANSFER"
                                           ${paymentDefault == 'BANK_TRANSFER' ? 'checked' : ''}>
                                    <label for="payment-bank-transfer">
                                        <span></span>
                                        Chuyển khoản ngân hàng
                                    </label>
                                    <div class="caption">
                                        <p>Thông tin tài khoản và mã QR sẽ hiển thị để bạn chuyển khoản đúng số tiền.</p>
                                        <div class="payment-note">
                                            Sau khi xác nhận đã chuyển khoản, đơn hàng sẽ được ghi nhận và chờ đối soát.
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
                                    <input type="hidden" id="checkoutTotalAmount" value="${subtotal}">
                                    <input type="hidden" name="bankTransferContent" id="bankTransferContent">
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
                <div class="bank-transfer-overlay" id="bankTransferOverlay" aria-hidden="true">
                    <div class="bank-transfer-modal" role="dialog" aria-modal="true" aria-labelledby="bankTransferTitle">
                        <div class="bank-transfer-header">
                            <div>
                                <h3 id="bankTransferTitle">Thông tin chuyển khoản</h3>
                                <p>Vui lòng chuyển đúng số tiền và nội dung để đơn hàng được xử lý nhanh hơn.</p>
                            </div>
                            <button type="button" class="bank-transfer-close" id="bankTransferClose" aria-label="Đóng">
                                <i class="fa fa-times"></i>
                            </button>
                        </div>
                        <div class="bank-transfer-body">
                            <div class="bank-transfer-qr">
                                <img id="bankQrImage" alt="Mã QR chuyển khoản">
                            </div>
                            <div>
                                <div class="bank-transfer-row">
                                    <span>Ngân hàng</span>
                                    <strong class="bank-transfer-value">Vietcombank</strong>
                                    <span></span>
                                </div>
                                <div class="bank-transfer-row">
                                    <span>Số tài khoản</span>
                                    <strong class="bank-transfer-value" id="bankAccountNumber">1020248888</strong>
                                    <button type="button" class="bank-copy-btn" data-copy-target="bankAccountNumber">Sao chép</button>
                                </div>
                                <div class="bank-transfer-row">
                                    <span>Chủ tài khoản</span>
                                    <strong class="bank-transfer-value" id="bankAccountName">CONG TY TNHH ELECTRO</strong>
                                    <button type="button" class="bank-copy-btn" data-copy-target="bankAccountName">Sao chép</button>
                                </div>
                                <div class="bank-transfer-row">
                                    <span>Số tiền</span>
                                    <strong class="bank-transfer-value" id="bankTransferAmount"></strong>
                                    <button type="button" class="bank-copy-btn" data-copy-target="bankTransferRawAmount">Sao chép</button>
                                </div>
                                <div class="bank-transfer-row">
                                    <span>Nội dung</span>
                                    <strong class="bank-transfer-value" id="bankTransferContentText"></strong>
                                    <button type="button" class="bank-copy-btn" data-copy-target="bankTransferContentText">Sao chép</button>
                                </div>
                                <span id="bankTransferRawAmount" hidden></span>
                            </div>
                        </div>
                        <div class="bank-transfer-footer">
                            <button type="button" class="bank-transfer-secondary" id="bankTransferBack">Chỉnh sửa thông tin</button>
                            <button type="button" class="primary-btn" id="bankTransferConfirm">
                                <i class="fa fa-check"></i>
                                Tôi đã chuyển khoản
                            </button>
                        </div>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%@ include file="../commons/footer.jsp" %>
<%@ include file="../commons/script.jsp" %>
<script>
    (function () {
        var form = document.querySelector('.checkout-form');
        var overlay = document.getElementById('checkoutProcessingOverlay');
        var bankOverlay = document.getElementById('bankTransferOverlay');
        if (!form || !overlay) {
            return;
        }

        var bankInfo = {
            code: 'VCB',
            accountNumber: '1020248888',
            accountName: 'CONG TY TNHH ELECTRO'
        };

        function digitsOnly(value) {
            return (value || '').replace(/\D/g, '');
        }

        function activePaymentMethod() {
            var selected = form.querySelector('input[name="paymentMethod"]:checked');
            return selected ? selected.value : '';
        }

        function getField(name) {
            return form.querySelector('[name="' + name + '"]');
        }

        function requireField(name, message) {
            var field = getField(name);
            if (!field || !field.value.trim()) {
                window.showElectroPopup(message, 'warning');
                if (field) {
                    field.focus();
                }
                return false;
            }
            return true;
        }

        function validatePaymentFields() {
            var method = activePaymentMethod();
            if (method === 'MOCK_CARD') {
                if (!requireField('cardName', 'Vui lòng nhập tên trên thẻ.')) return false;
                if (!requireField('cardNumber', 'Vui lòng nhập số thẻ.')) return false;
                if (!requireField('cardExpiry', 'Vui lòng nhập ngày hết hạn thẻ.')) return false;
                if (!requireField('cardCvv', 'Vui lòng nhập CVV.')) return false;

                var cardNumber = digitsOnly(getField('cardNumber').value);
                var cvv = digitsOnly(getField('cardCvv').value);
                if (cardNumber.length < 12 || cardNumber.length > 19) {
                    window.showElectroPopup('Số thẻ không hợp lệ.', 'warning');
                    getField('cardNumber').focus();
                    return false;
                }
                if (!/^(0[1-9]|1[0-2])\/[0-9]{2}$/.test(getField('cardExpiry').value.trim())) {
                    window.showElectroPopup('Ngày hết hạn phải có dạng MM/YY.', 'warning');
                    getField('cardExpiry').focus();
                    return false;
                }
                if (cvv.length < 3 || cvv.length > 4) {
                    window.showElectroPopup('CVV không hợp lệ.', 'warning');
                    getField('cardCvv').focus();
                    return false;
                }
            }
            return true;
        }

        function formatCurrency(amount) {
            return new Intl.NumberFormat('vi-VN', {
                style: 'currency',
                currency: 'VND',
                maximumFractionDigits: 0
            }).format(amount || 0);
        }

        function getCheckoutAmount() {
            var amountField = document.getElementById('checkoutTotalAmount');
            return Math.max(Number(amountField ? amountField.value : 0) || 0, 0);
        }

        function buildTransferContent() {
            var existing = getField('bankTransferContent');
            if (existing && existing.value.trim()) {
                return existing.value.trim();
            }
            var accountPart = '${sessionScope.account.id}';
            var timePart = String(Date.now()).slice(-8);
            return ('ELECTRO ' + accountPart + ' ' + timePart).trim();
        }

        function showBankTransferOverlay() {
            if (!bankOverlay) {
                return false;
            }

            var amount = Math.round(getCheckoutAmount());
            var content = buildTransferContent();
            getField('bankTransferContent').value = content;
            document.getElementById('bankTransferAmount').textContent = formatCurrency(amount);
            document.getElementById('bankTransferRawAmount').textContent = amount;
            document.getElementById('bankTransferContentText').textContent = content;

            var qrUrl = 'https://img.vietqr.io/image/'
                + encodeURIComponent(bankInfo.code + '-' + bankInfo.accountNumber + '-compact2')
                + '.png?amount=' + encodeURIComponent(amount)
                + '&addInfo=' + encodeURIComponent(content)
                + '&accountName=' + encodeURIComponent(bankInfo.accountName);
            document.getElementById('bankQrImage').src = qrUrl;

            bankOverlay.classList.add('is-active');
            bankOverlay.setAttribute('aria-hidden', 'false');
            return true;
        }

        function hideBankTransferOverlay() {
            if (!bankOverlay) {
                return;
            }
            bankOverlay.classList.remove('is-active');
            bankOverlay.setAttribute('aria-hidden', 'true');
        }

        function showProcessingOverlay() {
            overlay.classList.add('is-active');
            var steps = Array.from(overlay.querySelectorAll('.checkout-processing-step'));
            steps.forEach(function (step, index) {
                setTimeout(function () {
                    steps.forEach(function (item, itemIndex) {
                        item.classList.toggle('is-active', itemIndex === index);
                        if (itemIndex < index) {
                            item.classList.add('is-done');
                            item.querySelector('i').className = 'fa fa-check-circle';
                        }
                    });
                }, index * 450);
            });
        }

        ['cardNumber', 'cardCvv'].forEach(function (name) {
            var field = getField(name);
            if (field) {
                field.addEventListener('input', function () {
                    if (name === 'cardNumber') {
                        var digits = digitsOnly(field.value).slice(0, 19);
                        field.value = digits.replace(/(.{4})/g, '$1 ').trim();
                        return;
                    }
                    field.value = digitsOnly(field.value).slice(0, 4);
                });
            }
        });

        var expiry = getField('cardExpiry');
        if (expiry) {
            expiry.addEventListener('input', function () {
                var digits = digitsOnly(expiry.value).slice(0, 4);
                expiry.value = digits.length > 2 ? digits.slice(0, 2) + '/' + digits.slice(2) : digits;
            });
        }

        function submitOrder() {
            if (form.dataset.paymentSubmitting === 'true') {
                return;
            }
            form.dataset.paymentSubmitting = 'true';
            form.querySelectorAll('button[type="submit"]').forEach(function (button) {
                button.disabled = true;
                button.innerHTML = '<i class="fa fa-spinner fa-spin"></i> Đang xử lý';
            });
            showProcessingOverlay();
            setTimeout(function () {
                form.submit();
            }, 1800);
        }

        form.addEventListener('submit', function (event) {
            if (form.dataset.paymentSubmitting === 'true') {
                return;
            }
            if (!form.checkValidity()) {
                return;
            }
            if (!validatePaymentFields()) {
                event.preventDefault();
                return;
            }
            event.preventDefault();
            if (activePaymentMethod() === 'BANK_TRANSFER' && form.dataset.bankTransferConfirmed !== 'true') {
                showBankTransferOverlay();
                return;
            }
            submitOrder();
        });

        var bankConfirm = document.getElementById('bankTransferConfirm');
        if (bankConfirm) {
            bankConfirm.addEventListener('click', function () {
                form.dataset.bankTransferConfirmed = 'true';
                hideBankTransferOverlay();
                submitOrder();
            });
        }

        ['bankTransferClose', 'bankTransferBack'].forEach(function (id) {
            var button = document.getElementById(id);
            if (button) {
                button.addEventListener('click', function () {
                    hideBankTransferOverlay();
                });
            }
        });

        document.querySelectorAll('.bank-copy-btn').forEach(function (button) {
            button.addEventListener('click', function () {
                var target = document.getElementById(button.dataset.copyTarget);
                var value = target ? target.textContent.trim() : '';
                if (!value) {
                    return;
                }
                if (navigator.clipboard && navigator.clipboard.writeText) {
                    navigator.clipboard.writeText(value);
                }
                button.textContent = 'Đã sao chép';
                setTimeout(function () {
                    button.textContent = 'Sao chép';
                }, 1200);
            });
        });
    })();
</script>
</body>
</html>
