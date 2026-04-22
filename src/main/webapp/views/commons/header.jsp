<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ page import="com.daizuongkk.web.model.Role" %>
<base href="${pageContext.request.contextPath}/">

<fmt:setLocale value="vi_VN"/>

<!-- HEADER -->
<header>
    <!-- TOP HEADER -->
    <div id="top-header">
        <div class="container">
            <ul class="header-links pull-left">
                <li><a href="tel:+84334727801"><i class="fa fa-phone"></i> +84 334 727 801</a></li>
                <li><a href="mailto:trongdaidt147@gmail.com"><i class="fa fa-envelope-o"></i>trongdaidt147@gmail.com</a></li>
                <li><a href="#"><i class="fa fa-map-marker"></i> Xứ sở thần tiên, Việt Nam</a></li>
            </ul>
            <ul class="header-links pull-right">
                <c:choose>
                    <c:when test="${sessionScope.account.role == Role.ADMIN}">
                        <li><a href="admin"><i class="fa fa-database"></i>Trang Quản Trị</a></li>
                    </c:when>
                </c:choose>
                <c:choose>
                    <c:when test="${sessionScope.account != null}">
                        <li><a href="#"><i class="fa fa-user-o"></i>${sessionScope.account.firstName} ${sessionScope.account.lastName}</a></li>
                        <li><a href="logout"><i class="fa fa-sign-out"></i>Đăng Xuất</a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="login"><i class="fa fa-sign-in"></i>Đăng Nhập</a></li>
                        <li><a href="register"><i class="fa fa-user"></i>Đăng Kí</a></li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </div>
    <!-- /TOP HEADER -->

    <!-- MAIN HEADER -->
    <div id="header">
        <!-- container -->
        <div class="container">
            <!-- row -->
            <div class="row">
                <!-- LOGO -->
                <div class="col-md-3">
                    <div class="header-logo">
                        <a href="home" class="logo">
                            <img src="assets/img/logo.png" alt="">
                        </a>
                    </div>
                </div>
                <!-- /LOGO -->

                <!-- SEARCH BAR -->
                <div class="col-md-6">
                    <div class="header-search">
                        <form>
                            <select class="input-select">
                                <option value="0">Tất Cả</option>
                                <c:forEach var="category" items="${categories}">
                                    <option value="${category.key}">${category.value}</option>
                                </c:forEach>
                            </select>
                            <input class="input" placeholder="Tìm kếm...">
                            <button class="search-btn">Tìm Kếm</button>
                        </form>
                    </div>
                </div>
                <!-- /SEARCH BAR -->

                <!-- ACCOUNT -->
                <div class="col-md-3 clearfix">
                    <div class="header-ctn">
                        <!-- Wishlist -->
                        <div>
                            <a href="#">
                                <i class="fa fa-heart-o"></i>
                                <span>Yêu Thích</span>
                                <div class="qty">2</div>
                            </a>
                        </div>
                        <!-- /Wishlist -->

                        <!-- Cart -->
                        <div class="dropdown">
                            <a class="dropdown-toggle" data-toggle="dropdown" aria-expanded="true" >
                                <i class="fa fa-shopping-cart"></i>
                                <span>Giỏ Hàng</span>
                                <div class="qty"> ${fn:length(cart)}</div>
                            </a>
                            <div class="cart-dropdown">
                                <div class="cart-list">


                                    <c:choose>
                                        <c:when test="${empty sessionScope.cart}">
                                            <p>Giỏ hàng trống</p>
                                        </c:when>

<%--                                        <c:otherwise>--%>
<%--                                            <c:forEach var="item" items="${sessionScope.cart}">--%>
<%--                                                <div class="product-widget">--%>
<%--                                                    <div class="product-img">--%>
<%--                                                        <img src="${item.product.imageUrl[0]}" alt="">--%>
<%--                                                    </div>--%>
<%--                                                    <div class="product-body">--%>
<%--                                                        <h3 class="product-name"><a href="#">${item.product.name}</a>--%>
<%--                                                        </h3>--%>
<%--                                                        <h4 class="product-price"><span--%>
<%--                                                                class="qty">${item.quantity}</span>--%>
<%--                                                            <fmt:formatNumber--%>
<%--                                                                    value="${item.product.price}"--%>
<%--                                                                    type="currency"--%>
<%--                                                                    currencySymbol="₫" />--%>
<%--                                                        </h4>--%>
<%--                                                    </div>--%>
<%--                                                    <button class="delete"><i class="fa fa-close"></i></button>--%>
<%--                                                </div>--%>
<%--                                            </c:forEach>--%>

<%--                                        </c:otherwise>--%>
                                    </c:choose>

                                </div>
                                <div class="cart-summary">
<%--                                    <small>${fn:length(cart)} Đã chọn</small>--%>

<%--                                    <c:set var="totalPrice" value="0" />--%>

<%--                                    <c:forEach var="item" items="${sessionScope.cart}">--%>
<%--                                        <c:set var="totalPrice" value="${totalPrice + item.product.price * item.quantity}" />--%>
<%--                                    </c:forEach>--%>
<%--                                    <h5>Tổng:--%>
<%--                                        <fmt:formatNumber--%>
<%--                                                value="${totalPrice}"--%>
<%--                                                type="currency"--%>
<%--                                                currencySymbol="₫" /></h5>--%>
                                </div>
                                <div class="cart-btns">
                                    <a href="cart">Xem Giỏ Hàng <i class="fa fa-shopping-cart"></i></a>
                                </div>
                            </div>
                        </div>
                        <!-- /Cart -->

                        <!-- Menu Toogle -->
                        <div class="menu-toggle">
                            <a href="#">
                                <i class="fa fa-bars"></i>
                                <span>Menu</span>
                            </a>
                        </div>
                        <!-- /Menu Toogle -->
                    </div>
                </div>
                <!-- /ACCOUNT -->
            </div>
            <!-- row -->
        </div>
        <!-- container -->
    </div>
    <!-- /MAIN HEADER -->
</header>
<!-- /HEADER -->
<%@ include file="../commons/cart-modal.jsp" %>
