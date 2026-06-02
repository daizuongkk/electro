<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<base href="${pageContext.request.contextPath}/">

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
<c:url var="currentOrdersUrl" value="/admin/orders">
    <c:param name="p" value="${currentPage}"/>
    <c:param name="size" value="${pageSize}"/>
    <c:param name="keyword" value="${keyword}"/>
    <c:param name="status" value="${selectedStatus}"/>
    <c:param name="minTotal" value="${minTotal}"/>
    <c:param name="maxTotal" value="${maxTotal}"/>
    <c:param name="fromDate" value="${fromDate}"/>
    <c:param name="toDate" value="${toDate}"/>
    <c:param name="sortBy" value="${selectedSortBy}"/>
</c:url>
<c:url var="exportOrdersUrl" value="/admin/orders/export">
    <c:param name="keyword" value="${keyword}"/>
    <c:param name="status" value="${selectedStatus}"/>
    <c:param name="minTotal" value="${minTotal}"/>
    <c:param name="maxTotal" value="${maxTotal}"/>
    <c:param name="fromDate" value="${fromDate}"/>
    <c:param name="toDate" value="${toDate}"/>
    <c:param name="sortBy" value="${selectedSortBy}"/>
</c:url>

<main id="content" class="content py-10">
    <div class="container-fluid">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h1 class="fs-3 mb-1">Quản Lí Đơn Hàng</h1>
                <p class="mb-0">${totalOrders} đơn hàng trong hệ thống</p>
            </div>
            <div class="d-flex gap-2">
                <a href="${exportOrdersUrl}" class="btn btn-primary">
                    <i class="ti ti-file-spreadsheet"></i> Xuất Excel
                </a>
                <a href="admin" class="btn btn-outline-secondary">Thống kê</a>
            </div>
        </div>

        <form method="get" action="admin/orders" class="mb-3">
            <div class="d-flex gap-2 flex-wrap align-items-end">
                <div>
                    <label class="form-label small">Từ khóa</label>
                    <input type="text" name="keyword" class="form-control"
                           placeholder="Mã đơn, khách, SĐT, địa chỉ, sản phẩm..."
                           value="${fn:escapeXml(keyword)}" style="width: 310px;">
                </div>
                <div>
                    <label class="form-label small">Trạng thái</label>
                    <select name="status" class="form-select" style="width: 160px;">
                        <option value="" ${empty selectedStatus ? 'selected' : ''}>Tất cả</option>
                        <option value="PENDING" ${selectedStatus == 'PENDING' ? 'selected' : ''}>Chờ xử lý</option>
                        <option value="PAID" ${selectedStatus == 'PAID' ? 'selected' : ''}>Đã thanh toán</option>
                        <option value="SHIPPED" ${selectedStatus == 'SHIPPED' ? 'selected' : ''}>Đang giao</option>
                        <option value="COMPLETED" ${selectedStatus == 'COMPLETED' ? 'selected' : ''}>Hoàn tất</option>
                        <option value="CANCELLED" ${selectedStatus == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                    </select>
                </div>
                <div>
                    <label class="form-label small">Từ ngày</label>
                    <input type="date" name="fromDate" class="form-control" value="${fromDate}" style="width: 150px;">
                </div>
                <div>
                    <label class="form-label small">Đến ngày</label>
                    <input type="date" name="toDate" class="form-control" value="${toDate}" style="width: 150px;">
                </div>
                <div>
                    <label class="form-label small">Tổng từ</label>
                    <input type="number" name="minTotal" class="form-control" value="${minTotal}" style="width: 130px;">
                </div>
                <div>
                    <label class="form-label small">Tổng đến</label>
                    <input type="number" name="maxTotal" class="form-control" value="${maxTotal}" style="width: 130px;">
                </div>
                <div>
                    <label class="form-label small">Sắp xếp</label>
                    <select name="sortBy" class="form-select" style="width: 180px;">
                        <option value="created_desc" ${selectedSortBy == 'created_desc' ? 'selected' : ''}>Mới nhất</option>
                        <option value="created_asc" ${selectedSortBy == 'created_asc' ? 'selected' : ''}>Cũ nhất</option>
                        <option value="total_desc" ${selectedSortBy == 'total_desc' ? 'selected' : ''}>Tổng tiền cao</option>
                        <option value="total_asc" ${selectedSortBy == 'total_asc' ? 'selected' : ''}>Tổng tiền thấp</option>
                        <option value="status_asc" ${selectedSortBy == 'status_asc' ? 'selected' : ''}>Theo trạng thái</option>
                    </select>
                </div>
                <select name="size" class="form-select" style="width: 110px;">
                    <option value="10" ${pageSize == 10 ? 'selected' : ''}>10</option>
                    <option value="20" ${pageSize == 20 ? 'selected' : ''}>20</option>
                    <option value="50" ${pageSize == 50 ? 'selected' : ''}>50</option>
                </select>
                <button class="btn btn-outline-secondary" type="submit">
                    <i class="ti ti-search"></i> Tìm
                </button>
                <a href="${exportOrdersUrl}" class="btn btn-primary">
                    <i class="ti ti-file-spreadsheet"></i> Xuất Excel
                </a>
                <a href="admin/orders" class="btn btn-outline-secondary">Xóa lọc</a>
            </div>
        </form>

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
                                        <input type="hidden" name="returnUrl" value="${currentOrdersUrl}">
                                        <button class="btn btn-sm btn-primary" type="submit">Xác nhận</button>
                                    </form>
                                </c:if>
                                <c:if test="${order.status == 'PENDING' or order.status == 'PAID'}">
                                    <form method="post" action="admin/orders"
                                          data-confirm-message="Chuyển đơn hàng sang trạng thái đang giao?"
                                          data-confirm-text="Giao hàng">
                                        <input type="hidden" name="id" value="${order.id}">
                                        <input type="hidden" name="action" value="ship">
                                        <input type="hidden" name="returnUrl" value="${currentOrdersUrl}">
                                        <button class="btn btn-sm btn-outline-primary" type="submit">Giao hàng</button>
                                    </form>
                                </c:if>
                                <c:if test="${order.status == 'SHIPPED'}">
                                    <form method="post" action="admin/orders"
                                          data-confirm-message="Đánh dấu đơn hàng đã hoàn tất?"
                                          data-confirm-text="Hoàn tất">
                                        <input type="hidden" name="id" value="${order.id}">
                                        <input type="hidden" name="action" value="complete">
                                        <input type="hidden" name="returnUrl" value="${currentOrdersUrl}">
                                        <button class="btn btn-sm btn-success" type="submit">Hoàn tất</button>
                                    </form>
                                </c:if>
                                <c:if test="${order.status != 'COMPLETED' and order.status != 'CANCELLED'}">
                                    <form method="post" action="admin/orders"
                                          data-confirm-message="Bạn chắc chắn muốn hủy đơn hàng này?"
                                          data-confirm-text="Hủy đơn">
                                        <input type="hidden" name="id" value="${order.id}">
                                        <input type="hidden" name="action" value="cancel">
                                        <input type="hidden" name="returnUrl" value="${currentOrdersUrl}">
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
                <tfoot>
                <tr>
                    <td class="border-bottom-0">Trang ${currentPage}/${totalPages}</td>
                    <td colspan="7" class="border-bottom-0">
                        <nav aria-label="Page navigation" class="d-flex justify-content-end">
                            <ul class="pagination mb-0">
                                <c:url var="prevUrl" value="/admin/orders">
                                    <c:param name="p" value="${currentPage - 1}"/>
                                    <c:param name="size" value="${pageSize}"/>
                                    <c:param name="keyword" value="${keyword}"/>
                                    <c:param name="status" value="${selectedStatus}"/>
                                    <c:param name="minTotal" value="${minTotal}"/>
                                    <c:param name="maxTotal" value="${maxTotal}"/>
                                    <c:param name="fromDate" value="${fromDate}"/>
                                    <c:param name="toDate" value="${toDate}"/>
                                    <c:param name="sortBy" value="${selectedSortBy}"/>
                                </c:url>
                                <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                                    <a class="page-link" href="${prevUrl}">Previous</a>
                                </li>
                                <c:set var="startPage" value="${currentPage - 2}"/>
                                <c:set var="endPage" value="${currentPage + 2}"/>
                                <c:if test="${startPage < 1}">
                                    <c:set var="endPage" value="${endPage + (1 - startPage)}"/>
                                    <c:set var="startPage" value="1"/>
                                </c:if>
                                <c:if test="${endPage > totalPages}">
                                    <c:set var="startPage" value="${startPage - (endPage - totalPages)}"/>
                                    <c:set var="endPage" value="${totalPages}"/>
                                </c:if>
                                <c:if test="${startPage < 1}">
                                    <c:set var="startPage" value="1"/>
                                </c:if>
                                <c:if test="${startPage > 1}">
                                    <c:url var="firstUrl" value="/admin/orders">
                                        <c:param name="p" value="1"/>
                                        <c:param name="size" value="${pageSize}"/>
                                        <c:param name="keyword" value="${keyword}"/>
                                        <c:param name="status" value="${selectedStatus}"/>
                                        <c:param name="minTotal" value="${minTotal}"/>
                                        <c:param name="maxTotal" value="${maxTotal}"/>
                                        <c:param name="fromDate" value="${fromDate}"/>
                                        <c:param name="toDate" value="${toDate}"/>
                                        <c:param name="sortBy" value="${selectedSortBy}"/>
                                    </c:url>
                                    <li class="page-item"><a class="page-link" href="${firstUrl}">1</a></li>
                                    <li class="page-item disabled"><span class="page-link">...</span></li>
                                </c:if>
                                <c:forEach begin="${startPage}" end="${endPage}" var="pageNum">
                                    <c:url var="pageUrl" value="/admin/orders">
                                        <c:param name="p" value="${pageNum}"/>
                                        <c:param name="size" value="${pageSize}"/>
                                        <c:param name="keyword" value="${keyword}"/>
                                        <c:param name="status" value="${selectedStatus}"/>
                                        <c:param name="minTotal" value="${minTotal}"/>
                                        <c:param name="maxTotal" value="${maxTotal}"/>
                                        <c:param name="fromDate" value="${fromDate}"/>
                                        <c:param name="toDate" value="${toDate}"/>
                                        <c:param name="sortBy" value="${selectedSortBy}"/>
                                    </c:url>
                                    <li class="page-item ${pageNum == currentPage ? 'active' : ''}">
                                        <a class="page-link" href="${pageUrl}">${pageNum}</a>
                                    </li>
                                </c:forEach>
                                <c:if test="${endPage < totalPages}">
                                    <li class="page-item disabled"><span class="page-link">...</span></li>
                                    <c:url var="lastUrl" value="/admin/orders">
                                        <c:param name="p" value="${totalPages}"/>
                                        <c:param name="size" value="${pageSize}"/>
                                        <c:param name="keyword" value="${keyword}"/>
                                        <c:param name="status" value="${selectedStatus}"/>
                                        <c:param name="minTotal" value="${minTotal}"/>
                                        <c:param name="maxTotal" value="${maxTotal}"/>
                                        <c:param name="fromDate" value="${fromDate}"/>
                                        <c:param name="toDate" value="${toDate}"/>
                                        <c:param name="sortBy" value="${selectedSortBy}"/>
                                    </c:url>
                                    <li class="page-item"><a class="page-link" href="${lastUrl}">${totalPages}</a></li>
                                </c:if>
                                <c:url var="nextUrl" value="/admin/orders">
                                    <c:param name="p" value="${currentPage + 1}"/>
                                    <c:param name="size" value="${pageSize}"/>
                                    <c:param name="keyword" value="${keyword}"/>
                                    <c:param name="status" value="${selectedStatus}"/>
                                    <c:param name="minTotal" value="${minTotal}"/>
                                    <c:param name="maxTotal" value="${maxTotal}"/>
                                    <c:param name="fromDate" value="${fromDate}"/>
                                    <c:param name="toDate" value="${toDate}"/>
                                    <c:param name="sortBy" value="${selectedSortBy}"/>
                                </c:url>
                                <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                    <a class="page-link" href="${nextUrl}">Next</a>
                                </li>
                            </ul>
                        </nav>
                    </td>
                </tr>
                </tfoot>
            </table>
        </div>
    </div>
</main>
</body>
</html>
