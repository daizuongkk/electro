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
<%-- <div id="profileSubmitOverlay" class="profile-submit-overlay">
    <div class="profile-submit-box">
        <span class="profile-spinner" aria-hidden="true"></span>
        Đang lưu...
    </div>
</div> --%>
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
                                           value="${fn:escapeXml(profileUser.username)}" readonly>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="form-group">
                                    <label for="email">Email</label>
                                    <input class="input" id="email" name="email" type="email"
                                           value="${fn:escapeXml(profileUser.email)}"
                                    ${profileUser.verified ? 'readonly' : ''}
                                           required>
                                    <div class="verification-status-row" id="emailVerificationStatus">
                                        <c:choose>
                                            <c:when test="${profileUser.verified}">
                                                <span class="verification-badge verified">
                                                    <i class="fa fa-check-circle"></i>Đã xác minh
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <button type="submit" form="sendEmailOtpForm"
                                                        class="verification-request-btn">
                                                    <i class="fa fa-envelope-o"></i>Yêu cầu xác minh
                                                </button>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="otp-inline-box ${activeVerificationChannel == 'EMAIL' ? '' : 'hidden'}"
                                         id="emailOtpBox">
                                        <label for="emailOtp">Nhập OTP email</label>
                                        <div class="otp-target" id="emailOtpTarget">
                                            <c:if test="${activeVerificationChannel == 'EMAIL'}">
                                                Mã đã gửi đến ${fn:escapeXml(verificationTarget)}
                                            </c:if>
                                        </div>
                                        <div class="otp-control">
                                            <input class="input" id="emailOtp" type="text"
                                                   inputmode="numeric"
                                                   maxlength="6" pattern="[0-9]{6}" placeholder="000000">
                                            <button type="submit" form="verifyEmailOtpForm" class="primary-btn">Xác
                                                minh
                                            </button>
                                        </div>
                                    </div>
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
                                    <div class="verification-status-row" id="phoneVerificationStatus">
                                        <c:choose>
                                            <c:when test="${profileUser.phoneVerified}">
                                                <span class="verification-badge verified">
                                                    <i class="fa fa-check-circle"></i>Đã xác minh
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <button type="submit" form="sendPhoneOtpForm"
                                                        class="verification-request-btn">
                                                    <i class="fa fa-mobile"></i>Yêu cầu xác minh
                                                </button>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="otp-inline-box ${activeVerificationChannel == 'PHONE' ? '' : 'hidden'}"
                                         id="phoneOtpBox">
                                        <label for="phoneOtp">Nhập OTP SMS</label>
                                        <div class="otp-target" id="phoneOtpTarget">
                                            <c:if test="${activeVerificationChannel == 'PHONE'}">
                                                Mã đã gửi đến ${fn:escapeXml(verificationTarget)}
                                            </c:if>
                                        </div>
                                        <div class="otp-control">
                                            <input class="input" id="phoneOtp" type="text"
                                                   inputmode="numeric"
                                                   maxlength="6" pattern="[0-9]{6}" placeholder="000000">
                                            <button type="submit" form="verifyPhoneOtpForm" class="primary-btn">Xác
                                                minh
                                            </button>
                                        </div>
                                    </div>
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

<form id="sendEmailOtpForm" method="post" action="profile" class="hidden">
    <input type="hidden" name="profileAction" value="sendVerificationOtp">
    <input type="hidden" name="channel" value="EMAIL">
    <input type="hidden" name="targetValue" value="">
</form>
<form id="verifyEmailOtpForm" method="post" action="profile" class="hidden">
    <input type="hidden" name="profileAction" value="verifyOtp">
    <input type="hidden" name="channel" value="EMAIL">
    <input type="hidden" name="targetValue" value="${activeVerificationChannel == 'EMAIL' ? fn:escapeXml(verificationTarget) : ''}">
    <input type="hidden" name="otp" value="">
</form>
<form id="sendPhoneOtpForm" method="post" action="profile" class="hidden">
    <input type="hidden" name="profileAction" value="sendVerificationOtp">
    <input type="hidden" name="channel" value="PHONE">
    <input type="hidden" name="targetValue" value="">
</form>
<form id="verifyPhoneOtpForm" method="post" action="profile" class="hidden">
    <input type="hidden" name="profileAction" value="verifyOtp">
    <input type="hidden" name="channel" value="PHONE">
    <input type="hidden" name="targetValue" value="${activeVerificationChannel == 'PHONE' ? fn:escapeXml(verificationTarget) : ''}">
    <input type="hidden" name="otp" value="">
</form>

<%@ include file="../commons/footer.jsp" %>
<%@ include file="../commons/script.jsp" %>
<script>
    (function () {
        var maxAvatarSize = 10 * 1024 * 1024;

        function validateAvatarFile(file) {
            if (!file) {
                return true;
            }
            if (!file.type || !file.type.startsWith('image/')) {
                window.alert('Vui lòng chọn một file ảnh hợp lệ.');
                return false;
            }
            if (file.size > maxAvatarSize) {
                window.alert('Ảnh đại diện tối đa 10MB. Vui lòng chọn ảnh nhẹ hơn.');
                return false;
            }
            return true;
        }

        function lockForm(form, text) {
            if (!form) {
                return;
            }
            form.querySelectorAll('button[type="submit"]').forEach(function (button) {
                button.disabled = true;
                button.dataset.originalHtml = button.innerHTML;
                button.innerHTML = '<i class="fa fa-spinner fa-spin"></i>' + (text || 'Đang lưu');
            });
            var overlay = document.getElementById('profileSubmitOverlay');
            if (overlay) {
                overlay.classList.add('is-active');
            }
        }

        var verificationConfig = {
            EMAIL: {
                sendFormId: 'sendEmailOtpForm',
                verifyFormId: 'verifyEmailOtpForm',
                sourceInputId: 'email',
                otpInputId: 'emailOtp',
                otpBoxId: 'emailOtpBox',
                otpTargetId: 'emailOtpTarget',
                statusId: 'emailVerificationStatus',
                buttonIcon: 'fa-envelope-o'
            },
            PHONE: {
                sendFormId: 'sendPhoneOtpForm',
                verifyFormId: 'verifyPhoneOtpForm',
                sourceInputId: 'phone',
                otpInputId: 'phoneOtp',
                otpBoxId: 'phoneOtpBox',
                otpTargetId: 'phoneOtpTarget',
                statusId: 'phoneVerificationStatus',
                buttonIcon: 'fa-mobile'
            }
        };

        function setHiddenTarget(formId, value) {
            var form = document.getElementById(formId);
            if (!form) {
                return;
            }
            var targetInput = form.querySelector('input[name="targetValue"]');
            if (targetInput) {
                targetInput.value = value || '';
            }
        }

        function getHiddenTarget(formId) {
            var form = document.getElementById(formId);
            var targetInput = form ? form.querySelector('input[name="targetValue"]') : null;
            return targetInput ? targetInput.value : '';
        }

        function submitVerificationForm(form, submitButton) {
            var originalText = submitButton ? submitButton.innerHTML : '';
            if (submitButton) {
                submitButton.disabled = true;
                submitButton.innerHTML = '<i class="fa fa-spinner fa-spin"></i>Đang xử lý';
            }

            return $.ajax({
                url: form.action,
                method: 'POST',
                data: $(form).serialize(),
                dataType: 'json',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            }).always(function () {
                if (submitButton) {
                    submitButton.disabled = false;
                    submitButton.innerHTML = originalText;
                }
            });
        }

        function showOtpBox(channel, target) {
            var config = verificationConfig[channel];
            if (!config) {
                return;
            }
            var otpBox = document.getElementById(config.otpBoxId);
            var otpTarget = document.getElementById(config.otpTargetId);
            var otpInput = document.getElementById(config.otpInputId);
            if (otpBox) {
                otpBox.classList.remove('hidden');
            }
            if (otpTarget) {
                otpTarget.textContent = 'Mã đã gửi đến ' + target;
            }
            if (otpInput) {
                otpInput.value = '';
                otpInput.focus();
            }
        }

        function hideOtpBox(channel) {
            var config = verificationConfig[channel];
            var otpBox = config ? document.getElementById(config.otpBoxId) : null;
            if (otpBox) {
                otpBox.classList.add('hidden');
            }
        }

        function renderVerified(channel) {
            var config = verificationConfig[channel];
            var status = config ? document.getElementById(config.statusId) : null;
            if (!status) {
                return;
            }
            status.innerHTML = '<span class="verification-badge verified"><i class="fa fa-check-circle"></i>Đã xác minh</span>';
            hideOtpBox(channel);
        }

        function renderRequestButton(channel) {
            var config = verificationConfig[channel];
            var status = config ? document.getElementById(config.statusId) : null;
            if (!status) {
                return;
            }
            status.innerHTML = '<button type="submit" form="' + config.sendFormId + '" class="verification-request-btn">'
                + '<i class="fa ' + config.buttonIcon + '"></i>Yêu cầu xác minh</button>';
        }

        function wireVerification(channel) {
            var config = verificationConfig[channel];
            var sendForm = document.getElementById(config.sendFormId);
            var verifyForm = document.getElementById(config.verifyFormId);
            var sourceInput = document.getElementById(config.sourceInputId);

            if (sourceInput) {
                sourceInput.addEventListener('input', function () {
                    if (sourceInput.readOnly) {
                        return;
                    }
                    renderRequestButton(channel);
                    hideOtpBox(channel);
                    setHiddenTarget(config.verifyFormId, '');
                });
            }

            if (sendForm && sourceInput) {
                sendForm.addEventListener('submit', function (event) {
                    event.preventDefault();
                    var target = sourceInput.value.trim();
                    setHiddenTarget(config.sendFormId, target);

                    submitVerificationForm(sendForm, document.querySelector('[form="' + config.sendFormId + '"]'))
                        .done(function (res) {
                            if (res && res.success) {
                                setHiddenTarget(config.verifyFormId, res.target || target);
                                showOtpBox(channel, res.target || target);
                                window.showElectroPopup(res.message, 'success');
                            } else {
                                window.showElectroPopup((res && res.message) || 'Không thể gửi mã OTP.', 'danger');
                            }
                        })
                        .fail(function () {
                            window.showElectroPopup('Không thể gửi mã OTP. Vui lòng thử lại sau.', 'danger');
                        });
                });
            }

            if (verifyForm) {
                verifyForm.addEventListener('submit', function (event) {
                    event.preventDefault();
                    if (!getHiddenTarget(config.verifyFormId)) {
                        window.showElectroPopup('Vui lòng yêu cầu mã OTP trước khi xác minh.', 'warning');
                        return;
                    }
                    var visibleOtpInput = document.getElementById(config.otpInputId);
                    var hiddenOtpInput = verifyForm.querySelector('input[name="otp"]');
                    if (!visibleOtpInput || visibleOtpInput.value.trim().length !== 6) {
                        window.showElectroPopup('Vui lòng nhập mã OTP gồm 6 chữ số.', 'warning');
                        return;
                    }
                    if (hiddenOtpInput && visibleOtpInput) {
                        hiddenOtpInput.value = visibleOtpInput.value;
                    }

                    submitVerificationForm(verifyForm, document.querySelector('[form="' + config.verifyFormId + '"]'))
                        .done(function (res) {
                            if (res && res.success) {
                                renderVerified(channel);
                                if (sourceInput && res.target) {
                                    sourceInput.value = res.target;
                                }
                                window.showElectroPopup(res.message, 'success');
                            } else {
                                showOtpBox(channel, getHiddenTarget(config.verifyFormId));
                                window.showElectroPopup((res && res.message) || 'Mã OTP không hợp lệ.', 'danger');
                            }
                        })
                        .fail(function () {
                            window.showElectroPopup('Không thể xác minh OTP. Vui lòng thử lại sau.', 'danger');
                        });
                });
            }
        }

        wireVerification('EMAIL');
        wireVerification('PHONE');

        $('.otp-control input').on('input', function () {
            this.value = this.value.replace(/\D/g, '').slice(0, 6);
        });

        $('button[form="sendEmailOtpForm"], button[form="sendPhoneOtpForm"], button[form="verifyEmailOtpForm"], button[form="verifyPhoneOtpForm"]').on('click', function () {
            var form = document.getElementById(this.getAttribute('form'));
            if (form) {
                form.dataset.submitButtonSelector = '[form="' + this.getAttribute('form') + '"]';
            }
        });

        Object.keys(verificationConfig).forEach(function (channel) {
            var config = verificationConfig[channel];
            var target = getHiddenTarget(config.verifyFormId);
            if (target) {
                showOtpBox(channel, target);
            }
        });

        function wireVerificationForm(formId, sourceInputId) {
            var form = document.getElementById(formId);
            var sourceInput = document.getElementById(sourceInputId);
            if (!form || !sourceInput) {
                return;
            }
            form.addEventListener('submit', function () {
                setHiddenTarget(formId, sourceInput.value);
            });
        }

        wireVerificationForm('sendEmailOtpForm', 'email');
        wireVerificationForm('sendPhoneOtpForm', 'phone');

        var trigger = document.getElementById('avatarUploadTrigger');
        var input = document.getElementById('avatarFile');
        var preview = document.getElementById('avatarPreview');
        var placeholder = document.getElementById('avatarPlaceholder');
        var profileForm = input ? input.closest('form') : null;

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

            if (!validateAvatarFile(file)) {
                input.value = '';
                return;
            }

            preview.src = URL.createObjectURL(file);
            preview.classList.remove('hidden');
            if (placeholder) {
                placeholder.classList.add('hidden');
            }
        });

        if (profileForm) {
            profileForm.addEventListener('submit', function (event) {
                var file = input.files && input.files[0];
                if (!validateAvatarFile(file)) {
                    input.value = '';
                    event.preventDefault();
                    return;
                }
                lockForm(profileForm, 'Đang lưu');
            });
        }
    })();
</script>
</body>
</html>
