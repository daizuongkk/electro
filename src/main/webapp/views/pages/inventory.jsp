<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<base href="${pageContext.request.contextPath}/">

<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Admin - Danh Sách Sản Phẩm"/>
    <%@include file="../commons/admin-head.jsp" %>
    <style>
        .admin-product-thumb {
            width: 52px;
            height: 52px;
            border-radius: 8px;
            object-fit: cover;
            border: 1px solid #dee2e6;
            background: #fff;
            flex: 0 0 auto;
        }
    </style>
</head>
<body>
<fmt:setLocale value="vi_VN"/>
<c:url var="fallbackProductImage" value="/assets/img/fallback_product_img.jpg"/>
<div id="overlay" class="overlay"></div>
<%@include file="../commons/admin-header.jsp" %>
<%@include file="../commons/admin-sidebar.jsp" %>

<main id="content" class="content py-10">
    <div class="container-fluid">
        <div class="row">
            <div class="col-12">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <h1 class="fs-3 mb-1">Danh Sách Sản Phẩm</h1>
                        <p class="mb-0">${totalProducts} sản phẩm trong hệ thống</p>
                    </div>
                    <a href="admin/products/form" class="btn btn-primary">Thêm Sản Phẩm</a>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-12">
                <form method="get" action="admin/products" class="mb-3">
                    <div class="d-flex gap-2 flex-wrap align-items-end">
                        <div>
                            <label class="form-label small">Từ khóa</label>
                            <input type="text" name="keyword" class="form-control" placeholder="Tên, mô tả, hãng..."
                                   value="${fn:escapeXml(keyword)}" style="width: 260px;">
                        </div>
                        <div>
                            <label class="form-label small">Danh mục</label>
                            <select name="category" class="form-select" style="width: 160px;">
                                <option value="">Tất cả</option>
                                <c:forEach var="category" items="${categories}">
                                    <option value="${category.key}" ${selectedCategory == category.key ? 'selected' : ''}>${category.value}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div>
                            <label class="form-label small">Hãng</label>
                            <select name="brand" class="form-select" style="width: 160px;">
                                <option value="">Tất cả</option>
                                <c:forEach var="brand" items="${brands}">
                                    <option value="${brand.key}" ${selectedBrand == brand.key ? 'selected' : ''}>${brand.value}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div>
                            <label class="form-label small">Giá từ</label>
                            <input type="number" name="minPrice" class="form-control" value="${minPrice}" style="width: 130px;">
                        </div>
                        <div>
                            <label class="form-label small">Giá đến</label>
                            <input type="number" name="maxPrice" class="form-control" value="${maxPrice}" style="width: 130px;">
                        </div>
                        <div>
                            <label class="form-label small">Tồn từ</label>
                            <input type="number" name="minQuantity" class="form-control" value="${minQuantity}" style="width: 110px;">
                        </div>
                        <div>
                            <label class="form-label small">Tồn đến</label>
                            <input type="number" name="maxQuantity" class="form-control" value="${maxQuantity}" style="width: 110px;">
                        </div>
                        <select name="size" class="form-select" style="width: 110px;">
                            <option value="10" ${pageSize == 10 ? 'selected' : ''}>10</option>
                            <option value="20" ${pageSize == 20 ? 'selected' : ''}>20</option>
                            <option value="50" ${pageSize == 50 ? 'selected' : ''}>50</option>
                        </select>
                        <button class="btn btn-outline-secondary" type="submit">
                            <i class="ti ti-search"></i> Tìm
                        </button>
                        <a href="admin/products" class="btn btn-outline-secondary">Xóa lọc</a>
                    </div>
                </form>

                <div class="card table-responsive">
                    <table class="table mb-0 text-nowrap table-hover">
                        <thead class="table-light border-light">
                        <tr>
                            <th>Sản phẩm</th>
                            <th>Mã</th>
                            <th>Danh mục</th>
                            <th>Hãng</th>
                            <th>Giá</th>
                            <th>Khuyến mãi</th>
                            <th>Tồn kho</th>
                            <th>Ngày tạo</th>
                            <th>Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="product" items="${products}">
                            <tr class="align-middle">
                                <td>
                                    <a href="products?id=${product.id}" class="d-flex align-items-center">
                                        <img src="${not empty product.imageUrl ? product.imageUrl[0] : fallbackProductImage}"
                                             alt="${fn:escapeXml(product.name)}" class="admin-product-thumb"/>
                                        <span class="ms-3">${product.name}</span>
                                    </a>
                                </td>
                                <td>#${product.id}</td>
                                <td>${product.category}</td>
                                <td>${empty product.brand ? 'N/A' : product.brand}</td>
                                <td><fmt:formatNumber value="${product.price}" type="currency" currencySymbol="₫"/></td>
                                <td>${empty product.promotion ? 0 : product.promotion}%</td>
                                <td>
                                    <span class="${product.quantity <= 10 ? 'text-danger fw-semibold' : 'text-primary fw-semibold'}">
                                            ${product.quantity}
                                    </span>
                                </td>
                                <td><fmt:formatDate value="${product.createdAt}" pattern="dd/MM/yyyy"/></td>
                                <td>
                                    <a href="admin/products/form?id=${product.id}" class="me-2">
                                        <i class="ti ti-edit"></i>
                                    </a>
                                    <form method="post" action="admin/products" class="d-inline"
                                          onsubmit="return confirm('Xóa sản phẩm này?');">
                                        <input type="hidden" name="action" value="delete-product">
                                        <input type="hidden" name="id" value="${product.id}">
                                        <button type="submit" class="btn btn-link link-danger p-0">
                                            <i class="ti ti-trash"></i>
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty products}">
                            <tr>
                                <td colspan="9" class="text-center py-5 text-secondary">Không có sản phẩm phù hợp.</td>
                            </tr>
                        </c:if>
                        </tbody>
                        <tfoot>
                        <tr>
                            <td class="border-bottom-0">Trang ${currentPage}/${totalPages}</td>
                            <td colspan="8" class="border-bottom-0">
                                <nav aria-label="Page navigation" class="d-flex justify-content-end">
                                    <ul class="pagination mb-0">
                                        <c:url var="prevUrl" value="/admin/products">
                                            <c:param name="p" value="${currentPage - 1}"/>
                                            <c:param name="size" value="${pageSize}"/>
                                            <c:param name="keyword" value="${keyword}"/>
                                            <c:param name="category" value="${selectedCategory}"/>
                                            <c:param name="brand" value="${selectedBrand}"/>
                                            <c:param name="minPrice" value="${minPrice}"/>
                                            <c:param name="maxPrice" value="${maxPrice}"/>
                                            <c:param name="minQuantity" value="${minQuantity}"/>
                                            <c:param name="maxQuantity" value="${maxQuantity}"/>
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
                                            <c:url var="firstUrl" value="/admin/products">
                                                <c:param name="p" value="1"/>
                                                <c:param name="size" value="${pageSize}"/>
                                                <c:param name="keyword" value="${keyword}"/>
                                                <c:param name="category" value="${selectedCategory}"/>
                                                <c:param name="brand" value="${selectedBrand}"/>
                                                <c:param name="minPrice" value="${minPrice}"/>
                                                <c:param name="maxPrice" value="${maxPrice}"/>
                                                <c:param name="minQuantity" value="${minQuantity}"/>
                                                <c:param name="maxQuantity" value="${maxQuantity}"/>
                                            </c:url>
                                            <li class="page-item"><a class="page-link" href="${firstUrl}">1</a></li>
                                            <li class="page-item disabled"><span class="page-link">...</span></li>
                                        </c:if>
                                        <c:forEach begin="${startPage}" end="${endPage}" var="pageNum">
                                            <c:url var="pageUrl" value="/admin/products">
                                                <c:param name="p" value="${pageNum}"/>
                                                <c:param name="size" value="${pageSize}"/>
                                                <c:param name="keyword" value="${keyword}"/>
                                                <c:param name="category" value="${selectedCategory}"/>
                                                <c:param name="brand" value="${selectedBrand}"/>
                                                <c:param name="minPrice" value="${minPrice}"/>
                                                <c:param name="maxPrice" value="${maxPrice}"/>
                                                <c:param name="minQuantity" value="${minQuantity}"/>
                                                <c:param name="maxQuantity" value="${maxQuantity}"/>
                                            </c:url>
                                            <li class="page-item ${pageNum == currentPage ? 'active' : ''}">
                                                <a class="page-link" href="${pageUrl}">${pageNum}</a>
                                            </li>
                                        </c:forEach>
                                        <c:if test="${endPage < totalPages}">
                                            <li class="page-item disabled"><span class="page-link">...</span></li>
                                            <c:url var="lastUrl" value="/admin/products">
                                                <c:param name="p" value="${totalPages}"/>
                                                <c:param name="size" value="${pageSize}"/>
                                                <c:param name="keyword" value="${keyword}"/>
                                                <c:param name="category" value="${selectedCategory}"/>
                                                <c:param name="brand" value="${selectedBrand}"/>
                                                <c:param name="minPrice" value="${minPrice}"/>
                                                <c:param name="maxPrice" value="${maxPrice}"/>
                                                <c:param name="minQuantity" value="${minQuantity}"/>
                                                <c:param name="maxQuantity" value="${maxQuantity}"/>
                                            </c:url>
                                            <li class="page-item"><a class="page-link" href="${lastUrl}">${totalPages}</a></li>
                                        </c:if>
                                        <c:url var="nextUrl" value="/admin/products">
                                            <c:param name="p" value="${currentPage + 1}"/>
                                            <c:param name="size" value="${pageSize}"/>
                                            <c:param name="keyword" value="${keyword}"/>
                                            <c:param name="category" value="${selectedCategory}"/>
                                            <c:param name="brand" value="${selectedBrand}"/>
                                            <c:param name="minPrice" value="${minPrice}"/>
                                            <c:param name="maxPrice" value="${maxPrice}"/>
                                            <c:param name="minQuantity" value="${minQuantity}"/>
                                            <c:param name="maxQuantity" value="${maxQuantity}"/>
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
        </div>
    </div>
</main>
</body>
</html>
