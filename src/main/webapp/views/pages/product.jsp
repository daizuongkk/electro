<%@ page import="java.util.Map" %>
<%@ page import="com.daizuongkk.web.dto.response.ProductResponse" %>
<%@ page import="java.util.HashMap" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:url var="fallbackProductImage" value="/assets/img/fallback_product_img.jpg"/>

<fmt:setLocale value="vi_VN"/>
<c:set var="reviewScore" value="${product.reviewScore}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Electro - ${product.name}"/>
    <%@ include file="../commons/head.jsp" %>
</head>
<body>
<!-- HEADER -->
<%@ include file="../commons/header.jsp" %>

<!-- /HEADER -->

<!-- NAVIGATION -->
<jsp:include page="../commons/navigation.jsp"/>

<!-- /NAVIGATION -->

<!-- BREADCRUMB -->
<div id="breadcrumb" class="section">
    <!-- container -->
    <div class="container">
        <!-- row -->
        <div class="row">
            <div class="col-md-12">
                <ul class="breadcrumb-tree">
                    <li><a href="#">Home</a></li>

                    <li><a href="#">${product.category}</a></li>
                    <li class="active">${product.name}</li>
                </ul>
            </div>
        </div>
        <!-- /row -->
    </div>
    <!-- /container -->
</div>
<!-- /BREADCRUMB -->

<!-- SECTION -->
<div class="section">
    <!-- container -->
    <div class="container">
        <!-- row -->
        <div class="row">
            <!-- Product main img -->
            <div class="col-md-5 col-md-push-2">
                <div id="product-main-img">
                    <c:forEach items="${product.imageUrl}" var="imageUrl">

                        <div class="product-preview">
                            <img src="${not empty imageUrl ? imageUrl : fallbackProductImage}" alt="">
                        </div>
                    </c:forEach>


                </div>
            </div>
            <!-- /Product main img -->

            <!-- Product thumb imgs -->
            <div class="col-md-2  col-md-pull-5">
                <div id="product-imgs">
                    <c:forEach items="${product.imageUrl}" var="imageUrl">

                        <div class="product-preview">
                            <img src="${not empty imageUrl ? imageUrl : fallbackProductImage}" alt="">
                        </div>
                    </c:forEach>
                </div>
            </div>
            <!-- /Product thumb imgs -->

            <!-- Product details -->
            <div class="col-md-5">
                <div class="product-details">
                    <h2 class="product-name">${product.name}</h2>
                    <div>
                        <div class="product-rating">
                            <c:choose>
                                <c:when test="${reviewScore != 0}">
                                    <c:forEach begin="1" end="${reviewScore}" var="i">
                                        <i class="fa fa-star"></i>
                                    </c:forEach>
                                    <c:forEach begin="${reviewScore + 1}" end="5" var="i">
                                        <i class="fa fa-star-o"></i>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach begin="1" end="5" var="i">
                                        <i class="fa fa-star-o"></i>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <a class="review-link" href="#">${totalRv} Đánh Giá</a>
                    </div>
                    <div>
                        <h3 class="product-price">
                            <c:choose>
                                <c:when test="${product.price != null}">
                                    <c:set var="newPrice"
                                           value="${product.price - (product.price * product.promotion / 100)}"/>

                                    <fmt:formatNumber
                                            value="${newPrice}"
                                            type="currency"
                                            currencySymbol="₫"/>
                                </c:when>
                                <c:otherwise>Liên Hệ</c:otherwise>
                            </c:choose>
                            <c:if test="${product.promotion != null && product.promotion > 0}">
                                <del class="product-old-price">
                                    <fmt:formatNumber
                                            value="${product.price}"
                                            type="currency"
                                            currencySymbol="₫"/></del>
                            </c:if>
                        </h3>
                        <span class="product-available">In Stock</span>
                    </div>
                    <p>
                        <c:if test="${product.summary != null}">
                            <c:out value="${product.summary}" escapeXml="false"/>
                        </c:if>
                    </p>

                    <%--							<div class="product-options">--%>
                    <%--								<label>--%>
                    <%--									Size--%>
                    <%--									<select class="input-select">--%>
                    <%--										<option value="0">X</option>--%>
                    <%--									</select>--%>
                    <%--								</label>--%>
                    <%--								<label>--%>
                    <%--									Color--%>
                    <%--									<select class="input-select">--%>
                    <%--										<option value="0">Red</option>--%>
                    <%--									</select>--%>
                    <%--								</label>--%>
                    <%--							</div>--%>

                    <div class="add-to-cart">
                        <div class="qty-label">
                            Qty
                            <div class="input-number">
                                <!-- Gán ID động chứa mã sản phẩm -->
                                <input type="number" id="qty-${product.id}" value="1">
                                <span class="qty-up">+</span>
                                <span class="qty-down">-</span>
                            </div>
                        </div>
                        <!-- Tìm trực tiếp bằng ID vừa gán -->
                        <button class="add-to-cart-btn" onclick="addToCart(${product.id}, parseInt(document.getElementById('qty-${product.id}').value))">
                            <i class="fa fa-shopping-cart"></i>thêm vào giỏ
                        </button>
                    </div>

                    <c:set var="isWishlisted" value="false"/>
                    <c:forEach var="wishlistItem" items="${sessionScope.wishlist}">
                        <c:if test="${wishlistItem.id == product.id}">
                            <c:set var="isWishlisted" value="true"/>
                        </c:if>
                    </c:forEach>

                    <ul class="product-btns">
                        <li>
                            <a href="#" class="add-to-wishlist ${isWishlisted ? 'is-active' : ''}" data-product-id="${product.id}">
                                <i class="fa ${isWishlisted ? 'fa-heart' : 'fa-heart-o'}"></i> thêm yêu thích
                            </a>
                        </li>
                        <li><a href="#"><i class="fa fa-exchange"></i> thêm so sánh</a></li>
                    </ul>

                    <ul class="product-links">
                        <li>Phân Loại:</li>
                        <c:set var="categoryCode" value=""/>

                        <c:forEach var="entry" items="${categories}">
                            <c:if test="${entry.value == product.category}">
                                <c:set var="categoryCode" value="${entry.key}"/>
                            </c:if>
                        </c:forEach>

                        <a href="shop?category=${categoryCode}">
                            ${product.category}
                        </a>
                    </ul>

                    <ul class="product-links">
                        <li>Share:</li>
                        <li><a href="facebook.com/daizuongkk"><i class="fa fa-facebook"></i></a></li>
                        <li><a href="#"><i class="fa fa-twitter"></i></a></li>
                        <li><a href="#"><i class="fa fa-google-plus"></i></a></li>
                        <li><a href="#"><i class="fa fa-envelope"></i></a></li>
                    </ul>

                </div>
            </div>
            <!-- /Product details -->

            <!-- Product tab -->
            <div class="col-md-12">
                <div id="product-tab">
                    <!-- product tab nav -->
                    <ul class="tab-nav">
                        <li class="active"><a data-toggle="tab" href="#tab1">Mô tả</a></li>
                        <li><a data-toggle="tab" href="#tab2">Chi tiết</a></li>
                        <li><a data-toggle="tab" href="#tab3">Đánh giá (${totalRv})</a></li>
                    </ul>
                    <!-- /product tab nav -->

                    <!-- product tab content -->
                    <div class="tab-content">
                        <!-- tab1  -->
                        <div id="tab1" class="tab-pane fade in active">
                            <div class="row">
                                <div class="col-md-12">
                                    <p><c:if test="${product.description != null}">
                                        <c:out value="${product.description}" escapeXml="false"/>
                                    </c:if></p>
                                </div>
                            </div>
                        </div>
                        <!-- /tab1  -->

                        <!-- tab2  -->
                        <div id="tab2" class="tab-pane fade in">
                            <div class="row">
                                <div class="col-md-12">


                                    <table class="table table-hover">
                                        <tbody>
                                        <%
                                            ProductResponse product = (ProductResponse) request.getAttribute("product");

                                            Map<String, String> mapDetails = new HashMap<>();
                                            String details = product.getDetail();

                                            if (details != null) {
                                                String[] lines = details.split("-");
                                                for (String line : lines) {
                                                    String[] parts = line.split(":", 2);
                                                    if (parts.length == 2) {
                                                        String key = parts[0].trim();
                                                        String value = parts[1].trim();
                                                        mapDetails.put(key, value);
                                                    }
                                                }
                                            }

                                            request.setAttribute("mapDetails", mapDetails);
                                        %>

                                        <c:forEach items="${mapDetails}" var="entry">
                                            <tr>
                                                <th scope="row"><c:out value="${entry.key}"/></th>
                                                <td><c:out value="${entry.value}"/></td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>

                                    <p>
                                        <%--                                        <c:if test="${product.detail != null}">--%>
                                        <%--                                            <c:out value="${product.detail}" escapeXml="false"/>--%>
                                        <%--                                        </c:if>--%>
                                    </p>
                                </div>
                            </div>
                        </div>
                        <!-- /tab2  -->

                        <!-- tab3  -->
                        <div id="tab3" class="tab-pane fade in">
                            <div class="row">
                                <!-- Rating -->
                                <div class="col-md-3">
                                    <div id="rating">
                                        <div class="rating-avg">
                                            <span><fmt:formatNumber value="${reviewScore}" pattern="0.#"/> </span>
                                            <div class="rating-stars">
                                                <c:choose>
                                                    <c:when test="${reviewScore != 0}">
                                                        <c:forEach begin="1" end="${reviewScore}" var="i">
                                                            <i class="fa fa-star"></i>
                                                        </c:forEach>
                                                        <c:forEach begin="${reviewScore + 1}" end="5" var="i">
                                                            <i class="fa fa-star-o"></i>
                                                        </c:forEach>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:forEach begin="1" end="5" var="i">
                                                            <i class="fa fa-star-o"></i>
                                                        </c:forEach>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>
                                        <ul class="rating">
                                            <li>
                                                <div class="rating-stars">
                                                    <i class="fa fa-star"></i>
                                                    <i class="fa fa-star"></i>
                                                    <i class="fa fa-star"></i>
                                                    <i class="fa fa-star"></i>
                                                    <i class="fa fa-star"></i>
                                                </div>
                                                <div class="rating-progress">
                                                    <div style="width: ${stars.fiveStars / totalRv * 100}%"></div>
                                                </div>
                                                <span class="sum">${stars.fiveStars}</span>
                                            </li>
                                            <li>
                                                <div class="rating-stars">
                                                    <i class="fa fa-star"></i>
                                                    <i class="fa fa-star"></i>
                                                    <i class="fa fa-star"></i>
                                                    <i class="fa fa-star"></i>
                                                    <i class="fa fa-star-o"></i>
                                                </div>
                                                <div class="rating-progress">
                                                    <div style="width: ${stars.fourStars / totalRv * 100}%"></div>
                                                </div>
                                                <span class="sum">${stars.fourStars}</span>
                                            </li>
                                            <li>
                                                <div class="rating-stars">
                                                    <i class="fa fa-star"></i>
                                                    <i class="fa fa-star"></i>
                                                    <i class="fa fa-star"></i>
                                                    <i class="fa fa-star-o"></i>
                                                    <i class="fa fa-star-o"></i>
                                                </div>
                                                <div class="rating-progress">
                                                    <div style="width: ${stars.threeStars / totalRv * 100}%"></div>
                                                </div>
                                                <span class="sum">${stars.threeStars}</span>
                                            </li>
                                            <li>
                                                <div class="rating-stars">
                                                    <i class="fa fa-star"></i>
                                                    <i class="fa fa-star"></i>
                                                    <i class="fa fa-star-o"></i>
                                                    <i class="fa fa-star-o"></i>
                                                    <i class="fa fa-star-o"></i>
                                                </div>
                                                <div class="rating-progress">
                                                    <div style="width: ${stars.twoStars / totalRv * 100}%"></div>
                                                </div>
                                                <span class="sum">${stars.twoStars}</span>
                                            </li>
                                            <li>
                                                <div class="rating-stars">
                                                    <i class="fa fa-star"></i>
                                                    <i class="fa fa-star-o"></i>
                                                    <i class="fa fa-star-o"></i>
                                                    <i class="fa fa-star-o"></i>
                                                    <i class="fa fa-star-o"></i>
                                                </div>
                                                <div class="rating-progress">
                                                    <div style="width: ${stars.oneStars / totalRv * 100}%"></div>
                                                </div>
                                                <span class="sum">${stars.oneStars}</span>
                                            </li>
                                        </ul>
                                    </div>
                                </div>
                                <!-- /Rating -->

                                <!-- Reviews -->
                                <div class="col-md-6">
                                    <div id="reviews">
                                        <ul class="reviews">

                                            <c:if test="${totalRv == 0}">
                                                <li>
                                                    <p>Chưa có đánh giá nào cho sản phẩm này.</p>
                                                </li>
                                            </c:if>

                                            <c:if test="${totalRv > 0}">
                                                <c:forEach items="${reviews}" var="review">
                                                    <li>
                                                        <div class="review-heading">
                                                            <h5 class="name"><c:out
                                                                    value="${review.userDisplayName}"/></h5>
                                                            <p class="date"><fmt:formatDate value="${review.createdAt}"
                                                                                            pattern="dd MMM yyyy, h:mm a"/></p>
                                                            <div class="review-rating">
                                                                <c:forEach begin="1" end="${reviewScore}"
                                                                           var="i">
                                                                    <i class="fa fa-star"></i>
                                                                </c:forEach>
                                                                <c:forEach begin="${reviewScore + 1}" end="5"
                                                                           var="i">
                                                                    <i class="fa fa-star-o empty"></i>
                                                                </c:forEach>
                                                            </div>
                                                        </div>
                                                        <div class="review-body">
                                                            <p><c:out value="${review.message}" escapeXml="false"/></p>
                                                        </div>
                                                    </li>
                                                </c:forEach>
                                            </c:if>


                                        </ul>
                                        <ul class="reviews-pagination">
                                            <li class="active">1</li>
                                            <li><a href="#">2</a></li>
                                            <li><a href="#">3</a></li>
                                            <li><a href="#">4</a></li>
                                            <li><a href="#"><i class="fa fa-angle-right"></i></a></li>
                                        </ul>
                                    </div>
                                </div>
                                <!-- /Reviews -->

                                <!-- Review Form -->
                                <div class="col-md-3">
                                    <div id="review-form">
                                        <form class="review-form">

                                            <textarea class="input" placeholder="Đánh giá"></textarea>
                                            <div class="input-rating">
                                                <span>Your Rating: </span>
                                                <div class="stars">
                                                    <input id="star5" name="rating" value="5" type="radio"><label
                                                        for="star5"></label>
                                                    <input id="star4" name="rating" value="4" type="radio"><label
                                                        for="star4"></label>
                                                    <input id="star3" name="rating" value="3" type="radio"><label
                                                        for="star3"></label>
                                                    <input id="star2" name="rating" value="2" type="radio"><label
                                                        for="star2"></label>
                                                    <input id="star1" name="rating" value="1" type="radio"><label
                                                        for="star1"></label>
                                                </div>
                                            </div>
                                            <button class="primary-btn">Submit</button>
                                        </form>
                                    </div>
                                </div>
                                <!-- /Review Form -->
                            </div>
                        </div>
                        <!-- /tab3  -->
                    </div>
                    <!-- /product tab content  -->
                </div>
            </div>
            <!-- /product tab -->
        </div>
        <!-- /row -->
    </div>
    <!-- /container -->
</div>
<!-- /SECTION -->

<!-- Section -->
<div class="section">
    <!-- container -->
    <div class="container">
        <!-- row -->
        <div class="row">

            <div class="col-md-12">
                <div class="section-title text-center">
                    <h3 class="title">Sản Phẩm Tương Tự</h3>
                </div>
            </div>

            <div class="clearfix visible-sm visible-xs"></div>

            <!-- Products tab & slick -->
            <div class="col-md-12">
                <div class="row">
                    <div class="products-tabs">
                        <!-- tab -->
                        <div id="tab1" class="tab-pane active">
                            <div class="products-slick" data-nav="#slick-nav-1">
                                <c:forEach var="item" items="${relatedProducts}">
                                    <div class="col-md-4 col-xs-6">
                                        <c:set var="product" value="${item}" scope="request"/>
                                        <jsp:include page="../commons/product-card.jsp"/>
                                    </div>
                                </c:forEach>
                                <c:remove var="product" scope="request"/>

                                <c:if test="${empty relatedProducts}">
                                    <div class="col-md-12">
                                        <p>Không có sản phẩm tương tự.</p>
                                    </div>
                                </c:if><!-- product -->
                            </div>
                            <div id="slick-nav-1" class="products-slick-nav"></div>
                        </div>
                        <!-- /tab -->
                    </div>
                </div>
            </div>
            <!-- /product -->


        </div>
        <!-- /row -->
    </div>
    <!-- /container -->
</div>
<!-- /Section -->

<!-- NEWSLETTER -->
<div id="newsletter" class="section">
    <!-- container -->
    <div class="container">
        <!-- row -->
        <div class="row">
            <div class="col-md-12">
                <div class="newsletter">
                    <p>Đăng kí nhận thông tin từ <strong>CellphoneS</strong></p>
                    <form>
                        <input class="input" type="email" placeholder="Enter Your Email">
                        <button class="newsletter-btn"><i class="fa fa-envelope"></i> Subscribe</button>
                    </form>
                    <ul class="newsletter-follow">
                        <li>
                            <a href="#"><i class="fa fa-facebook"></i></a>
                        </li>
                        <li>
                            <a href="#"><i class="fa fa-twitter"></i></a>
                        </li>
                        <li>
                            <a href="#"><i class="fa fa-instagram"></i></a>
                        </li>
                        <li>
                            <a href="#"><i class="fa fa-pinterest"></i></a>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
        <!-- /row -->
    </div>
    <!-- /container -->
</div>
<!-- /NEWSLETTER -->

<!-- FOOTER -->
<footer id="footer">
    <!-- top footer -->
    <div class="section">
        <!-- container -->
        <div class="container">
            <!-- row -->
            <div class="row">
                <div class="col-md-3 col-xs-6">
                    <div class="footer">
                        <h3 class="footer-title">About Us</h3>
                        <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt
                            ut.</p>
                        <ul class="footer-links">
                            <li><a href="#"><i class="fa fa-map-marker"></i>1734 Stonecoal Road</a></li>
                            <li><a href="#"><i class="fa fa-phone"></i>+021-95-51-84</a></li>
                            <li><a href="#"><i class="fa fa-envelope-o"></i>email@email.com</a></li>
                        </ul>
                    </div>
                </div>

                <div class="col-md-3 col-xs-6">
                    <div class="footer">
                        <h3 class="footer-title">Categories</h3>
                        <ul class="footer-links">
                            <li><a href="#">Hot deals</a></li>
                            <li><a href="#">Laptops</a></li>
                            <li><a href="#">Smartphones</a></li>
                            <li><a href="#">Cameras</a></li>
                            <li><a href="#">Accessories</a></li>
                        </ul>
                    </div>
                </div>

                <div class="clearfix visible-xs"></div>

                <div class="col-md-3 col-xs-6">
                    <div class="footer">
                        <h3 class="footer-title">Information</h3>
                        <ul class="footer-links">
                            <li><a href="#">About Us</a></li>
                            <li><a href="#">Contact Us</a></li>
                            <li><a href="#">Privacy Policy</a></li>
                            <li><a href="#">Orders and Returns</a></li>
                            <li><a href="#">Terms & Conditions</a></li>
                        </ul>
                    </div>
                </div>

                <div class="col-md-3 col-xs-6">
                    <div class="footer">
                        <h3 class="footer-title">Service</h3>
                        <ul class="footer-links">
                            <li><a href="#">My Account</a></li>
                            <li><a href="#">View Cart</a></li>
                            <li><a href="#">Wishlist</a></li>
                            <li><a href="#">Track My Order</a></li>
                            <li><a href="#">Help</a></li>
                        </ul>
                    </div>
                </div>
            </div>
            <!-- /row -->
        </div>
        <!-- /container -->
    </div>
    <!-- /top footer -->

    <!-- bottom footer -->
    <div id="bottom-footer" class="section">
        <div class="container">
            <!-- row -->
            <div class="row">
                <div class="col-md-12 text-center">
                    <ul class="footer-payments">
                        <li><a href="#"><i class="fa fa-cc-visa"></i></a></li>
                        <li><a href="#"><i class="fa fa-credit-card"></i></a></li>
                        <li><a href="#"><i class="fa fa-cc-paypal"></i></a></li>
                        <li><a href="#"><i class="fa fa-cc-mastercard"></i></a></li>
                        <li><a href="#"><i class="fa fa-cc-discover"></i></a></li>
                        <li><a href="#"><i class="fa fa-cc-amex"></i></a></li>
                    </ul>
                    <span class="copyright">
								<!-- Link back to Colorlib can't be removed. Template is licensed under CC BY 3.0. -->
								Copyright &copy;<script>document.write(new Date().getFullYear());</script> All rights reserved | This template is made with <i
                            class="fa fa-heart-o" aria-hidden="true"></i> by <a href="https://colorlib.com"
                                                                                target="_blank">Colorlib</a>
                        <!-- Link back to Colorlib can't be removed. Template is licensed under CC BY 3.0. -->
							</span>
                </div>
            </div>
            <!-- /row -->
        </div>
        <!-- /container -->
    </div>
    <!-- /bottom footer -->
</footer>
<!-- /FOOTER -->

<!-- jQuery Plugins -->
<%@ include file="../commons/script.jsp" %>

</body>
</html>
