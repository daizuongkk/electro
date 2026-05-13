(function($) {
    "use strict";

    $(document).ready(function() {
        fetchCart();
        fetchWishlist();
    })

    const formatCurrency = (value) => {
        return value.toLocaleString('vi-VN') + "₫";
    };

    const fallbackProductImage = "assets/img/fallback_product_img.jpg";
    const wishlistState = new Set();

    const getFirstImage = (item) => {
        if (item && item.imageUrl && item.imageUrl.length > 0) {
            return item.imageUrl[0];
        }

        return fallbackProductImage;
    };

    const syncWishlistButtons = () => {
        $(".add-to-wishlist[data-product-id]").each(function () {
            const $button = $(this);
            const productId = Number($button.data("product-id"));
            const isActive = wishlistState.has(productId);
            const $icon = $button.find("i").first();

            $button.toggleClass("is-active", isActive);
            $icon.toggleClass("fa-heart", isActive);
            $icon.toggleClass("fa-heart-o", !isActive);
        });
    };

    const fetchCart = () => {
        const $cartList = $(".cart-dropdown .cart-list");
        const $cartSummary = $(".cart-dropdown .cart-summary");
        const $cartQty = $(".cart-qty");

        $.ajax({
            url: `api/carts`,
            method: "GET",
            dataType: "json",
            success: function (res) {

                const cart = res;
                if (!cart || cart.length === 0) {
                    $cartList
                        .html("<p class='text-center'>Giỏ hàng trống</p>");
                    $cartSummary.empty();
                    $cartQty.text(0);
                    return;
                }

                $cartQty.text(cart.length);

                $cartSummary.html(`
                    <p>${cart.length} sản phẩm</p>
                    <h5>${formatCurrency(cart.reduce((total, item) => total + item.product.price * item.quantity, 0))}</h5>
                `);


                let html = "";

                cart.forEach(item => {
                    html += `
                            <div class="product-widget" >
                                <div class="product-img" onclick="location.href='products?id=${item.product.id}'">
                                    <img src="${getFirstImage(item.product)}" alt="">
                                </div>
                                <div class="product-body">
                                    <h3 class="product-name">${item.product.name}</h3>
                                    <h4 class="product-price">
                                        <span class="qty">${item.quantity}</span>
                                        ${formatCurrency(item.product.price)}
                                    </h4>
                                </div>
                                <button class="delete" onclick="deleteCartItem(${item.product.id})"><i class="fa fa-close"></i></button>
                            </div>
                        
                    `;
                });

                $cartList.html(html);
            },
            error: function (xhr) {
                if (xhr.status === 401) {
                    $cartList
                        .html("<p class='text-center'>Đăng nhập để bắt đầu mua sắm!</p>");
                    $cartSummary.empty();
                    $cartQty.text(0);
                }
            }
        });
    };

    const fetchWishlist = () => {
        const $wishlistList = $(".wishlist-dropdown .wishlist-list");
        const $wishlistSummary = $(".wishlist-dropdown .wishlist-summary");
        const $wishlistQty = $(".wishlist-qty");

        $.ajax({
            url: `api/whishlist`,
            method: "GET",
            dataType: "json",
            success: function (res) {
                const wishlist = res || [];
                wishlistState.clear();

                wishlist.forEach(item => wishlistState.add(Number(item.id)));
                $wishlistQty.text(wishlist.length);

                if (!wishlist.length) {
                    $wishlistList
                        .html("<p class='text-center'>Danh sách yêu thích trống</p>");
                    $wishlistSummary.empty();
                    syncWishlistButtons();
                    return;
                }

                let html = "";
                wishlist.forEach(item => {
                    html += `
                        <div class="product-widget">
                            <div class="product-img" onclick="location.href='products?id=${item.id}'">
                                <img src="${getFirstImage(item)}" alt="${item.name}">
                            </div>
                            <div class="product-body">
                                <h3 class="product-name"><a href="products?id=${item.id}">${item.name}</a></h3>
                                <h4 class="product-price">${formatCurrency(item.price || 0)}</h4>
                            </div>
                            <button class="delete" type="button" onclick="deleteWishlistItem(${item.id})"><i class="fa fa-close"></i></button>
                        </div>
                    `;
                });

                $wishlistList.html(html);
                $wishlistSummary.html(`
                    <p>${wishlist.length} sản phẩm</p>
                `);
                syncWishlistButtons();
            },
            error: function (xhr) {
                if (xhr.status === 401) {
                    wishlistState.clear();
                    $wishlistList
                        .html("<p class='text-center'>Đăng nhập để quản lý danh sách yêu thích!</p>");
                    $wishlistSummary.empty();
                    $wishlistQty.text(0);
                    syncWishlistButtons();
                }
            }
        });
    };


    $(".header-ctn .dropdown").on("shown.bs.dropdown", function () {
        if ($(this).find(".cart-dropdown").length) {
            fetchCart();
        }

        if ($(this).find(".wishlist-dropdown").length) {
            fetchWishlist();
        }
    });

    $(document).on("click", ".add-to-wishlist", function (e) {
        e.preventDefault();
        e.stopPropagation();

        const productId = $(this).data("product-id");
        if (!productId) {
            return;
        }

        $.ajax({
            url: `api/whishlist/` + productId,
            method: "POST",
            dataType: "json",
            success: function () {
                fetchWishlist();
            },
            error: function (xhr) {
                if (xhr.status === 401) {
                    if (confirm("Bạn cần đăng nhập để quản lý danh sách yêu thích. Bạn có muốn đăng nhập ngay bây giờ?")) {
                        window.location.href = "login";
                    }
                    return;
                }

                alert("Lỗi xảy ra khi cập nhật danh sách yêu thích");
            }
        });
    });


    // expose ra ngoài nếu cần
    window.addToCart = function(productId, quantity) {


        $.ajax({
            url: `api/carts/` + productId + `?qty=${quantity}`,
            method: 'POST',
            success: function () {
                fetchCart();
                alert("Đã thêm vào giỏ hàng")
            },
            error: function (res) {
                if (res.status === 401) {
                    if (confirm("Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng. Bạn có muốn đăng nhập ngay bây giờ?")) {
                        window.location.href = "login";
                    }
                    return;
                }
                alert("Lỗi xảy ra khi thêm sản phẩm");
                console.error("Failed to add product to cart");
            }
        });
    };

    window.deleteWishlistItem = (productId) => {
        if (confirm("Chắc chắn xóa khỏi danh sách yêu thích?")) {
            return $.ajax({
                url: `api/whishlist/` + productId,
                method: 'DELETE',
                dataType: 'json',
                success: function () {
                    fetchWishlist();
                },
                error: function () {
                    alert("Có lỗi xảy ra");
                }
            });
        }
    };


    window.deleteCartItem = (productIds) => {
        if (confirm("Chắc chắn xóa?")) {
            return $.ajax({
                url: `api/carts/` + productIds,
                method: 'DELETE',
                success: function () {
                    // alert("Xóa sản phẩm thành công");
                    fetchCart();
                },
                error: function () {
                    alert("Có lỗi xảy ra");
                }
            });
        }

    }




    $("#delete-carts").click(function () {

        const productIds =  $("tbody .cart-item-check:checked")
            .map(function () {
                return $(this).val();
            })
            .get()
        if (productIds.length === 0) {
            alert("Chưa chọn sản phẩm nào")
            return;
        }

        deleteCartItem(productIds.join(",")).done(function () {
            window.location.reload();
        })

    })

})(jQuery);