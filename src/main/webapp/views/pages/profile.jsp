<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <c:set var="pageTitle" value="Electro - Thông Tin Tài Khoản"/>
    <%@ include file="../commons/head.jsp" %>
    <link type="text/css" rel="stylesheet" href="assets/css/profile.css"/>
</head>
<body id="profile-page">
<%@ include file="../commons/header.jsp" %>
<jsp:include page="../commons/navigation.jsp"/>

<%-- Breadcrumb — identical to cart page --%>
<div id="breadcrumb" class="section">
    <div class="container">
        <div class="row">
            <div class="col-md-12">
                <ul class="breadcrumb-tree">
                    <li><a href="home">Trang chủ</a></li>
                    <li class="active">Tài khoản</li>
                </ul>
            </div>
        </div>
    </div>
</div>

<div class="section profile-section">
    <div class="container">
        <div class="row">

            <%-- ── Sidebar ── --%>
            <div class="col-md-3 col-sm-12">
                <div class="profile-sidebar">
                    <div class="profile-avatar">
                        <c:choose>
                            <c:when test="${not empty profileUser.avtUrl}">
                                <img id="avatarPreview"
                                     src="${fn:escapeXml(profileUser.avtUrl)}"
                                     alt="${fn:escapeXml(profileUser.username)}">
                            </c:when>
                            <c:otherwise>
                                <i id="avatarPlaceholder" class="fa fa-user-o"></i>
                                <img id="avatarPreview"
                                     class="hidden"
                                     src=""
                                     alt="${fn:escapeXml(profileUser.username)}">
                            </c:otherwise>
                        </c:choose>

                        <button type="button"
                                class="avatar-upload-trigger"
                                id="avatarUploadTrigger"
                                aria-label="Chọn ảnh đại diện">
                            <i class="fa fa-camera"></i>
                        </button>
                    </div>
 
                    <div class="sidebar-name">
                        <c:choose>
                            <c:when test="${not empty profileUser.firstName or not empty profileUser.lastName}">
                                ${fn:escapeXml(profileUser.firstName)} ${fn:escapeXml(profileUser.lastName)}
                            </c:when>
                            <c:otherwise>${fn:escapeXml(profileUser.username)}</c:otherwise>
                        </c:choose>
                    </div>
                    <div class="sidebar-email">${fn:escapeXml(profileUser.email)}</div>

                    <hr class="sidebar-divider">

                    <a href="cart" class="sidebar-cart-link">
                        <i class="fa fa-shopping-cart"></i>
                        Xem giỏ hàng
                    </a>
                </div>
            </div>

            <%-- ── Main panel ── --%>
            <div class="col-md-9 col-sm-12">
                <div class="profile-panel">

                    <div class="panel-header">
                        <h3>Thông Tin Cá Nhân</h3>
                        <p>Cập nhật thông tin hiển thị và thông tin liên hệ của tài khoản.</p>
                    </div>

                    <c:if test="${not empty profileSuccess}">
                        <div class="js-popup-message hidden" data-type="success" data-message="${fn:escapeXml(profileSuccess)}"></div>
                    </c:if>
                    <c:if test="${not empty profileError}">
                        <div class="js-popup-message hidden" data-type="danger" data-message="${fn:escapeXml(profileError)}"></div>
                    </c:if>

                    <form method="post" action="profile" class="profile-form" enctype="multipart/form-data">
                        <input type="hidden" name="profileAction" value="updateProfile">
                        <input class="hidden" id="avatarFile" name="avatarFile" type="file" accept="image/*">

                        <div class="form-section-label">Thông tin đăng nhập</div>

                        <div class="row">
                            <div class="col-sm-6">
                                <div class="form-group">
                                    <label for="username">Tên đăng nhập</label>
                                    <input class="input" id="username" name="username" type="text"
                                           value="${fn:escapeXml(profileUser.username)}" required>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="form-group">
                                    <label for="email">Email</label>
                                    <input class="input" id="email" name="email" type="email"
                                           value="${fn:escapeXml(profileUser.email)}" required>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-sm-6">
                                <div class="form-group">
                                    <label for="passwordDisplay">Mật khẩu</label>
                                    <input class="input" id="passwordDisplay" type="password" value="********" disabled>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="form-group password-action-group">
                                    <label>&nbsp;</label>
                                    <button type="button" class="profile-secondary-btn" data-toggle="modal"
                                            data-target="#changePasswordModal">
                                        <i class="fa fa-lock"></i>Đổi mật khẩu
                                    </button>
                                </div>
                            </div>
                        </div>

                        <div class="form-section-label">Thông tin cá nhân</div>

                        <div class="row">
                            <div class="col-sm-6">
                                <div class="form-group">
                                    <label for="lastName">Họ</label>
                                    <input class="input" id="lastName" name="lastName" type="text"
                                           value="${fn:escapeXml(profileUser.lastName)}">
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="form-group">
                                    <label for="firstName">Tên</label>
                                    <input class="input" id="firstName" name="firstName" type="text"
                                           value="${fn:escapeXml(profileUser.firstName)}">
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-sm-12">
                                <div class="form-group">
                                    <label for="phone">Số điện thoại</label>
                                    <input class="input" id="phone" name="phone" type="tel"
                                           value="${fn:escapeXml(profileUser.phone)}">
                                </div>
                            </div>
                        </div>

                        <div class="profile-actions">
                            <button type="submit" class="primary-btn">
                                <i class="fa fa-save"></i>Lưu thay đổi
                            </button>
                            <a href="home" class="btn-cancel">Hủy</a>
                        </div>

                    </form>
                </div><%-- /profile-panel --%>
            </div><%-- /col-md-9 --%>

        </div><%-- /row --%>
    </div><%-- /container --%>
</div><%-- /section --%>

<div class="modal fade profile-password-modal" id="changePasswordModal" tabindex="-1" role="dialog"
     aria-labelledby="changePasswordTitle" aria-hidden="true">
    <div class="modal-dialog modal-sm" role="document">
        <div class="modal-content">
            <button type="button" class="app-popup-close" data-dismiss="modal" aria-label="Đóng">
                <i class="fa fa-close"></i>
            </button>
            <div class="app-popup-icon warning">
                <i class="fa fa-lock"></i>
            </div>
            <h4 class="app-popup-title" id="changePasswordTitle">Đổi mật khẩu</h4>
            <form method="post" action="profile" class="profile-form profile-password-form">
                <input type="hidden" name="profileAction" value="changePassword">

                <div class="form-group">
                    <label for="modalCurrentPassword">Mật khẩu hiện tại</label>
                    <input class="input" id="modalCurrentPassword" name="currentPassword"
                           type="password" autocomplete="current-password" required>
                </div>

                <div class="form-group">
                    <label for="modalNewPassword">Mật khẩu mới</label>
                    <input class="input" id="modalNewPassword" name="newPassword"
                           type="password" autocomplete="new-password" minlength="8" maxlength="32" required>
                </div>

                <div class="form-group">
                    <label for="modalConfirmPassword">Xác nhận mật khẩu</label>
                    <input class="input" id="modalConfirmPassword" name="confirmPassword"
                           type="password" autocomplete="new-password" minlength="8" maxlength="32" required>
                </div>

                <div class="profile-modal-actions">
                    <button type="button" class="profile-modal-cancel" data-dismiss="modal">Hủy</button>
                    <button type="submit" class="primary-btn">Xác nhận</button>
                </div>
            </form>
        </div>
    </div>
</div>

<%@ include file="../commons/footer.jsp" %>
<%@ include file="../commons/script.jsp" %>
<script>
    (function () {
        var trigger = document.getElementById('avatarUploadTrigger');
        var input = document.getElementById('avatarFile');
        var preview = document.getElementById('avatarPreview');
        var placeholder = document.getElementById('avatarPlaceholder');

        if (!trigger || !input || !preview) {
            return;
        }

        trigger.addEventListener('click', function () {
            input.click();
        });

        input.addEventListener('change', function () {
            var file = input.files && input.files[0];
            if (!file) {
                return;
            }

            if (!file.type || !file.type.startsWith('image/')) {
                window.alert('Vui lòng chọn một file ảnh hợp lệ.');
                input.value = '';
                return;
            }

            preview.src = URL.createObjectURL(file);
            preview.classList.remove('hidden');
            if (placeholder) {
                placeholder.classList.add('hidden');
            }
        });
    })();
</script>
</body>
</html>
