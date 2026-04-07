<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="util" tagdir="../utils" %>

<%@ attribute name="currentPage" required="true" %>
<%@ attribute name="totalPages" required="true" %>
<%@ attribute name="baseUrl" required="true" %>
<%@ attribute name="params" required="false" %>

<ul class="pagination">

    <!-- Prev -->
    <c:if test="${currentPage > 1}">
        <li>
            <a href="<util:buildingUrl baseUrl='${baseUrl}' params='${params}' page='${currentPage - 1}' />">
                «
            </a>
        </li>
    </c:if>

    <!-- Tính window -->
    <c:set var="startPage" value="${currentPage - 2}" />
    <c:set var="endPage" value="${currentPage + 2}" />

    <c:if test="${startPage < 1}">
        <c:set var="startPage" value="1"/>
        <c:set var="endPage" value="5"/>
    </c:if>

    <c:if test="${endPage > totalPages}">
        <c:set var="endPage" value="${totalPages}"/>
        <c:set var="startPage" value="${totalPages - 4}"/>
    </c:if>

    <!-- Fix nếu totalPages < 5 -->
    <c:if test="${startPage < 1}">
        <c:set var="startPage" value="1"/>
    </c:if>

    <!-- Trang đầu -->
    <c:if test="${startPage > 1}">
        <li>
            <a href="<util:buildUrl baseUrl='${baseUrl}' params='${params}' page='1' />">1</a>
        </li>
        <li><span>...</span></li>
    </c:if>

    <!-- Loop page -->
    <c:forEach begin="${startPage}" end="${endPage}" var="pageNum">
        <li class="${pageNum == currentPage ? 'active' : ''}">
            <a href="<util:buildUrl baseUrl='${baseUrl}' params='${params}' page='${pageNum}' />">
                    ${pageNum}
            </a>
        </li>
    </c:forEach>

    <!-- Trang cuối -->
    <c:if test="${endPage < totalPages}">
        <li><span>...</span></li>
        <li>
            <a href="<util:buildUrl baseUrl='${baseUrl}' params='${params}' page='${totalPages}' />">
                    ${totalPages}
            </a>
        </li>
    </c:if>

    <!-- Next -->
    <c:if test="${currentPage < totalPages}">
        <li>
            <a href="<util:buildUrl baseUrl='${baseUrl}' params='${params}' page='${currentPage + 1}' />">
                »
            </a>
        </li>
    </c:if>

</ul>