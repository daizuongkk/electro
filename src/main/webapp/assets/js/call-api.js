(function($) {
    "use strict";

    $(document).ready(function() {
        fetchCart();
    })

    const formatCurrency = (value) => {
        return value.toLocaleString('vi-VN') + "₫";
    };

    const fetchCart = () => {
        $.ajax({
            url: `api/carts`,
            method: "GET",
            dataType: "json",
            success: function (res, textStatus, xhr) {

                const cart = res;
                if (!cart || cart.length === 0) {
                    $(".cart-dropdown .cart-list")
                        .html("<p class='text-center'>Giỏ hàng trống</p>");
                    return;
                }

                $('.dropdown .dropdown-toggle .qty').text(cart.length);

                $(".cart-dropdown .cart-summary").html(`
                    <p>${cart.length} sản phẩm</p>
                    <h5>${formatCurrency(cart.reduce((total, item) => total + item.product.price * item.quantity, 0))}</h5>
                `);


                let html = "";

                cart.forEach(item => {
                    html += `
                            <div class="product-widget" >
                                <div class="product-img" onclick="location.href='/products?id=${item.id}'">
                                    <img src="${item.product.imageUrl[0]}" alt="">
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

                $(".cart-dropdown .cart-list").html(html);
            },
            error: function (xhr, status, error) {
                if (xhr.status === 401) {
                    $(".cart-dropdown .cart-list")
                        .html("<p class='text-center'>Đăng nhập để bắt đầu mua sắm!</p>");
                }
            }
        });
    };


    $(".header-ctn .dropdown").on("shown.bs.dropdown", function () {
        fetchCart();
    });


    // expose ra ngoài nếu cần
    window.addToCart = function(productId) {
        $.ajax({
            url: `api/carts/` + productId,
            method: 'POST',
            success: function (res) {
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


    window.deleteCartItem = (productIds) => {
        return $.ajax({
            url: `api/carts/` + productIds,
            method: 'DELETE',
            success: function (res) {
                alert("Xóa sản phẩm thành công");
                fetchCart();
            },
            error: function () {
                alert("Có lỗi xảy ra");
            }
        });
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