<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
This file contains the <c:param> tags for pagination.
Must be included INSIDE a <c:url> tag.
--%>
<c:param name="size" value="${pageSize}"/>
<c:param name="name" value="${filterName}"/>
<c:param name="sortBy" value="${selectedSort}"/>
<c:param name="minPrice" value="${filterMinPrice}"/>
<c:param name="maxPrice" value="${filterMaxPrice}"/>

<c:forEach var="cat" items="${selectedCategories}">
    <c:param name="category" value="${cat}"/>
</c:forEach>
<c:forEach var="brand" items="${selectedBrands}">
    <c:param name="brand" value="${brand}"/>
</c:forEach>

