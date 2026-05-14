<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <c:set var="pageTitle" value="Admin - Quản Lí Đơn Hàng"/>
    <%@include file="../commons/admin-head.jsp" %>
</head>
<body>
<div id="overlay" class="overlay"></div>
<%@include file="../commons/admin-header.jsp" %>
<%@include file="../commons/admin-sidebar.jsp" %>

<main id="content" class="content py-10">
    <div class="container-fluid">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h1 class="fs-3 mb-1">Quản Lí Đơn Hàng</h1>
                <p class="mb-0">${fn:length(orders)} đơn hàng trong hệ thống</p>
            </div>
            <a href="admin" class="btn btn-outline-secondary">Thống kê</a>
        </div>

        <div class="card table-responsive">
            <table class="table mb-0 align-middle">
                <thead class="table-light">
                <tr>
                    <th>Mã đơn</th>
                    <th>Khách hàng</th>
                    <th>Liên hệ</th>
                    <th>Sản phẩm</th>
                    <th>Tổng tiền</th>
                    <th>Trạng thái</th>
                    <th>Ngày tạo</th>
                    <th>Thao tác</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="order" items="${orders}">
                    <tr>
                        <td class="fw-semibold">#${order.id}</td>
                        <td>
                            <div class="fw-semibold">${fn:escapeXml(order.recipientName)}</div>
                            <small class="text-secondary">${fn:escapeXml(order.address)}</small>
                        </td>
                        <td>${fn:escapeXml(order.phone)}</td>
                        <td>
                            <c:forEach var="item" items="${order.items}">
                                <div class="small">${item.quantity}x ${fn:escapeXml(item.productName)}</div>
                            </c:forEach>
                        </td>
                        <td class="fw-semibold">
                            <fmt:formatNumber value="${order.totalPrice}" type="currency" currencySymbol="₫"/>
                        </td>
                        <td>
                            <span class="badge ${order.status == 'COMPLETED' ? 'bg-success' : (order.status == 'CANCELLED' ? 'bg-danger' : (order.status == 'SHIPPED' ? 'bg-info' : 'bg-warning'))}">
                                ${order.status}
                            </span>
                        </td>
                        <td><fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                        <td>
                            <div class="d-flex gap-2 flex-wrap">
                                <c:if test="${order.status == 'PENDING'}">
                                    <form method="post" action="admin/orders"
                                          data-confirm-message="Xác nhận đơn hàng này?"
                                          data-confirm-text="Xác nhận">
                                        <input type="hidden" name="id" value="${order.id}">
                                        <input type="hidden" name="action" value="confirm">
                                        <button class="btn btn-sm btn-primary" type="submit">Xác nhận</button>
                                    </form>
                                </c:if>
                                <c:if test="${order.status == 'PENDING' or order.status == 'PAID'}">
                                    <form method="post" action="admin/orders"
                                          data-confirm-message="Chuyển đơn hàng sang trạng thái đang giao?"
                                          data-confirm-text="Giao hàng">
                                        <input type="hidden" name="id" value="${order.id}">
                                        <input type="hidden" name="action" value="ship">
                                        <button class="btn btn-sm btn-outline-primary" type="submit">Giao hàng</button>
                                    </form>
                                </c:if>
                                <c:if test="${order.status == 'SHIPPED'}">
                                    <form method="post" action="admin/orders"
                                          data-confirm-message="Đánh dấu đơn hàng đã hoàn tất?"
                                          data-confirm-text="Hoàn tất">
                                        <input type="hidden" name="id" value="${order.id}">
                                        <input type="hidden" name="action" value="complete">
                                        <button class="btn btn-sm btn-success" type="submit">Hoàn tất</button>
                                    </form>
                                </c:if>
                                <c:if test="${order.status != 'COMPLETED' and order.status != 'CANCELLED'}">
                                    <form method="post" action="admin/orders"
                                          data-confirm-message="Bạn chắc chắn muốn hủy đơn hàng này?"
                                          data-confirm-text="Hủy đơn">
                                        <input type="hidden" name="id" value="${order.id}">
                                        <input type="hidden" name="action" value="cancel">
                                        <button class="btn btn-sm btn-outline-danger" type="submit">Hủy</button>
                                    </form>
                                </c:if>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty orders}">
                    <tr>
                        <td colspan="8" class="text-center py-5 text-secondary">Chưa có đơn hàng.</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </div>
</main>
</body>
</html>
