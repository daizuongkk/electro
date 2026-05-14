<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <c:set var="pageTitle" value="Electro - Đơn Hàng Của Tôi"/>
    <%@ include file="../commons/head.jsp" %>
    <link type="text/css" rel="stylesheet" href="assets/css/order.css"/>
</head>
<body id="orders-page">
<fmt:setLocale value="vi_VN"/>
<c:url var="fallbackProductImage" value="/assets/img/fallback_product_img.jpg"/>

<%@ include file="../commons/header.jsp" %>
<jsp:include page="../commons/navigation.jsp"/>

<%-- Breadcrumb --%>
<div id="breadcrumb" class="section">
    <div class="container">
        <div class="row">
            <div class="col-md-12">
                <ul class="breadcrumb-tree">
                    <li><a href="home">Trang chủ</a></li>
                    <li class="active">Đơn hàng</li>
                </ul>
            </div>
        </div>
    </div>
</div>

<div class="section orders-section">
    <div class="container">

        <%-- Success flash --%>
        <c:if test="${not empty placedOrderId}">
            <div class="js-popup-message hidden" data-type="success" data-message="Đặt hàng thành công. Mã đơn hàng của bạn là #${placedOrderId}."></div>
        </c:if>
        <c:if test="${param.cancelled == '1'}">
            <div class="js-popup-message hidden" data-type="success" data-message="Đơn hàng đã được hủy."></div>
        </c:if>
        <c:if test="${param.error == 'cancel'}">
            <div class="js-popup-message hidden" data-type="danger" data-message="Không thể hủy đơn hàng ở trạng thái hiện tại."></div>
        </c:if>

        <div class="orders-tabs" role="tablist" aria-label="Lọc đơn hàng">
            <a class="orders-tab ${activeOrderTab == 'active' ? 'active' : ''}" href="orders?tab=active" role="tab">
                Đang thực hiện <span>${activeOrderCount}</span>
            </a>
            <a class="orders-tab ${activeOrderTab == 'completed' ? 'active' : ''}" href="orders?tab=completed" role="tab">
                Đã hoàn thành <span>${completedOrderCount}</span>
            </a>
            <a class="orders-tab ${activeOrderTab == 'cancelled' ? 'active' : ''}" href="orders?tab=cancelled" role="tab">
                Đã hủy <span>${cancelledOrderCount}</span>
            </a>
        </div>

        <c:choose>

            <%-- Empty state --%>
            <c:when test="${activeOrderCount + completedOrderCount + cancelledOrderCount == 0}">
                <div class="orders-empty-state">
                    <i class="fa fa-list-alt"></i>
                    <h3>Bạn chưa có đơn hàng nào</h3>
                    <p>Các đơn hàng sau khi đặt sẽ xuất hiện tại đây.</p>
                    <a href="shop" class="primary-btn">Mua sắm ngay</a>
                </div>
            </c:when>

            <%-- Order list + detail panel --%>
            <c:otherwise>
                <div class="orders-layout">

                        <%-- ── Left: danh sách đơn ── --%>
                    <div class="orders-list">
                        <c:if test="${empty orders}">
                            <div class="orders-empty-tab">
                                <i class="fa fa-inbox"></i>
                                <h4>Không có đơn hàng trong tab này</h4>
                                <p>Chọn tab khác để xem các đơn hàng còn lại.</p>
                            </div>
                        </c:if>
                        <c:forEach var="order" items="${orders}">
                            <c:set var="isSelected" value="${not empty selectedOrder and selectedOrder.id == order.id}"/>
                            <article class="order-card ${isSelected ? 'active' : ''}">

                                    <%-- Header: ID + badge --%>
                                <div class="order-card-header">
                                    <div>
                                        <h4>Đơn hàng #${order.id}</h4>
                                        <p><fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm"/></p>
                                    </div>
                                    <c:choose>
                                        <c:when test="${order.status == 'PENDING'}">
                                            <span class="order-status pending">Chờ xử lý</span>
                                        </c:when>
                                        <c:when test="${order.status == 'PAID'}">
                                            <span class="order-status paid">Đã thanh toán</span>
                                        </c:when>
                                        <c:when test="${order.status == 'SHIPPED'}">
                                            <span class="order-status shipped">Đang giao</span>
                                        </c:when>
                                        <c:when test="${order.status == 'COMPLETED'}">
                                            <span class="order-status completed">Hoàn tất</span>
                                        </c:when>
                                        <c:when test="${order.status == 'CANCELLED'}">
                                            <span class="order-status cancelled">Đã hủy</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="order-status">${fn:escapeXml(order.status)}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                    <%-- Meta: người nhận · tổng tiền · số sản phẩm --%>
                                <div class="order-card-body">
                                    <div>
                                        <span>Người nhận</span>
                                        <strong>${fn:escapeXml(order.recipientName)}</strong>
                                    </div>
                                    <div class="total-value">
                                        <span>Tổng tiền</span>
                                        <strong>
                                            <fmt:formatNumber value="${order.totalPrice}" type="currency" currencySymbol="₫"/>
                                        </strong>
                                    </div>
                                    <div>
                                        <span>Sản phẩm</span>
                                        <strong>${fn:length(order.items)} món</strong>
                                    </div>
                                </div>

                                    <%-- Action --%>
                                <div class="order-card-actions">
                                    <a href="orders?tab=${activeOrderTab}&id=${order.id}" class="primary-btn">
                                        <i class="fa fa-eye"></i>Xem chi tiết
                                    </a>
                                </div>

                            </article>
                        </c:forEach>
                    </div><%-- /orders-list --%>

                        <%-- ── Right: detail panel ── --%>
                    <aside class="order-detail-panel">

                        <c:choose>
                            <c:when test="${empty selectedOrder}">
                                <div class="order-detail-placeholder">
                                    <i class="fa fa-truck"></i>
                                    <h4>Chọn một đơn hàng</h4>
                                    <p>Bấm "Xem chi tiết" để xem tiến trình và sản phẩm trong đơn.</p>
                                </div>
                            </c:when>

                            <c:otherwise>
                                <%-- Header --%>
                                <div class="order-detail-header">
                                    <h3>Đơn hàng #${selectedOrder.id}</h3>
                                    <p><i class="fa fa-map-marker" style="color:#D10024;margin-right:4px;"></i>${fn:escapeXml(selectedOrder.address)}</p>
                                </div>

                                <%-- Timeline --%>
                                <div class="order-timeline">
                                    <c:set var="s" value="${selectedOrder.status}"/>

                                    <div class="timeline-step done"><%-- Tạo đơn: luôn done --%>
                                        <span></span>
                                        <strong>Tạo đơn</strong>
                                    </div>
                                    <div class="timeline-step ${s=='PAID' or s=='SHIPPED' or s=='COMPLETED' ? 'done' : ''}">
                                        <span></span>
                                        <strong>Thanh toán</strong>
                                    </div>
                                    <div class="timeline-step ${s=='SHIPPED' or s=='COMPLETED' ? 'done' : ''}">
                                        <span></span>
                                        <strong>Đang giao</strong>
                                    </div>
                                    <div class="timeline-step ${s=='COMPLETED' ? 'done' : ''}">
                                        <span></span>
                                        <strong>Hoàn tất</strong>
                                    </div>
                                </div>

                                <%-- Product list --%>
                                <div class="order-detail-items">
                                    <c:forEach var="item" items="${selectedOrder.items}">
                                        <div class="order-detail-item">
                                            <a class="order-detail-image" href="products?id=${item.productId}">
                                                <img src="${not empty item.productImageUrl ? item.productImageUrl : fallbackProductImage}"
                                                     alt="${fn:escapeXml(item.productName)}">
                                            </a>
                                            <div class="order-detail-info">
                                                <h4><a href="products?id=${item.productId}">${fn:escapeXml(item.productName)}</a></h4>
                                                <p>${item.quantity} &times; <fmt:formatNumber value="${item.price}" type="currency" currencySymbol="₫"/></p>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>

                                <%-- Total --%>
                                <div class="order-detail-total">
                                    <span>Tổng cộng</span>
                                    <strong>
                                        <fmt:formatNumber value="${selectedOrder.totalPrice}" type="currency" currencySymbol="₫"/>
                                    </strong>
                                </div>
                                <c:if test="${selectedOrder.status == 'PENDING' or selectedOrder.status == 'PAID'}">
                                    <form method="post" action="orders" class="order-cancel-form"
                                          data-confirm-message="Bạn chắc chắn muốn hủy đơn hàng này?"
                                          data-confirm-text="Hủy đơn">
                                        <input type="hidden" name="id" value="${selectedOrder.id}">
                                        <input type="hidden" name="action" value="cancel">
                                        <input type="hidden" name="tab" value="${activeOrderTab}">
                                        <button type="submit" class="order-cancel-btn">Hủy đơn hàng</button>
                                    </form>
                                </c:if>

                            </c:otherwise>
                        </c:choose>

                    </aside><%-- /order-detail-panel --%>

                </div><%-- /orders-layout --%>
            </c:otherwise>
        </c:choose>

    </div><%-- /container --%>
</div><%-- /section --%>

<%@ include file="../commons/footer.jsp" %>
<%@ include file="../commons/script.jsp" %>
</body>
</html>
