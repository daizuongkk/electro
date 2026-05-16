<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<base href="${pageContext.request.contextPath}/">

<!DOCTYPE html>
<html lang="vi">
<head>
    <c:set var="pageTitle" value="Admin - Quản Lí Đánh Giá"/>
    <%@include file="../commons/admin-head.jsp" %>
</head>
<body>
<div id="overlay" class="overlay"></div>
<%@include file="../commons/admin-header.jsp" %>
<%@include file="../commons/admin-sidebar.jsp" %>

<c:url var="currentReviewsUrl" value="/admin/reviews">
    <c:param name="p" value="${currentPage}"/>
    <c:param name="size" value="${pageSize}"/>
    <c:param name="keyword" value="${keyword}"/>
    <c:param name="productId" value="${productId}"/>
    <c:param name="score" value="${selectedScore}"/>
    <c:param name="fromDate" value="${fromDate}"/>
    <c:param name="toDate" value="${toDate}"/>
    <c:param name="sortBy" value="${selectedSortBy}"/>
</c:url>

<main id="content" class="content py-10">
    <div class="container-fluid">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h1 class="fs-3 mb-1">Quản Lí Đánh Giá</h1>
                <p class="mb-0">${totalReviews} đánh giá trong hệ thống</p>
            </div>
            <a href="admin/products" class="btn btn-outline-secondary">Kho sản phẩm</a>
        </div>

        <form method="get" action="admin/reviews" class="mb-3">
            <div class="d-flex gap-2 flex-wrap align-items-end">
                <div>
                    <label class="form-label small">Từ khóa</label>
                    <input type="text" name="keyword" class="form-control"
                           placeholder="Sản phẩm, khách hàng, email, nội dung..."
                           value="${fn:escapeXml(keyword)}" style="width: 330px;">
                </div>
                <div>
                    <label class="form-label small">Mã sản phẩm</label>
                    <input type="number" name="productId" min="1" class="form-control"
                           placeholder="VD: 125"
                           value="${fn:escapeXml(productId)}" style="width: 140px;">
                </div>
                <div>
                    <label class="form-label small">Số sao</label>
                    <select name="score" class="form-select" style="width: 130px;">
                        <option value="" ${empty selectedScore ? 'selected' : ''}>Tất cả</option>
                        <c:forEach begin="1" end="5" var="score">
                            <option value="${score}" ${selectedScore == score ? 'selected' : ''}>${score} sao</option>
                        </c:forEach>
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
                    <label class="form-label small">Sắp xếp</label>
                    <select name="sortBy" class="form-select" style="width: 170px;">
                        <option value="created_desc" ${selectedSortBy == 'created_desc' ? 'selected' : ''}>Mới nhất</option>
                        <option value="created_asc" ${selectedSortBy == 'created_asc' ? 'selected' : ''}>Cũ nhất</option>
                        <option value="score_desc" ${selectedSortBy == 'score_desc' ? 'selected' : ''}>Sao cao</option>
                        <option value="score_asc" ${selectedSortBy == 'score_asc' ? 'selected' : ''}>Sao thấp</option>
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
                <a href="admin/reviews" class="btn btn-outline-secondary">Xóa lọc</a>
            </div>
        </form>

        <div class="card table-responsive">
            <table class="table mb-0 align-middle">
                <thead class="table-light">
                <tr>
                    <th>ID</th>
                    <th>Sản phẩm</th>
                    <th>Khách hàng</th>
                    <th>Sao</th>
                    <th>Nội dung</th>
                    <th>Ngày tạo</th>
                    <th>Thao tác</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="review" items="${reviews}">
                    <tr>
                        <td class="fw-semibold">#${review.id}</td>
                        <td>
                            <a class="fw-semibold" href="products?id=${review.productId}" target="_blank">
                                ${fn:escapeXml(review.productName)}
                            </a>
                            <div class="small text-secondary">Mã SP: ${review.productId}</div>
                        </td>
                        <td>
                            <div class="fw-semibold">${fn:escapeXml(review.userDisplayName)}</div>
                            <small class="text-secondary">${fn:escapeXml(review.userEmail)}</small>
                        </td>
                        <td>
                            <span class="badge bg-warning text-dark">${review.score}/5</span>
                        </td>
                        <td style="min-width: 280px; max-width: 520px;">
                            ${fn:escapeXml(review.message)}
                        </td>
                        <td><fmt:formatDate value="${review.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                        <td>
                            <form method="post" action="admin/reviews"
                                  data-confirm-message="Xóa đánh giá này khỏi hệ thống?"
                                  data-confirm-text="Xóa">
                                <input type="hidden" name="id" value="${review.id}">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="returnUrl" value="${currentReviewsUrl}">
                                <button class="btn btn-sm btn-outline-danger" type="submit">Xóa</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty reviews}">
                    <tr>
                        <td colspan="7" class="text-center py-5 text-secondary">Chưa có đánh giá phù hợp.</td>
                    </tr>
                </c:if>
                </tbody>
                <tfoot>
                <tr>
                    <td class="border-bottom-0">Trang ${currentPage}/${totalPages}</td>
                    <td colspan="6" class="border-bottom-0">
                        <nav aria-label="Page navigation" class="d-flex justify-content-end">
                            <ul class="pagination mb-0">
                                <c:url var="prevUrl" value="/admin/reviews">
                                    <c:param name="p" value="${currentPage - 1}"/>
                                    <c:param name="size" value="${pageSize}"/>
                                    <c:param name="keyword" value="${keyword}"/>
                                    <c:param name="productId" value="${productId}"/>
                                    <c:param name="score" value="${selectedScore}"/>
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
                                    <c:url var="firstUrl" value="/admin/reviews">
                                        <c:param name="p" value="1"/>
                                        <c:param name="size" value="${pageSize}"/>
                                        <c:param name="keyword" value="${keyword}"/>
                                        <c:param name="productId" value="${productId}"/>
                                        <c:param name="score" value="${selectedScore}"/>
                                        <c:param name="fromDate" value="${fromDate}"/>
                                        <c:param name="toDate" value="${toDate}"/>
                                        <c:param name="sortBy" value="${selectedSortBy}"/>
                                    </c:url>
                                    <li class="page-item"><a class="page-link" href="${firstUrl}">1</a></li>
                                    <li class="page-item disabled"><span class="page-link">...</span></li>
                                </c:if>
                                <c:forEach begin="${startPage}" end="${endPage}" var="pageNum">
                                    <c:url var="pageUrl" value="/admin/reviews">
                                        <c:param name="p" value="${pageNum}"/>
                                        <c:param name="size" value="${pageSize}"/>
                                        <c:param name="keyword" value="${keyword}"/>
                                        <c:param name="productId" value="${productId}"/>
                                        <c:param name="score" value="${selectedScore}"/>
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
                                    <c:url var="lastUrl" value="/admin/reviews">
                                        <c:param name="p" value="${totalPages}"/>
                                        <c:param name="size" value="${pageSize}"/>
                                        <c:param name="keyword" value="${keyword}"/>
                                        <c:param name="productId" value="${productId}"/>
                                        <c:param name="score" value="${selectedScore}"/>
                                        <c:param name="fromDate" value="${fromDate}"/>
                                        <c:param name="toDate" value="${toDate}"/>
                                        <c:param name="sortBy" value="${selectedSortBy}"/>
                                    </c:url>
                                    <li class="page-item"><a class="page-link" href="${lastUrl}">${totalPages}</a></li>
                                </c:if>
                                <c:url var="nextUrl" value="/admin/reviews">
                                    <c:param name="p" value="${currentPage + 1}"/>
                                    <c:param name="size" value="${pageSize}"/>
                                    <c:param name="keyword" value="${keyword}"/>
                                    <c:param name="productId" value="${productId}"/>
                                    <c:param name="score" value="${selectedScore}"/>
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
