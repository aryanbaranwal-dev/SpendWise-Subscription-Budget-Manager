<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Something went wrong"/>
<c:set var="code" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<%@ include file="common/auth-header.jspf" %>
  <c:choose>
    <c:when test="${code == 404}">
      <h1 class="auth-title">Page not found</h1>
      <p>The page you asked for does not exist. Check the address or go back to the dashboard.</p>
    </c:when>
    <c:when test="${code == 403}">
      <h1 class="auth-title">Request blocked</h1>
      <p><c:out value="${requestScope['jakarta.servlet.error.message']}"/></p>
    </c:when>
    <c:otherwise>
      <h1 class="auth-title">Something went wrong</h1>
      <p>The server could not complete your request. If you are the developer, open the Tomcat console
         for the full error, and check <code>/health</code> to test the database connection.</p>
    </c:otherwise>
  </c:choose>
  <p class="auth-switch"><a href="${ctx}/dashboard">Back to the dashboard</a></p>
<%@ include file="common/auth-footer.jspf" %>
