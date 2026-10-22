<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Create account"/>
<%@ include file="common/auth-header.jspf" %>
  <h1 class="auth-title">Create your account</h1>
  <form method="post" action="${ctx}/register" class="form" novalidate>
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <div class="field">
      <label for="fullName">Full name</label>
      <input type="text" id="fullName" name="fullName" value="<c:out value='${fullName}'/>" maxlength="100" required autofocus>
      <c:if test="${not empty errors.fullName}"><span class="field-error"><c:out value="${errors.fullName}"/></span></c:if>
    </div>
    <div class="field">
      <label for="email">Email</label>
      <input type="email" id="email" name="email" value="<c:out value='${email}'/>" maxlength="150" required autocomplete="username">
      <c:if test="${not empty errors.email}"><span class="field-error"><c:out value="${errors.email}"/></span></c:if>
    </div>
    <div class="field">
      <label for="password">Password</label>
      <input type="password" id="password" name="password" required autocomplete="new-password">
      <span class="hint">At least 8 characters, with a letter and a number.</span>
      <c:if test="${not empty errors.password}"><span class="field-error"><c:out value="${errors.password}"/></span></c:if>
    </div>
    <div class="field">
      <label for="confirm">Confirm password</label>
      <input type="password" id="confirm" name="confirm" required autocomplete="new-password">
      <c:if test="${not empty errors.confirm}"><span class="field-error"><c:out value="${errors.confirm}"/></span></c:if>
    </div>
    <button type="submit" class="btn btn-primary btn-block">Create account</button>
  </form>
  <p class="auth-switch">Already registered? <a href="${ctx}/login">Sign in</a></p>
<%@ include file="common/auth-footer.jspf" %>
