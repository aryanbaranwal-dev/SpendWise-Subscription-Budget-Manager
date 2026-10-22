<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Sign in"/>
<%@ include file="common/auth-header.jspf" %>
  <h1 class="auth-title">Sign in</h1>
  <c:if test="${not empty loginError}"><div class="alert alert-error" role="alert"><c:out value="${loginError}"/></div></c:if>
  <form method="post" action="${ctx}/login" class="form">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <div class="field">
      <label for="email">Email</label>
      <input type="email" id="email" name="email" value="<c:out value='${email}'/>" required maxlength="150" autocomplete="username" autofocus>
    </div>
    <div class="field">
      <label for="password">Password</label>
      <input type="password" id="password" name="password" required autocomplete="current-password">
    </div>
    <button type="submit" class="btn btn-primary btn-block">Sign in</button>
  </form>
  <p class="auth-switch">New to SpendWise? <a href="${ctx}/register">Create an account</a></p>
<%@ include file="common/auth-footer.jspf" %>
