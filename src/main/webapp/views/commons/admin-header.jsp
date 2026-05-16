<%--
  Created by IntelliJ IDEA.
  User: daizuongkk
  Date: 3/23/2026
  Time: 9:37 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="adminAccount" value="${sessionScope.account}"/>
<c:choose>
    <c:when test="${not empty adminAccount.firstName or not empty adminAccount.lastName}">
        <c:set var="adminDisplayName" value="${fn:trim(adminAccount.firstName)} ${fn:trim(adminAccount.lastName)}"/>
    </c:when>
    <c:otherwise>
        <c:set var="adminDisplayName" value="${adminAccount.username}"/>
    </c:otherwise>
</c:choose>
<c:url var="adminFallbackAvatar" value="/assets/img/avatar-1.jpg"/>
<nav id="topbar" class="navbar bg-black border-bottom fixed-top topbar px-3">
    <button id="toggleBtn" class="d-none d-lg-inline-flex btn btn-light btn-icon btn-sm ">
        <i class="ti ti-layout-sidebar-left-expand"></i>
    </button>

    <!-- MOBILE -->
    <button id="mobileBtn" class="btn btn-dark btn-icon btn-sm d-lg-none me-2">
        <i class="ti ti-layout-sidebar-left-expand"></i>
    </button>
    <div>
        <!-- Navbar nav -->
        <ul class="list-unstyled d-flex align-items-center mb-0 gap-1">
            <!-- Pages link -->

            <!-- Bell icon -->
<%--            <li>--%>
<%--                <a class="position-relative btn-icon btn-sm btn-light btn rounded-circle" data-bs-toggle="dropdown"--%>
<%--                   aria-expanded="false" href="#" role="button">--%>
<%--                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"--%>
<%--                         stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"--%>
<%--                         class="icon icon-tabler icons-tabler-outline icon-tabler-bell">--%>
<%--                        <path stroke="none" d="M0 0h24v24H0z" fill="none"/>--%>
<%--                        <path d="M10 5a2 2 0 1 1 4 0a7 7 0 0 1 4 6v3a4 4 0 0 0 2 3h-16a4 4 0 0 0 2 -3v-3a7 7 0 0 1 4 -6"/>--%>
<%--                        <path d="M9 17v1a3 3 0 0 0 6 0v-1"/>--%>
<%--                    </svg>--%>
<%--                    <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger mt-2 ms-n2">--%>
<%--              2--%>
<%--              <span class="visually-hidden">unread messages</span>--%>
<%--            </span>--%>
<%--                </a>--%>
<%--                <div class="dropdown-menu dropdown-menu-end dropdown-menu-md p-0">--%>
<%--                    <ul class="list-unstyled p-0 m-0">--%>
<%--                        <li class="p-3 border-bottom ">--%>
<%--                            <div class="d-flex gap-3">--%>
<%--                                <img src="assets/img/avatar-1.jpg" alt="" class="avatar avatar-sm rounded-circle"/>--%>
<%--                                <div class="flex-grow-1 small">--%>
<%--                                    <p class="mb-0">New order received</p>--%>
<%--                                    <p class="mb-1">Order #12345 has been placed</p>--%>
<%--                                    <div class="text-secondary">5 minutes ago</div>--%>
<%--                                </div>--%>
<%--                            </div>--%>
<%--                        </li>--%>
<%--                        <li class="p-3 border-bottom ">--%>
<%--                            <div class="d-flex gap-3">--%>
<%--                                <img src="assets/img/avatar-4.jpg" alt="" class="avatar avatar-sm rounded-circle"/>--%>
<%--                                <div class="flex-grow-1 small">--%>
<%--                                    <p class="mb-0">New user registered</p>--%>
<%--                                    <p class="mb-1">User @john_doe has signed up</p>--%>
<%--                                    <div class="text-secondary">30 minutes ago</div>--%>
<%--                                </div>--%>
<%--                            </div>--%>
<%--                        </li>--%>

<%--                        <li class="p-3 border-bottom">--%>
<%--                            <div class="d-flex gap-3">--%>
<%--                                <img src="assets/img/avatar-2.jpg" alt="" class="avatar avatar-sm rounded-circle"/>--%>
<%--                                <div class="flex-grow-1 small">--%>
<%--                                    <p class="mb-0">Payment confirmed</p>--%>
<%--                                    <p class="mb-1">Payment of $299 has been received</p>--%>
<%--                                    <div class="text-secondary">1 hour ago</div>--%>
<%--                                </div>--%>
<%--                            </div>--%>
<%--                        </li>--%>
<%--                        <li class="px-4 py-3 text-center">--%>
<%--                            <a href="#" class="text-primary ">View all notifications</a>--%>
<%--                        </li>--%>
<%--                    </ul>--%>
<%--                </div>--%>
<%--            </li>--%>
<%--            <!-- Dropdown -->--%>
            <li class="ms-3 dropdown">
                <a href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false"
                   class="d-inline-flex align-items-center">
                    <img src="${not empty adminAccount.avtUrl ? fn:escapeXml(adminAccount.avtUrl) : adminFallbackAvatar}"
                         alt="${fn:escapeXml(adminDisplayName)}"
                         class="avatar avatar-sm rounded-circle"/>
                </a>
                <div class="dropdown-menu dropdown-menu-end p-0" style="min-width: 240px;">
                    <div>
                        <div class="d-flex gap-3 align-items-center border-dashed border-bottom px-3 py-3">
                            <img src="${not empty adminAccount.avtUrl ? fn:escapeXml(adminAccount.avtUrl) : adminFallbackAvatar}"
                                 alt="${fn:escapeXml(adminDisplayName)}"
                                 class="avatar avatar-md rounded-circle"/>
                            <div class="min-w-0">
                                <h4 class="mb-0 small text-truncate">${fn:escapeXml(adminDisplayName)}</h4>
                                <p class="mb-0 small text-secondary text-truncate">${fn:escapeXml(adminAccount.email)}</p>
                                <span class="badge bg-primary mt-1">${fn:escapeXml(adminAccount.role)}</span>
                            </div>
                        </div>
                        <div class="p-2 small">
                            <a href="profile" class="dropdown-item d-flex align-items-center gap-2 rounded-2">
                                <i class="ti ti-user"></i>
                                <span>Thông tin tài khoản</span>
                            </a>
                            <div class="dropdown-divider"></div>
                            <a href="logout" class="dropdown-item d-flex align-items-center gap-2 rounded-2 text-danger">
                                <i class="ti ti-logout"></i>
                                <span>Đăng xuất</span>
                            </a>
                        </div>

                    </div>
                </div>
            </li>
        </ul>
    </div>

</nav>

<div id="admin-confirm-modal" class="modal fade" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-sm">
        <div class="modal-content">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title">Xác nhận</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
            </div>
            <div class="modal-body">
                <p id="admin-confirm-message" class="mb-0">Bạn chắc chắn muốn thực hiện thao tác này?</p>
            </div>
            <div class="modal-footer border-0 pt-0">
                <button id="admin-confirm-cancel" type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy</button>
                <button id="admin-confirm-ok" type="button" class="btn btn-danger">Xác nhận</button>
            </div>
        </div>
    </div>
</div>

<script>
    (function () {
        function showAdminConfirm(message, options) {
            options = options || {};
            return new Promise(function (resolve) {
                var modalEl = document.getElementById('admin-confirm-modal');
                var messageEl = document.getElementById('admin-confirm-message');
                var okBtn = document.getElementById('admin-confirm-ok');
                var cancelBtn = document.getElementById('admin-confirm-cancel');
                var closeBtn = modalEl ? modalEl.querySelector('.btn-close') : null;
                var resolved = false;

                if (!modalEl || !messageEl || !okBtn) {
                    resolve(false);
                    return;
                }

                messageEl.textContent = message || 'Bạn chắc chắn muốn thực hiện thao tác này?';
                okBtn.textContent = options.confirmText || 'Xác nhận';
                cancelBtn.textContent = options.cancelText || 'Hủy';
                okBtn.classList.toggle('btn-danger', (options.type || 'danger') === 'danger');
                okBtn.classList.toggle('btn-primary', (options.type || 'danger') !== 'danger');

                var modal = typeof bootstrap !== 'undefined' && bootstrap.Modal
                    ? bootstrap.Modal.getOrCreateInstance(modalEl)
                    : null;
                var backdrop = null;

                function finish(value) {
                    if (resolved) {
                        return;
                    }
                    resolved = true;
                    okBtn.removeEventListener('click', onConfirm);
                    cancelBtn.removeEventListener('click', onCancel);
                    if (closeBtn) {
                        closeBtn.removeEventListener('click', onCancel);
                    }
                    modalEl.removeEventListener('hidden.bs.modal', onHidden);
                    modalEl.removeEventListener('click', onBackdropClick);
                    document.removeEventListener('keydown', onKeydown);
                    if (!modal) {
                        closeManualModal();
                    }
                    resolve(value);
                }

                function onConfirm() {
                    finish(true);
                    if (modal) {
                        modal.hide();
                    }
                }

                function onCancel(event) {
                    if (event) {
                        event.preventDefault();
                    }
                    finish(false);
                    if (modal) {
                        modal.hide();
                    }
                }

                function onHidden() {
                    finish(false);
                }

                function onBackdropClick(event) {
                    if (event.target === modalEl) {
                        finish(false);
                    }
                }

                function onKeydown(event) {
                    if (event.key === 'Escape') {
                        finish(false);
                    }
                }

                function openManualModal() {
                    backdrop = document.createElement('div');
                    backdrop.className = 'modal-backdrop fade show';
                    document.body.appendChild(backdrop);
                    document.body.classList.add('modal-open');
                    modalEl.style.display = 'block';
                    modalEl.classList.add('show');
                    modalEl.removeAttribute('aria-hidden');
                    modalEl.setAttribute('aria-modal', 'true');
                    modalEl.setAttribute('role', 'dialog');
                    okBtn.focus();
                }

                function closeManualModal() {
                    modalEl.classList.remove('show');
                    modalEl.style.display = 'none';
                    modalEl.setAttribute('aria-hidden', 'true');
                    modalEl.removeAttribute('aria-modal');
                    modalEl.removeAttribute('role');
                    document.body.classList.remove('modal-open');
                    if (backdrop) {
                        backdrop.remove();
                        backdrop = null;
                    }
                }

                okBtn.addEventListener('click', onConfirm);
                cancelBtn.addEventListener('click', onCancel);
                if (closeBtn) {
                    closeBtn.addEventListener('click', onCancel);
                }
                modalEl.addEventListener('hidden.bs.modal', onHidden);
                modalEl.addEventListener('click', onBackdropClick);
                document.addEventListener('keydown', onKeydown);
                if (modal) {
                    modal.show();
                } else {
                    openManualModal();
                }
            });
        }

        window.showAdminConfirm = showAdminConfirm;

        document.addEventListener('submit', function (event) {
            var form = event.target.closest('form[data-confirm-message]');
            if (!form) {
                return;
            }
            if (form.dataset.confirmed === 'true') {
                form.dataset.confirmed = 'false';
                return;
            }

            event.preventDefault();
            showAdminConfirm(form.dataset.confirmMessage, {
                confirmText: form.dataset.confirmText || 'Xác nhận'
            }).then(function (confirmed) {
                if (confirmed) {
                    form.dataset.confirmed = 'true';
                    form.submit();
                }
            });
        }, true);

        document.addEventListener('click', function (event) {
            var target = event.target.closest('[data-confirm-click]');
            if (!target || target.dataset.confirmed === 'true') {
                return;
            }

            event.preventDefault();
            event.stopPropagation();
            showAdminConfirm(target.dataset.confirmClick, {
                confirmText: target.dataset.confirmText || 'Xác nhận'
            }).then(function (confirmed) {
                if (!confirmed) {
                    return;
                }
                target.dataset.confirmed = 'true';
                target.click();
                target.dataset.confirmed = 'false';
            });
        }, true);
    })();
</script>
