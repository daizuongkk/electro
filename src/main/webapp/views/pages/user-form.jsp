<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<base href="${pageContext.request.contextPath}/">

<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="${editMode ? 'Admin - Chỉnh Sửa Người Dùng' : 'Admin - Thêm Người Dùng'}"/>
    <%@include file="../commons/admin-head.jsp" %>
</head>
<body>
<div id="overlay" class="overlay"></div>
<%@include file="../commons/admin-header.jsp" %>
<%@include file="../commons/admin-sidebar.jsp" %>

<main id="content" class="content py-10">
    <div class="container-fluid">
        <div class="row">
            <div class="col-12">
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
                    <div>
                        <h1 class="fs-3 mb-1">${editMode ? 'Chỉnh Sửa Người Dùng' : 'Thêm Người Dùng'}</h1>
                        <p class="mb-0">${editMode ? 'Cập nhật tài khoản người dùng' : 'Tạo tài khoản mới từ trang quản trị'}</p>
                    </div>
                    <a href="admin/users" class="btn btn-primary">Danh Sách Người Dùng</a>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-12">
                <div class="card">
                    <div class="card-body p-4">
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger">${fn:escapeXml(error)}</div>
                        </c:if>

                        <form method="post" action="admin/users">
                            <c:if test="${editMode}">
                                <input type="hidden" name="id" value="${userForm.id}">
                            </c:if>

                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label for="username" class="form-label">Username</label>
                                    <input type="text" class="form-control" id="username" name="username"
                                           value="${fn:escapeXml(userForm.username)}" required>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label for="email" class="form-label">Email</label>
                                    <input type="email" class="form-control" id="email" name="email"
                                           value="${fn:escapeXml(userForm.email)}" required>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-4 mb-3">
                                    <label for="firstName" class="form-label">Tên</label>
                                    <input type="text" class="form-control" id="firstName" name="firstName"
                                           value="${fn:escapeXml(userForm.firstName)}">
                                </div>
                                <div class="col-md-4 mb-3">
                                    <label for="lastName" class="form-label">Họ</label>
                                    <input type="text" class="form-control" id="lastName" name="lastName"
                                           value="${fn:escapeXml(userForm.lastName)}">
                                </div>
                                <div class="col-md-4 mb-3">
                                    <label for="phone" class="form-label">Số điện thoại</label>
                                    <input type="text" class="form-control" id="phone" name="phone"
                                           value="${fn:escapeXml(userForm.phone)}">
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-4 mb-3">
                                    <label for="role" class="form-label">Vai trò</label>
                                    <select class="form-select" id="role" name="role">
                                        <c:forEach var="role" items="${roles}">
                                            <option value="${role}" ${userForm.role == role ? 'selected' : ''}>${role}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-4 mb-3">
                                    <label for="status" class="form-label">Trạng thái</label>
                                    <select class="form-select" id="status" name="status">
                                        <option value="ACTIVE" ${userForm.status == 'ACTIVE' or empty userForm.status ? 'selected' : ''}>ACTIVE</option>
                                        <option value="INACTIVE" ${userForm.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                                        <option value="BANNED" ${userForm.status == 'BANNED' ? 'selected' : ''}>BANNED</option>
                                    </select>
                                </div>
                                <div class="col-md-4 mb-3">
                                    <label for="verified" class="form-label">Xác thực</label>
                                    <select class="form-select" id="verified" name="verified">
                                        <option value="true" ${userForm.verified ? 'selected' : ''}>Đã xác thực</option>
                                        <option value="false" ${not userForm.verified ? 'selected' : ''}>Chưa xác thực</option>
                                    </select>
                                </div>
                            </div>

                            <div class="mb-3">
                                <label for="password" class="form-label">${editMode ? 'Mật khẩu mới' : 'Mật khẩu'}</label>
                                <input type="password" class="form-control" id="password" name="password"
                                       ${editMode ? '' : 'required'}
                                       placeholder="${editMode ? 'Bỏ trống nếu không đổi mật khẩu' : 'Nhập mật khẩu'}">
                            </div>

                            <div class="d-flex gap-2">
                                <button type="submit" class="btn btn-primary">${editMode ? 'Lưu Thay Đổi' : 'Thêm Người Dùng'}</button>
                                <button type="reset" class="btn btn-secondary">Xóa Form</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>
</body>
</html>
