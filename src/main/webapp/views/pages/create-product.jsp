<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<base href="${pageContext.request.contextPath}/">

<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Admin - Thêm Sản Phẩm"/>
    <%@include file="../commons/admin-head.jsp" %>
    <style>
        .admin-product-images {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
        }

        .admin-product-image-item,
        .admin-product-image-add {
            width: 86px;
            height: 86px;
            border-radius: 8px;
            overflow: hidden;
            position: relative;
            border: 1px solid #dee2e6;
            background: #fff;
            flex: 0 0 auto;
        }

        .admin-product-image-item img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
        }

        .admin-product-image-remove {
            position: absolute;
            top: 4px;
            right: 4px;
            width: 24px;
            height: 24px;
            border: 0;
            border-radius: 50%;
            background: rgba(220, 53, 69, 0.92);
            color: #fff;
            line-height: 24px;
            padding: 0;
        }

        .admin-product-image-add {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            border-style: dashed;
            color: #6c757d;
            cursor: pointer;
            font-size: 28px;
        }

        .admin-product-image-item.is-removed {
            display: none;
        }
    </style>
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
                        <h1 class="fs-3 mb-1">${editMode ? 'Chỉnh Sửa Sản Phẩm' : 'Thêm Sản Phẩm'}</h1>
                        <p class="mb-0">${editMode ? 'Cập nhật thông tin sản phẩm trong kho' : 'Tạo sản phẩm mới trong kho hàng'}</p>
                    </div>
                    <a href="admin/products" class="btn btn-primary">Danh Sách Sản Phẩm</a>
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

                        <form method="post" action="admin/products" enctype="multipart/form-data">
                            <c:if test="${editMode}">
                                <input type="hidden" name="id" value="${productForm.id}">
                            </c:if>

                            <div class="row">
                                <div class="col-md-8 mb-3">
                                    <label for="productName" class="form-label">Tên sản phẩm</label>
                                    <input type="text" class="form-control" id="productName" name="name"
                                           placeholder="Nhập tên sản phẩm" value="${fn:escapeXml(productForm.name)}" required>
                                </div>
                                <div class="col-md-4 mb-3">
                                    <label for="productPrice" class="form-label">Giá</label>
                                    <input type="number" class="form-control" id="productPrice" name="price"
                                           placeholder="0" min="0" step="1000" value="${productForm.price}" required>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-4 mb-3">
                                    <label for="productCategory" class="form-label">Danh mục</label>
                                    <select class="form-select" id="productCategory" name="category" required>
                                        <option value="">Chọn danh mục</option>
                                        <c:forEach var="category" items="${categories}">
                                            <option value="${category.key}" ${productForm.category == category.key ? 'selected' : ''}>${category.value}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-4 mb-3">
                                    <label for="productBrand" class="form-label">Hãng</label>
                                    <select class="form-select" id="productBrand" name="brand" required>
                                        <option value="">Chọn hãng</option>
                                        <c:forEach var="brand" items="${brands}">
                                            <option value="${brand.key}" ${productForm.brand == brand.key ? 'selected' : ''}>${brand.value}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-2 mb-3">
                                    <label for="productStock" class="form-label">Tồn kho</label>
                                    <input type="number" class="form-control" id="productStock" name="quantity"
                                           min="0" value="${editMode ? productForm.quantity : 0}" required>
                                </div>
                                <div class="col-md-2 mb-3">
                                    <label for="productPromotion" class="form-label">KM (%)</label>
                                    <input type="number" class="form-control" id="productPromotion" name="promotion"
                                           min="0" max="100" value="${editMode ? productForm.promotion : 0}">
                                </div>
                            </div>

                            <div class="mb-3">
                                <label for="productSummary" class="form-label">Tóm tắt</label>
                                <input type="text" class="form-control" id="productSummary" name="summary"
                                       placeholder="Tóm tắt ngắn hiển thị ngoài danh sách" value="${fn:escapeXml(productForm.summary)}">
                            </div>

                            <div class="mb-3">
                                <label for="productDescription" class="form-label">Mô tả</label>
                                <textarea class="form-control" id="productDescription" name="description" rows="3"
                                          placeholder="Mô tả sản phẩm">${fn:escapeXml(productForm.description)}</textarea>
                            </div>

                            <div class="mb-3">
                                <label for="productDetail" class="form-label">Thông số chi tiết</label>
                                <textarea class="form-control" id="productDetail" name="detail" rows="5"
                                          placeholder="- RAM: ...&#10;- Màn hình: ...">${fn:escapeXml(productForm.detail)}</textarea>
                            </div>

                            <div class="mb-3">
                                <label class="form-label">Ảnh sản phẩm</label>
                                <div id="productImageList" class="admin-product-images">
                                    <c:forEach var="image" items="${productImages}">
                                        <div class="admin-product-image-item" data-image-id="${image.id}">
                                            <img src="${fn:escapeXml(image.url)}" alt="Ảnh sản phẩm">
                                            <button type="button" class="admin-product-image-remove" aria-label="Xóa ảnh">&times;</button>
                                        </div>
                                    </c:forEach>
                                    <label class="admin-product-image-add" for="productImageFiles" title="Thêm ảnh">
                                        <i class="ti ti-plus"></i>
                                    </label>
                                </div>
                                <input type="file" id="productImageFiles" name="imageFiles" accept="image/*" multiple hidden>
                                <small class="text-secondary d-block mt-2">
                                    Bấm dấu + để chọn ảnh từ thiết bị. Ảnh mới sẽ upload lên Cloudinary khi lưu.
                                </small>
                            </div>

                            <div class="d-flex gap-2">
                                <button type="submit" class="btn btn-primary">${editMode ? 'Lưu Thay Đổi' : 'Thêm Sản Phẩm'}</button>
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
        var fileInput = document.getElementById('productImageFiles');
        var imageList = document.getElementById('productImageList');
        if (!fileInput || !imageList) {
            return;
        }

        var selectedFiles = new DataTransfer();
        var addButton = imageList.querySelector('.admin-product-image-add');

        imageList.addEventListener('click', function (event) {
            var removeButton = event.target.closest('.admin-product-image-remove');
            if (!removeButton) {
                return;
            }

            var item = removeButton.closest('.admin-product-image-item');
            if (!item) {
                return;
            }

            var imageId = item.getAttribute('data-image-id');
            if (imageId) {
                var hidden = document.createElement('input');
                hidden.type = 'hidden';
                hidden.name = 'deletedImageId';
                hidden.value = imageId;
                imageList.appendChild(hidden);
                item.classList.add('is-removed');
                return;
            }

            var fileIndex = parseInt(item.getAttribute('data-file-index'), 10);
            if (!isNaN(fileIndex)) {
                removeSelectedFile(fileIndex);
                renderSelectedFiles();
            }
        });

        fileInput.addEventListener('change', function () {
            Array.from(fileInput.files || []).forEach(function (file) {
                selectedFiles.items.add(file);
            });
            fileInput.files = selectedFiles.files;
            renderSelectedFiles();
        });

        function removeSelectedFile(index) {
            var nextFiles = new DataTransfer();
            Array.from(selectedFiles.files).forEach(function (file, currentIndex) {
                if (currentIndex !== index) {
                    nextFiles.items.add(file);
                }
            });
            selectedFiles = nextFiles;
            fileInput.files = selectedFiles.files;
        }

        function renderSelectedFiles() {
            imageList.querySelectorAll('.admin-product-image-item[data-new-image="true"]').forEach(function (item) {
                item.remove();
            });

            Array.from(selectedFiles.files).forEach(function (file, index) {
                var item = document.createElement('div');
                item.className = 'admin-product-image-item';
                item.setAttribute('data-new-image', 'true');
                item.setAttribute('data-file-index', String(index));

                var img = document.createElement('img');
                img.alt = file.name;
                img.src = URL.createObjectURL(file);

                var remove = document.createElement('button');
                remove.type = 'button';
                remove.className = 'admin-product-image-remove';
                remove.setAttribute('aria-label', 'Xóa ảnh');
                remove.innerHTML = '&times;';

                item.appendChild(img);
                item.appendChild(remove);
                imageList.insertBefore(item, addButton);
            });
        }
    })();
</script>
</body>
</html>
