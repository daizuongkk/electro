<%--
  Created by IntelliJ IDEA.
  User: daizuongkk
  Date: 3/21/2026
  Time: 11:56 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<script src="assets/js/jquery.min.js"></script>
<script src="assets/js/bootstrap.min.js"></script>
<script src="assets/js/slick.min.js"></script>
<script src="assets/js/nouislider.min.js"></script>
<script src="assets/js/jquery.zoom.min.js"></script>
<script src="assets/js/main.js"></script>
<script src="assets/js/login.js"></script>
<script src="assets/js/call-api.js"></script>

<div id="app-popup-modal" class="modal fade app-popup-modal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-sm">
        <div class="modal-content">
            <button type="button" class="app-popup-close" data-dismiss="modal" aria-label="Đóng">
                <i class="fa fa-close"></i>
            </button>
            <div id="app-popup-icon" class="app-popup-icon">
                <i class="fa fa-info"></i>
            </div>
            <h4 id="app-popup-title" class="app-popup-title">Thông báo</h4>
            <p id="app-popup-message" class="app-popup-message"></p>
            <button type="button" class="primary-btn app-popup-button" data-dismiss="modal">Đã hiểu</button>
        </div>
    </div>
</div>

<div id="app-confirm-modal" class="modal fade app-popup-modal app-confirm-modal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-sm">
        <div class="modal-content">
            <button type="button" class="app-popup-close" data-dismiss="modal" aria-label="Đóng">
                <i class="fa fa-close"></i>
            </button>
            <div class="app-popup-icon warning">
                <i class="fa fa-question"></i>
            </div>
            <h4 id="app-confirm-title" class="app-popup-title">Xác nhận</h4>
            <p id="app-confirm-message" class="app-popup-message"></p>
            <div class="app-confirm-actions">
                <button id="app-confirm-cancel" type="button" class="app-confirm-cancel" data-dismiss="modal">Hủy</button>
                <button id="app-confirm-ok" type="button" class="primary-btn app-popup-button">Xác nhận</button>
            </div>
        </div>
    </div>
</div>

<script>
    (function () {
        function normalizeType(type) {
            return type === 'success' || type === 'danger' || type === 'warning' ? type : 'info';
        }

        function popupTitle(type) {
            if (type === 'success') return 'Thành công';
            if (type === 'danger') return 'Có lỗi xảy ra';
            if (type === 'warning') return 'Cần kiểm tra';
            return 'Thông báo';
        }

        function popupIcon(type) {
            if (type === 'success') return 'fa-check';
            if (type === 'danger') return 'fa-exclamation';
            if (type === 'warning') return 'fa-warning';
            return 'fa-info';
        }

        window.showElectroPopup = function (message, type, title) {
            type = normalizeType(type);
            var modal = $('#app-popup-modal');
            var icon = $('#app-popup-icon');
            icon.removeClass('success danger warning info').addClass(type);
            icon.find('i').attr('class', 'fa ' + popupIcon(type));
            $('#app-popup-title').text(title || popupTitle(type));
            $('#app-popup-message').text(message || '');
            modal.modal('show');
        };

        window.alert = function (message) {
            window.showElectroPopup(message, 'info');
        };

        window.showElectroConfirm = function (message, options) {
            options = options || {};
            return new Promise(function (resolve) {
                var modal = $('#app-confirm-modal');
                var resolved = false;

                $('#app-confirm-title').text(options.title || 'Xác nhận');
                $('#app-confirm-message').text(message || '');
                $('#app-confirm-ok').text(options.confirmText || 'Xác nhận');
                $('#app-confirm-cancel').text(options.cancelText || 'Hủy');

                function finish(value) {
                    if (resolved) {
                        return;
                    }
                    resolved = true;
                    modal.off('hidden.bs.modal', onHidden);
                    $('#app-confirm-ok').off('click', onConfirm);
                    resolve(value);
                }

                function onConfirm() {
                    finish(true);
                    modal.modal('hide');
                }

                function onHidden() {
                    finish(false);
                }

                $('#app-confirm-ok').on('click', onConfirm);
                modal.on('hidden.bs.modal', onHidden);
                modal.modal('show');
            });
        };

        $(document).on('submit', 'form[data-confirm-message]', function (event) {
            var form = this;
            if (form.dataset.confirmed === 'true') {
                form.dataset.confirmed = 'false';
                return;
            }

            event.preventDefault();
            window.showElectroConfirm(form.dataset.confirmMessage, {
                confirmText: form.dataset.confirmText || 'Xác nhận',
                cancelText: form.dataset.cancelText || 'Hủy'
            }).then(function (confirmed) {
                if (confirmed) {
                    form.dataset.confirmed = 'true';
                    form.submit();
                }
            });
        });

        $(function () {
            $('.js-popup-message').each(function () {
                var el = $(this);
                window.showElectroPopup(el.data('message') || el.text(), el.data('type'), el.data('title'));
            });
        });
    })();
</script>
