<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<base href="${pageContext.request.contextPath}/">


<head>
    <c:set var="pageTitle" value="Admin - Thống Kê"/>
    <%@include file="../commons/admin-head.jsp"%>
</head>

<body>
<fmt:setLocale value="vi_VN"/>
<c:url var="fallbackProductImage" value="/assets/img/fallback_product_img.jpg"/>
<div id="overlay" class="overlay"></div>
<!-- TOPBAR -->
<%@include file="../commons/admin-header.jsp"%>
<!-- SIDEBAR -->
<%@ include file="../commons/admin-sidebar.jsp" %>
<!-- MAIN CONTENT -->
<main id="content" class="content py-10">
    <div class="container-fluid">
        <div class="row ">
            <div class="col-12">
                <div class="mb-6">
                    <h1 class="fs-3 mb-1">Dashboard</h1>
                    <p>Tổng quan dữ liệu bán hàng và kho hiện tại.</p>
                </div>
            </div>
        </div>
        <div class="row g-3 mb-3">
            <div class="col-lg-3 col-12">

                <div class="card p-4  bg-primary bg-opacity-10 border border-primary border-opacity-25 rounded-2">

                    <div class="d-flex gap-3 ">
                        <div class="icon-shape icon-md bg-primary text-white rounded-2">
                            <i class="ti ti-report-analytics fs-4"></i>
                        </div>
                        <div>
                            <h2 class="mb-3 fs-6">Doanh thu</h2>
                            <h3 class="fw-bold mb-0"><fmt:formatNumber value="${totalRevenue}" type="currency" currencySymbol="₫"/></h3>
                            <p class="text-primary mb-0 small">Đơn đã thanh toán/giao/hoàn tất</p>
                        </div>
                    </div>
                </div>


            </div>
            <div class="col-lg-3 col-12">

                <div class="card p-4  bg-success bg-opacity-10 border border-success border-opacity-25 rounded-2">

                    <div class="d-flex gap-3 ">
                        <div class="icon-shape icon-md bg-success text-white rounded-2">
                            <i class="ti ti-repeat fs-4"></i>
                        </div>
                        <div>
                            <h2 class="mb-3 fs-6">Đơn hàng</h2>
                            <h3 class="fw-bold mb-0">${totalOrders}</h3>
                            <p class="text-success mb-0 small">${completedOrders} đơn hoàn tất</p>
                        </div>
                    </div>
                </div>


            </div>
            <div class="col-lg-3 col-12">

                <div class="card p-4  bg-info bg-opacity-10 border border-info border-opacity-25 rounded-2">

                    <div class="d-flex gap-3 ">
                        <div class="icon-shape icon-md bg-info text-white rounded-2">
                            <i class="ti ti-currency-dollar fs-4"></i>
                        </div>
                        <div>
                            <h2 class="mb-3 fs-6">Sản phẩm</h2>
                            <h3 class="fw-bold mb-0">${totalProducts}</h3>
                            <p class="text-info mb-0 small">Đang quản lý trong kho</p>
                        </div>
                    </div>
                </div>


            </div>
            <div class="col-lg-3 col-12">

                <div class="card p-4  bg-warning bg-opacity-10 border border-warning border-opacity-25 rounded-2">

                    <div class="d-flex gap-3 ">
                        <div class="icon-shape icon-md bg-warning text-white rounded-2">
                            <i class="ti ti-notes fs-4"></i>
                        </div>
                        <div>
                            <h2 class="mb-3 fs-6">Khách hàng</h2>
                            <h3 class="fw-bold mb-0">${totalUsers}</h3>
                            <p class="text-warning mb-0 small">${pendingOrders} đơn đang chờ</p>
                        </div>
                    </div>
                </div>


            </div>

        </div>
        <div class="row g-3 mb-3">
            <div class="col-lg-4 col-12">
                <div class="card">
                    <div class="card-body p-4">
                        <div class="d-flex justify-content-between border-bottom pb-5 mb-3">
                            <div>
                                <h3 class="fw-bold h4">$25,458</h3>
                                <span>Total Profit</span>
                            </div>
                            <div>
                                <i class="ti ti-layers-subtract fs-1 text-primary"></i>
                            </div>
                        </div>
                        <div class="d-flex justify-content-between align-items-center small">
                            <div class="text-muted"><span class="text-success">+35%</span> vs Last Month</div>
                            <div><a href="#" class="link-primary text-decoration-underline">View</a></div>
                        </div>
                    </div>
                </div>

            </div>
            <div class="col-lg-4 col-12">
                <div class="card">
                    <div class="card-body p-4">
                        <div class="d-flex justify-content-between border-bottom pb-5 mb-3">
                            <div>
                                <h3 class="fw-bold h4">$45,458</h3>
                                <span>Total Payment Returns</span>
                            </div>
                            <div>
                                <i class="ti ti-credit-card fs-1 text-danger"></i>
                            </div>
                        </div>
                        <div class="d-flex justify-content-between align-items-center small">
                            <div class="text-muted"><span class="text-danger">-20%</span> vs Last Month</div>
                            <div><a href="#" class="link-primary text-decoration-underline">View</a></div>
                        </div>
                    </div>
                </div>

            </div>
            <div class="col-lg-4 col-12">
                <div class="card">
                    <div class="card-body p-4">
                        <div class="d-flex justify-content-between border-bottom pb-5 mb-3">
                            <div>
                                <h3 class="fw-bold h4">$34,458</h3>
                                <span>Total Expenses</span>
                            </div>
                            <div>
                                <i class="ti ti-cash-banknote fs-1 text-warning"></i>
                            </div>
                        </div>
                        <div class="d-flex justify-content-between align-items-center small">
                            <div class="text-muted"><span class="text-warning">-20%</span> vs Last Month</div>
                            <div><a href="#" class="link-primary text-decoration-underline">View</a></div>
                        </div>
                    </div>
                </div>

            </div>

        </div>
        <div class="row g-3 mb-3">
            <div class="col-12 col-lg-6">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center bg-transparent px-4 py-3">
                        <h3 class="h5 mb-0">Sales vs Purchase</h3>
                        <div>
                            <select class="form-select form-select-sm">
                                <option selected>This Year</option>
                                <option>This Month</option>
                                <option>This Week</option>
                            </select>
                        </div>
                    </div>
                    <div class="card-body p-4">

                        <div id="salesPurchaseChart"></div>
                    </div>
                </div>
            </div>


            <div class="col-12 col-lg-6">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center bg-transparent px-4 py-3">
                        <h3 class="h5 mb-0">Overall Information</h3>
                        <div>
                            <select class="form-select form-select-sm">
                                <option selected>Last 6 Months</option>
                                <option>This Month</option>
                                <option>This Week</option>
                            </select>
                        </div>
                    </div>
                    <div class="card-body p-4">
                        <h3 class="h6">Customers Overview</h3>
                        <div class="row align-items-center">
                            <div class="col-sm-6">
                                <div id="customerChart">

                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="row">
                                    <div class="col-6 border-end">
                                        <div class="text-center ">
                                            <h2 class="mb-1">5.5K</h2>
                                            <p class="text-success mb-2">First Time</p>
                                            <span class="badge bg-success"><i
                                                    class="ti ti-arrow-up-left me-1"></i>25%</span>
                                        </div>
                                    </div>
                                    <div class="col-6">
                                        <div class="text-center">
                                            <h2 class="mb-1">3.5K</h2>
                                            <p class="text-warning mb-2">Return</p>
                                            <span class="badge bg-success badge-xs d-inline-flex align-items-center"><i
                                                    class="ti ti-arrow-up-left me-1"></i>21%</span>
                                        </div>
                                    </div>
                                </div>
                            </div>


                        </div>
                        <div class="row text-center border-top mt-4 pt-4">
                            <div class="col-4 border-end">
                                <h3 class="fw-bold mb-2">6987</h3>
                                <small class="text-secondary">Suppliers</small>
                            </div>
                            <div class="col-4 border-end">
                                <h3 class="fw-bold mb-2">4896</h3>
                                <small class="text-secondary">Customers</small>
                            </div>
                            <div class="col-4">
                                <h3 class="fw-bold mb-2">487</h3>
                                <small class="text-secondary">Orders</small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="row g-3">

            <!-- CARD 1 — Latest Products -->
            <div class="col-lg-4">
                <div class="card  h-100">
                    <div class="card-header bg-white d-flex justify-content-between align-items-center px-4 py-3">
                        <h4 class="mb-0 h5">Sản phẩm mới</h4>
                        <a href="admin/products" class="small text-primary text-decoration-underline">Xem tất cả</a>
                    </div>

                    <ul class="list-group list-group-flush">
                        <c:forEach var="product" items="${latestProducts}">
                            <li class="list-group-item d-flex align-items-center gap-3">
                                <img src="${not empty product.imageUrl ? product.imageUrl[0] : fallbackProductImage}"
                                     class="rounded" width="48" height="48" alt="${fn:escapeXml(product.name)}">
                                <div class="flex-grow-1">
                                    <p class="mb-1">${product.name}</p>
                                    <div class="d-flex align-items-center gap-2 text-muted">
                                        <small class="fw-semibold"><fmt:formatNumber value="${product.price}" type="currency" currencySymbol="₫"/></small>
                                        <small>•</small>
                                        <small>${product.quantity} tồn kho</small>
                                    </div>
                                </div>
                                <span class="badge bg-primary-subtle text-primary border border-primary">${product.category}</span>
                            </li>
                        </c:forEach>
                        <c:if test="${empty latestProducts}">
                            <li class="list-group-item text-center text-secondary py-4">Chưa có sản phẩm.</li>
                        </c:if>
                    </ul>
                </div>
            </div>

            <!-- CARD 2 — Low Stock Products -->
            <div class="col-lg-4">
                <div class="card  h-100">
                    <div class="card-header bg-white d-flex justify-content-between align-items-center px-4 py-3">
                        <div class="d-flex align-items-center">

                            <h4 class="mb-0 h5">Sản phẩm sắp hết</h4>
                        </div>
                        <a href="admin/products" class="small text-primary text-decoration-underline">Xem tất cả</a>
                    </div>

                    <ul class="list-group list-group-flush">
                        <c:forEach var="product" items="${lowStockProducts}">
                            <li class="list-group-item d-flex align-items-center gap-3">
                                <img src="${not empty product.imageUrl ? product.imageUrl[0] : fallbackProductImage}"
                                     class="rounded" width="48" height="48" alt="${fn:escapeXml(product.name)}">
                                <div class="flex-grow-1">
                                    <p class="mb-1">${product.name}</p>
                                    <small>ID: #${product.id}</small>
                                </div>
                                <div class="d-flex flex-column gap-0 align-items-center">
                                    <span class="fw-semibold ${product.quantity <= 10 ? 'text-danger' : 'text-primary'}">${product.quantity}</span>
                                    <small class="text-muted">Tồn kho</small>
                                </div>
                            </li>
                        </c:forEach>
                        <c:if test="${empty lowStockProducts}">
                            <li class="list-group-item text-center text-secondary py-4">Chưa có dữ liệu tồn kho.</li>
                        </c:if>
                    </ul>
                </div>
            </div>

            <!-- CARD 3 — Recent Orders -->
            <div class="col-lg-4">
                <div class="card  h-100">
                    <div class="card-header bg-white d-flex justify-content-between align-items-center px-4 py-3">
                        <h4 class="mb-0 h5">Đơn hàng mới</h4>
                        <span class="btn btn-sm btn-outline-secondary">
                            <i class="ti ti-calendar-event"></i> Gần nhất
                        </span>
                    </div>

                    <ul class="list-group list-group-flush">
                        <c:forEach var="order" items="${recentOrders}">
                            <li class="list-group-item d-flex align-items-center gap-3">
                                <div class="icon-shape icon-md bg-primary bg-opacity-10 text-primary rounded-2">
                                    <i class="ti ti-receipt"></i>
                                </div>
                                <div class="flex-grow-1">
                                    <p class="mb-1">#${order.id} - ${order.recipientName}</p>
                                    <div class="d-flex align-items-center gap-2 text-muted">
                                        <small class="fw-semibold"><fmt:formatNumber value="${order.totalPrice}" type="currency" currencySymbol="₫"/></small>
                                        <small>•</small>
                                        <small>${order.phone}</small>
                                    </div>
                                </div>
                                <span class="badge ${order.status == 'COMPLETED' ? 'bg-success-subtle text-success' : (order.status == 'CANCELLED' ? 'bg-danger-subtle text-danger' : 'bg-warning-subtle text-warning')}">
                                        ${order.status}
                                </span>
                            </li>
                        </c:forEach>
                        <c:if test="${empty recentOrders}">
                            <li class="list-group-item text-center text-secondary py-4">Chưa có đơn hàng.</li>
                        </c:if>
                    </ul>
                </div>
            </div>

        </div>
        <div class="row">
            <div class="col-12">
                <footer class="text-center py-2 mt-6 text-secondary ">
                    <p class="mb-0">Copyright © 2026 InApp Inventory Dashboard. Developed by <a
                            href="https://codescandy.com/" target="_blank" class="text-primary">CodesCandy</a> •
                        Distributed by <a href="https://themewagon.com/" target="_blank"
                                          class="text-primary">ThemeWagon</a></p>
                </footer>
            </div>

        </div>

    </div>
</main>

<!-- Bootstrap JS -->


</body>

</html>
