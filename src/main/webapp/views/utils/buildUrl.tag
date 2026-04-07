<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ attribute name="params" required="false" %>
<%@ attribute name="baseUrl" required="true" %>
<%@ attribute name="page" required="true" %>

<c:url var="resultUrl" value="${baseUrl}">
    <c:forEach var="entry" items="${params}">
        <c:forEach var="val" items="${entry.value}">
            <c:if test="${entry.key ne 'page'}">
                <c:param name="${entry.key}" value="${val}" />
            </c:if>
        </c:forEach>
    </c:forEach>
    <c:param name="page" value="${page}" />
</c:url>

${resultUrl}