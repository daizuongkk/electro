<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<base href="${pageContext.request.contextPath}/">

<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Admin - Quản Lí Tài Khoản"/>
    <%@include file="../commons/admin-head.jsp" %>
    <style>
        .admin-user-avatar {
            width: 48px;
            height: 48px;
            border-radius: 50%;
            object-fit: cover;
            border: 1px solid #dee2e6;
            background: #fff;
            flex: 0 0 auto;
        }

        .admin-user-avatar-placeholder {
            width: 48px;
            height: 48px;
            border-radius: 50%;
            border: 1px solid #dee2e6;
            background: #f8f9fa;
            color: #6c757d;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            flex: 0 0 auto;
        }
    </style>
</head>
<body>
<div id="overlay" class="overlay"></div>
<%@include file="../commons/admin-header.jsp" %>
<%@include file="../commons/admin-sidebar.jsp" %>
<c:url var="currentUsersUrl" value="/admin/users">
    <c:param name="p" value="${currentPage}"/>
    <c:param name="size" value="${pageSize}"/>
    <c:param name="keyword" value="${keyword}"/>
    <c:param name="role" value="${selectedRole}"/>
    <c:param name="status" value="${selectedStatus}"/>
    <c:param name="verified" value="${selectedVerified}"/>
    <c:param name="deleted" value="${selectedDeleted}"/>
    <c:param name="sortBy" value="${selectedSortBy}"/>
</c:url>

<main id="content" class="content py-10">
    <div class="container-fluid">
        <div class="row">
            <div class="col-12">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <h1 class="fs-3 mb-1">Quản Lí Tài Khoản</h1>
                        <p class="mb-0">${totalUsers} tài khoản trong hệ thống</p>
                    </div>
                    <a href="admin/users/form" class="btn btn-primary">Thêm Người Dùng</a>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-12">
                <form method="get" action="admin/users" class="mb-3">
                    <div class="d-flex gap-2 flex-wrap align-items-end">
                        <div>
                            <label class="form-label small">Từ khóa</label>
                            <input type="text" name="keyword" class="form-control" placeholder="Username, email, tên, SĐT..."
                                   value="${fn:escapeXml(keyword)}" style="width: 320px;">
                        </div>
                        <div>
                            <label class="form-label small">Vai trò</label>
                            <select name="role" class="form-select" style="width: 150px;">
                                <option value="">Tất cả</option>
                                <c:forEach var="role" items="${roles}">
                                    <option value="${role}" ${selectedRole == role ? 'selected' : ''}>${role}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div>
                            <label class="form-label small">Trạng thái</label>
                            <select name="status" class="form-select" style="width: 150px;">
                                <option value="">Tất cả</option>
                                <option value="ACTIVE" ${selectedStatus == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                                <option value="INACTIVE" ${selectedStatus == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                                <option value="BANNED" ${selectedStatus == 'BANNED' ? 'selected' : ''}>BANNED</option>
                            </select>
                        </div>
                        <div>
                            <label class="form-label small">Xác thực</label>
                            <select name="verified" class="form-select" style="width: 150px;">
                                <option value="">Tất cả</option>
                                <option value="true" ${selectedVerified == 'true' ? 'selected' : ''}>Đã xác thực</option>
                                <option value="false" ${selectedVerified == 'false' ? 'selected' : ''}>Chưa xác thực</option>
                            </select>
                        </div>
                        <div>
                            <label class="form-label small">Xóa mềm</label>
                            <select name="deleted" class="form-select" style="width: 150px;">
                                <option value="" ${empty selectedDeleted ? 'selected' : ''}>Tất cả</option>
                                <option value="active" ${selectedDeleted == 'active' ? 'selected' : ''}>Hoạt động</option>
                                <option value="deleted" ${selectedDeleted == 'deleted' ? 'selected' : ''}>Đã xóa</option>
                            </select>
                        </div>
                        <div>
                            <label class="form-label small">Sắp xếp</label>
                            <select name="sortBy" class="form-select" style="width: 180px;">
                                <option value="created_desc" ${selectedSortBy == 'created_desc' ? 'selected' : ''}>Mới nhất</option>
                                <option value="created_asc" ${selectedSortBy == 'created_asc' ? 'selected' : ''}>Cũ nhất</option>
                                <option value="deleted_asc" ${selectedSortBy == 'deleted_asc' ? 'selected' : ''}>Hoạt động trước</option>
                                <option value="deleted_desc" ${selectedSortBy == 'deleted_desc' ? 'selected' : ''}>Đã xóa trước</option>
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
                        <a href="admin/users" class="btn btn-outline-secondary">Xóa lọc</a>
                    </div>
                </form>

                <div class="card table-responsive">
                    <table class="table mb-0 text-nowrap table-hover">
                        <thead class="table-light border-light">
                        <tr>
                            <th>Tài khoản</th>
                            <th>Email</th>
                            <th>Điện thoại</th>
                            <th>Vai trò</th>
                            <th>Trạng thái</th>
                            <th>Xóa mềm</th>
                            <th>Xác thực</th>
                            <th>Ngày tạo</th>
                            <th>Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="user" items="${users}">
                            <tr class="align-middle">
                                <td>
                                    <div class="d-flex align-items-center">
                                        <c:choose>
                                            <c:when test="${not empty user.avtUrl}">
                                                <img src="${fn:escapeXml(user.avtUrl)}"
                                                     alt="${fn:escapeXml(user.username)}" class="admin-user-avatar">
                                            </c:when>
                                            <c:otherwise>
                                                <span class="admin-user-avatar-placeholder">
                                                    <i class="ti ti-user"></i>
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                        <div class="ms-3">
                                            <div class="fw-semibold">${user.username}</div>
                                            <small class="text-secondary">${user.firstName} ${user.lastName}</small>
                                        </div>
                                    </div>
                                </td>
                                <td>${user.email}</td>
                                <td>${empty user.phone ? 'N/A' : user.phone}</td>
                                <td>${user.role}</td>
                                <td>
                                    <span class="badge ${user.status == 'ACTIVE' ? 'bg-success' : (user.status == 'BANNED' ? 'bg-danger' : 'bg-secondary')}">
                                            ${user.status}
                                    </span>
                                </td>
                                <td>
                                    <span class="badge ${user.deleted ? 'bg-danger' : 'bg-success'}">
                                        ${user.deleted ? 'Đã xóa' : 'Hoạt động'}
                                    </span>
                                </td>
                                <td>${user.verified ? 'Đã xác thực' : 'Chưa xác thực'}</td>
                                <td><fmt:formatDate value="${user.createdAt}" pattern="dd/MM/yyyy"/></td>
                                <td>
                                    <a href="admin/users/form?id=${user.id}" class="btn btn-sm btn-outline-primary">
                                        <i class="ti ti-edit"></i> Sửa
                                    </a>
                                    <c:if test="${sessionScope.account.id != user.id and not user.deleted}">
                                        <form method="post" action="admin/users" class="d-inline"
                                              data-confirm-message="Bạn chắc chắn muốn xóa người dùng này?"
                                              data-confirm-text="Xóa">
                                            <input type="hidden" name="action" value="delete-user">
                                            <input type="hidden" name="id" value="${user.id}">
                                            <input type="hidden" name="returnUrl" value="${currentUsersUrl}">
                                            <button class="btn btn-sm btn-outline-danger" type="submit">
                                                <i class="ti ti-trash"></i> Xóa
                                            </button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty users}">
                            <tr>
                                <td colspan="9" class="text-center py-5 text-secondary">Không có tài khoản phù hợp.</td>
                            </tr>
                        </c:if>
                        </tbody>
                        <tfoot>
                        <tr>
                            <td class="border-bottom-0">Trang ${currentPage}/${totalPages}</td>
                            <td colspan="8" class="border-bottom-0">
                                <nav aria-label="Page navigation" class="d-flex justify-content-end">
                                    <ul class="pagination mb-0">
                                        <c:url var="prevUrl" value="/admin/users">
                                            <c:param name="p" value="${currentPage - 1}"/>
                                            <c:param name="size" value="${pageSize}"/>
                                            <c:param name="keyword" value="${keyword}"/>
                                            <c:param name="role" value="${selectedRole}"/>
                                            <c:param name="status" value="${selectedStatus}"/>
                                            <c:param name="verified" value="${selectedVerified}"/>
                                            <c:param name="deleted" value="${selectedDeleted}"/>
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
                                            <c:url var="firstUrl" value="/admin/users">
                                                <c:param name="p" value="1"/>
                                                <c:param name="size" value="${pageSize}"/>
                                                <c:param name="keyword" value="${keyword}"/>
                                                <c:param name="role" value="${selectedRole}"/>
                                                <c:param name="status" value="${selectedStatus}"/>
                                                <c:param name="verified" value="${selectedVerified}"/>
                                                <c:param name="deleted" value="${selectedDeleted}"/>
                                                <c:param name="sortBy" value="${selectedSortBy}"/>
                                            </c:url>
                                            <li class="page-item"><a class="page-link" href="${firstUrl}">1</a></li>
                                            <li class="page-item disabled"><span class="page-link">...</span></li>
                                        </c:if>
                                        <c:forEach begin="${startPage}" end="${endPage}" var="pageNum">
                                            <c:url var="pageUrl" value="/admin/users">
                                                <c:param name="p" value="${pageNum}"/>
                                                <c:param name="size" value="${pageSize}"/>
                                                <c:param name="keyword" value="${keyword}"/>
                                                <c:param name="role" value="${selectedRole}"/>
                                                <c:param name="status" value="${selectedStatus}"/>
                                                <c:param name="verified" value="${selectedVerified}"/>
                                                <c:param name="deleted" value="${selectedDeleted}"/>
                                                <c:param name="sortBy" value="${selectedSortBy}"/>
                                            </c:url>
                                            <li class="page-item ${pageNum == currentPage ? 'active' : ''}">
                                                <a class="page-link" href="${pageUrl}">${pageNum}</a>
                                            </li>
                                        </c:forEach>
                                        <c:if test="${endPage < totalPages}">
                                            <li class="page-item disabled"><span class="page-link">...</span></li>
                                            <c:url var="lastUrl" value="/admin/users">
                                                <c:param name="p" value="${totalPages}"/>
                                                <c:param name="size" value="${pageSize}"/>
                                                <c:param name="keyword" value="${keyword}"/>
                                                <c:param name="role" value="${selectedRole}"/>
                                                <c:param name="status" value="${selectedStatus}"/>
                                                <c:param name="verified" value="${selectedVerified}"/>
                                                <c:param name="deleted" value="${selectedDeleted}"/>
                                                <c:param name="sortBy" value="${selectedSortBy}"/>
                                            </c:url>
                                            <li class="page-item"><a class="page-link" href="${lastUrl}">${totalPages}</a></li>
                                        </c:if>
                                        <c:url var="nextUrl" value="/admin/users">
                                            <c:param name="p" value="${currentPage + 1}"/>
                                            <c:param name="size" value="${pageSize}"/>
                                            <c:param name="keyword" value="${keyword}"/>
                                            <c:param name="role" value="${selectedRole}"/>
                                            <c:param name="status" value="${selectedStatus}"/>
                                            <c:param name="verified" value="${selectedVerified}"/>
                                            <c:param name="deleted" value="${selectedDeleted}"/>
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
        </div>
    </div>
</main>
</body>
</html>
