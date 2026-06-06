<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<base href="${pageContext.request.contextPath}/">

<!DOCTYPE html>
<html lang="vi">
<head>
    <c:set var="pageTitle" value="Quản trị - Báo cáo"/>
    <%@include file="../commons/admin-head.jsp" %>
</head>
<body>
<fmt:setLocale value="vi_VN"/>
<div id="overlay" class="overlay"></div>
<%@include file="../commons/admin-header.jsp" %>
<%@include file="../commons/admin-sidebar.jsp" %>

<c:url var="excelExportUrl" value="/admin/reports/export/excel">
    <c:param name="type" value="${reportType}"/>
    <c:param name="fromDate" value="${filter.fromDate}"/>
    <c:param name="toDate" value="${filter.toDate}"/>
    <c:param name="status" value="${filter.status}"/>
    <c:param name="topLimit" value="${filter.topLimit}"/>
</c:url>
<c:url var="pdfExportUrl" value="/admin/reports/export/pdf">
    <c:param name="type" value="${reportType}"/>
    <c:param name="fromDate" value="${filter.fromDate}"/>
    <c:param name="toDate" value="${filter.toDate}"/>
    <c:param name="status" value="${filter.status}"/>
    <c:param name="topLimit" value="${filter.topLimit}"/>
</c:url>

<main id="content" class="content py-10">
    <div class="container-fluid">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h1 class="fs-3 mb-1">Báo cáo quản trị</h1>
                <p class="mb-0">${reportTitle}</p>
            </div>
            <a href="admin" class="btn btn-outline-secondary">Bảng điều khiển</a>
        </div>

        <form method="get" action="admin/reports" class="card p-4 mb-4">
            <div class="d-flex gap-2 flex-wrap align-items-end">
                <div>
                    <label class="form-label small">Loại báo cáo</label>
                    <select name="type" class="form-select" style="width: 220px;">
                        <option value="revenue" ${reportType == 'revenue' ? 'selected' : ''}>Doanh thu</option>
                        <option value="orders" ${reportType == 'orders' ? 'selected' : ''}>Đơn hàng</option>
                        <option value="products" ${reportType == 'products' ? 'selected' : ''}>Sản phẩm bán chạy</option>
                        <option value="inventory" ${reportType == 'inventory' ? 'selected' : ''}>Tồn kho</option>
                    </select>
                </div>
                <div>
                    <label class="form-label small">Từ ngày</label>
                    <input type="date" name="fromDate" class="form-control" value="${filter.fromDate}" style="width: 160px;">
                </div>
                <div>
                    <label class="form-label small">Đến ngày</label>
                    <input type="date" name="toDate" class="form-control" value="${filter.toDate}" style="width: 160px;">
                </div>
                <div>
                    <label class="form-label small">Trạng thái</label>
                    <select name="status" class="form-select" style="width: 170px;">
                        <option value="" ${empty filter.status ? 'selected' : ''}>Tất cả</option>
                        <c:forEach var="status" items="${statusOptions}">
                            <option value="${status.key}" ${filter.status == status.key ? 'selected' : ''}>${status.value}</option>
                        </c:forEach>
                    </select>
                </div>
                <div>
                    <label class="form-label small">Số lượng sản phẩm</label>
                    <select name="topLimit" class="form-select" style="width: 130px;">
                        <c:forEach var="limit" items="${topLimitOptions}">
                            <option value="${limit}" ${filter.topLimit == limit ? 'selected' : ''}>${limit} sản phẩm</option>
                        </c:forEach>
                    </select>
                </div>
                <div>
                    <label class="form-label small">Thanh toán</label>
                    <select class="form-select" style="width: 190px;" disabled>
                        <option>Không có cột riêng</option>
                    </select>
                </div>
                <button class="btn btn-outline-secondary" type="submit">
                    <i class="ti ti-search"></i> Xem báo cáo
                </button>
                <a class="btn btn-primary" href="${excelExportUrl}">
                    <i class="ti ti-file-spreadsheet"></i> Xuất Excel
                </a>
                <a class="btn btn-danger" href="${pdfExportUrl}">
                    <i class="fa fa-file-pdf-o"></i> Xuất PDF
                </a>
            </div>
        </form>

        <c:choose>
            <c:when test="${reportType == 'orders'}">
                <div class="row g-3 mb-3">
                    <div class="col-md-6 col-xl-3">
                        <div class="card p-4 bg-primary bg-opacity-10 border border-primary border-opacity-25 rounded-2">
                            <small class="text-secondary">Tổng số đơn hàng</small>
                            <h2 class="fs-4 mb-0">${orderReport.totalOrders}</h2>
                        </div>
                    </div>
                    <div class="col-md-6 col-xl-3">
                        <div class="card p-4 bg-success bg-opacity-10 border border-success border-opacity-25 rounded-2">
                            <small class="text-secondary">Tổng giá trị đơn hàng</small>
                            <h2 class="fs-4 mb-0"><fmt:formatNumber value="${orderReport.totalAmount}" type="currency" currencySymbol="₫"/></h2>
                        </div>
                    </div>
                </div>

                <div class="card table-responsive">
                    <table class="table mb-0 align-middle">
                        <thead class="table-light">
                        <tr>
                            <th>Mã đơn hàng</th>
                            <th>Khách hàng</th>
                            <th>Ngày đặt</th>
                            <th>Tổng tiền</th>
                            <th>Trạng thái</th>
                            <th>Phương thức thanh toán</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="order" items="${orderReport.rows}">
                            <tr>
                                <td class="fw-semibold">#${order.orderId}</td>
                                <td>${fn:escapeXml(order.customerName)}</td>
                                <td><fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                <td><fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="₫"/></td>
                                <td>
                                    <span class="badge ${order.status == 'COMPLETED' ? 'bg-success' : (order.status == 'CANCELLED' ? 'bg-danger' : (order.status == 'SHIPPED' ? 'bg-info' : 'bg-warning'))}">
                                        ${order.statusLabel}
                                    </span>
                                </td>
                                <td>${fn:escapeXml(order.paymentMethod)}</td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty orderReport.rows}">
                            <tr>
                                <td colspan="6" class="text-center py-5 text-secondary">Không có dữ liệu phù hợp.</td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </c:when>

            <c:when test="${reportType == 'products'}">
                <div class="row g-3 mb-3">
                    <div class="col-md-6 col-xl-3">
                        <div class="card p-4 bg-primary bg-opacity-10 border border-primary border-opacity-25 rounded-2">
                            <small class="text-secondary">Số lượng hiển thị</small>
                            <h2 class="fs-4 mb-0">${productSalesReport.topLimit} sản phẩm</h2>
                        </div>
                    </div>
                    <div class="col-md-6 col-xl-3">
                        <div class="card p-4 bg-success bg-opacity-10 border border-success border-opacity-25 rounded-2">
                            <small class="text-secondary">Số lượng đã bán</small>
                            <h2 class="fs-4 mb-0">${productSalesReport.totalQuantitySold}</h2>
                        </div>
                    </div>
                    <div class="col-md-6 col-xl-3">
                        <div class="card p-4 bg-info bg-opacity-10 border border-info border-opacity-25 rounded-2">
                            <small class="text-secondary">Doanh thu</small>
                            <h2 class="fs-4 mb-0"><fmt:formatNumber value="${productSalesReport.totalRevenue}" type="currency" currencySymbol="₫"/></h2>
                        </div>
                    </div>
                </div>

                <div class="card table-responsive">
                    <table class="table mb-0 align-middle">
                        <thead class="table-light">
                        <tr>
                            <th>Mã sản phẩm</th>
                            <th>Tên sản phẩm</th>
                            <th>Danh mục</th>
                            <th>Số lượng đã bán</th>
                            <th>Doanh thu</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="product" items="${productSalesReport.rows}">
                            <tr>
                                <td class="fw-semibold">#${product.productId}</td>
                                <td>${fn:escapeXml(product.productName)}</td>
                                <td>${fn:escapeXml(product.category)}</td>
                                <td>${product.quantitySold}</td>
                                <td><fmt:formatNumber value="${product.revenue}" type="currency" currencySymbol="₫"/></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty productSalesReport.rows}">
                            <tr>
                                <td colspan="5" class="text-center py-5 text-secondary">Không có dữ liệu phù hợp.</td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </c:when>

            <c:when test="${reportType == 'inventory'}">
                <div class="row g-3 mb-3">
                    <div class="col-md-6 col-xl-3">
                        <div class="card p-4 bg-primary bg-opacity-10 border border-primary border-opacity-25 rounded-2">
                            <small class="text-secondary">Tổng sản phẩm</small>
                            <h2 class="fs-4 mb-0">${inventoryReport.totalProducts}</h2>
                        </div>
                    </div>
                    <div class="col-md-6 col-xl-3">
                        <div class="card p-4 bg-danger bg-opacity-10 border border-danger border-opacity-25 rounded-2">
                            <small class="text-secondary">Hết hàng</small>
                            <h2 class="fs-4 mb-0">${inventoryReport.outOfStockProducts}</h2>
                        </div>
                    </div>
                    <div class="col-md-6 col-xl-3">
                        <div class="card p-4 bg-warning bg-opacity-10 border border-warning border-opacity-25 rounded-2">
                            <small class="text-secondary">Sắp hết hàng</small>
                            <h2 class="fs-4 mb-0">${inventoryReport.lowStockProducts}</h2>
                        </div>
                    </div>
                    <div class="col-md-6 col-xl-3">
                        <div class="card p-4 bg-success bg-opacity-10 border border-success border-opacity-25 rounded-2">
                            <small class="text-secondary">Giá trị tồn</small>
                            <h2 class="fs-4 mb-0"><fmt:formatNumber value="${inventoryReport.inventoryValue}" type="currency" currencySymbol="₫"/></h2>
                        </div>
                    </div>
                </div>

                <div class="card table-responsive">
                    <table class="table mb-0 align-middle">
                        <thead class="table-light">
                        <tr>
                            <th>Mã sản phẩm</th>
                            <th>Tên sản phẩm</th>
                            <th>Danh mục</th>
                            <th>Số lượng tồn</th>
                            <th>Giá bán</th>
                            <th>Trạng thái tồn kho</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="item" items="${inventoryReport.rows}">
                            <tr>
                                <td class="fw-semibold">#${item.productId}</td>
                                <td>${fn:escapeXml(item.productName)}</td>
                                <td>${fn:escapeXml(item.category)}</td>
                                <td>${item.quantity}</td>
                                <td><fmt:formatNumber value="${item.price}" type="currency" currencySymbol="₫"/></td>
                                <td>
                                    <span class="badge ${item.stockStatus == 'Hết hàng' ? 'bg-danger' : (item.stockStatus == 'Sắp hết hàng' ? 'bg-warning text-dark' : 'bg-success')}">
                                        ${item.stockStatus}
                                    </span>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty inventoryReport.rows}">
                            <tr>
                                <td colspan="6" class="text-center py-5 text-secondary">Không có dữ liệu phù hợp.</td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </c:when>

            <c:otherwise>
                <div class="row g-3 mb-3">
                    <div class="col-md-6 col-xl-3">
                        <div class="card p-4 bg-primary bg-opacity-10 border border-primary border-opacity-25 rounded-2">
                            <small class="text-secondary">Tổng doanh thu</small>
                            <h2 class="fs-4 mb-0"><fmt:formatNumber value="${revenueReport.totalRevenue}" type="currency" currencySymbol="₫"/></h2>
                        </div>
                    </div>
                    <div class="col-md-6 col-xl-3">
                        <div class="card p-4 bg-success bg-opacity-10 border border-success border-opacity-25 rounded-2">
                            <small class="text-secondary">Tổng số đơn hàng</small>
                            <h2 class="fs-4 mb-0">${revenueReport.totalOrders}</h2>
                        </div>
                    </div>
                    <div class="col-md-6 col-xl-3">
                        <div class="card p-4 bg-info bg-opacity-10 border border-info border-opacity-25 rounded-2">
                            <small class="text-secondary">Sản phẩm đã bán</small>
                            <h2 class="fs-4 mb-0">${revenueReport.totalProductsSold}</h2>
                        </div>
                    </div>
                    <div class="col-md-6 col-xl-3">
                        <div class="card p-4 bg-warning bg-opacity-10 border border-warning border-opacity-25 rounded-2">
                            <small class="text-secondary">Giá trị trung bình/đơn</small>
                            <h2 class="fs-4 mb-0"><fmt:formatNumber value="${revenueReport.averageOrderValue}" type="currency" currencySymbol="₫"/></h2>
                        </div>
                    </div>
                </div>

                <div class="card table-responsive">
                    <table class="table mb-0 align-middle">
                        <thead class="table-light">
                        <tr>
                            <th>Ngày</th>
                            <th>Số đơn hàng</th>
                            <th>Số sản phẩm bán</th>
                            <th>Doanh thu</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="row" items="${revenueReport.rows}">
                            <tr>
                                <td>${fn:substring(row.reportDate, 8, 10)}/${fn:substring(row.reportDate, 5, 7)}/${fn:substring(row.reportDate, 0, 4)}</td>
                                <td>${row.orderCount}</td>
                                <td>${row.productCount}</td>
                                <td><fmt:formatNumber value="${row.revenue}" type="currency" currencySymbol="₫"/></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty revenueReport.rows}">
                            <tr>
                                <td colspan="4" class="text-center py-5 text-secondary">Không có dữ liệu phù hợp.</td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>
</body>
</html>
