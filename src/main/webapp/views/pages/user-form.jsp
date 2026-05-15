<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<base href="${pageContext.request.contextPath}/">

<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="${editMode ? 'Admin - Chỉnh Sửa Người Dùng' : 'Admin - Thêm Người Dùng'}"/>
    <%@include file="../commons/admin-head.jsp" %>
    <style>
        .admin-avatar-editor {
            display: flex;
            align-items: center;
            gap: 16px;
            padding: 16px;
            border: 1px solid #dee2e6;
            border-radius: 8px;
            background: #fff;
        }

        .admin-avatar-preview {
            width: 96px;
            height: 96px;
            border-radius: 50%;
            object-fit: cover;
            border: 1px solid #dee2e6;
            background: #f8f9fa;
            flex: 0 0 auto;
        }

        .admin-avatar-placeholder {
            width: 96px;
            height: 96px;
            border-radius: 50%;
            border: 1px solid #dee2e6;
            background: #f8f9fa;
            color: #6c757d;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 32px;
            flex: 0 0 auto;
        }

        .admin-submit-overlay {
            position: fixed;
            inset: 0;
            z-index: 2000;
            display: none;
            align-items: center;
            justify-content: center;
            background: rgba(248, 249, 250, 0.72);
            backdrop-filter: blur(2px);
        }

        .admin-submit-overlay.is-active {
            display: flex;
        }

        .admin-submit-box {
            min-width: 220px;
            padding: 18px 22px;
            border-radius: 8px;
            background: #fff;
            box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18);
            text-align: center;
            font-weight: 600;
        }

        .admin-submit-box .spinner-border {
            width: 1.35rem;
            height: 1.35rem;
            margin-right: 10px;
            vertical-align: -0.2rem;
        }
    </style>
</head>
<body>
<div id="adminSubmitOverlay" class="admin-submit-overlay">
    <div class="admin-submit-box">
        <span class="spinner-border spinner-border-sm" aria-hidden="true"></span>
        Đang lưu...
    </div>
</div>
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
                        <c:if test="${editMode and userForm.deleted}">
                            <div class="alert alert-warning">
                                Người dùng này đang ở trạng thái đã xóa. Lưu thay đổi để kích hoạt lại tài khoản.
                            </div>
                        </c:if>

                        <form method="post" action="admin/users" enctype="multipart/form-data">
                            <c:if test="${editMode}">
                                <input type="hidden" name="id" value="${userForm.id}">
                            </c:if>
                            <input type="hidden" name="avatarUrl" value="${fn:escapeXml(userForm.avtUrl)}">

                            <div class="mb-4">
                                <label for="avatarFile" class="form-label">Ảnh đại diện</label>
                                <div class="admin-avatar-editor">
                                    <c:choose>
                                        <c:when test="${not empty userForm.avtUrl}">
                                            <img id="adminAvatarPreview" src="${fn:escapeXml(userForm.avtUrl)}"
                                                 alt="${fn:escapeXml(userForm.username)}" class="admin-avatar-preview">
                                            <span id="adminAvatarPlaceholder" class="admin-avatar-placeholder d-none">
                                                <i class="ti ti-user"></i>
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <img id="adminAvatarPreview" src="" alt="" class="admin-avatar-preview d-none">
                                            <span id="adminAvatarPlaceholder" class="admin-avatar-placeholder">
                                                <i class="ti ti-user"></i>
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                    <div class="flex-grow-1">
                                        <input type="file" class="form-control" id="avatarFile" name="avatarFile" accept="image/*">
                                        <small class="text-secondary">Chọn 1 ảnh đại diện, tối đa 10MB. Bỏ trống nếu không muốn thay đổi.</small>
                                    </div>
                                </div>
                            </div>

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
<script>
    (function () {
        var input = document.getElementById('avatarFile');
        var preview = document.getElementById('adminAvatarPreview');
        var placeholder = document.getElementById('adminAvatarPlaceholder');
        var form = input ? input.closest('form') : null;
        var overlay = document.getElementById('adminSubmitOverlay');
        var maxAvatarSize = 10 * 1024 * 1024;

        if (!input || !preview || !placeholder) {
            return;
        }

        function validateAvatarFile(file) {
            if (!file) {
                return true;
            }
            if (!file.type || !file.type.startsWith('image/')) {
                alert('Vui lòng chọn một file ảnh hợp lệ.');
                return false;
            }
            if (file.size > maxAvatarSize) {
                alert('Ảnh đại diện tối đa 10MB. Vui lòng chọn ảnh nhẹ hơn.');
                return false;
            }
            return true;
        }

        input.addEventListener('change', function () {
            var file = input.files && input.files[0];
            if (!file) {
                return;
            }

            if (!validateAvatarFile(file)) {
                input.value = '';
                return;
            }

            preview.src = URL.createObjectURL(file);
            preview.classList.remove('d-none');
            placeholder.classList.add('d-none');
        });

        if (form) {
            form.addEventListener('submit', function (event) {
                var file = input.files && input.files[0];
                if (!validateAvatarFile(file)) {
                    input.value = '';
                    event.preventDefault();
                    return;
                }

                form.querySelectorAll('button[type="submit"]').forEach(function (button) {
                    button.disabled = true;
                    button.dataset.originalText = button.textContent;
                    button.innerHTML = '<span class="spinner-border spinner-border-sm" aria-hidden="true"></span> Đang lưu...';
                });
                if (overlay) {
                    overlay.classList.add('is-active');
                }
            });
        }
    })();
</script>
</body>
</html>
