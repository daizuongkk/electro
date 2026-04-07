<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<nav id="navigation">
    <!-- container -->
    <div class="container">
        <!-- responsive-nav -->
        <div id="responsive-nav">
            <!-- NAV -->
            <ul class="main-nav nav navbar-nav">
                <c:set var="allTabActive" value="${param.tab eq 'all' or (empty param.category and empty selectedCategories)}"/>
                <li class="${allTabActive ? 'active' : ''}"><a href="shop?tab=all">Tất Cả</a></li>
                <c:forEach var="category" items="${categories}">
                    <c:set var="categoryActive"
                           value="${(not empty selectedCategories and selectedCategories.contains(category.key)) or (param.category eq category.key)}"/>
                    <li class="${categoryActive ? 'active' : ''}">
                        <a href="shop?category=${category.key}">
                                ${category.value}
                        </a>
                    </li>
                </c:forEach>
            </ul>
            <!-- /NAV -->
        </div>
        <!-- /responsive-nav -->
    </div>
    <!-- /container -->
</nav>
