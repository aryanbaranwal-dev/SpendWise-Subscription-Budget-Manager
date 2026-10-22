<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="${editing ? 'Edit expense' : 'Add expense'}"/>
<c:set var="activePage" value="expenses"/>
<%@ include file="common/header.jspf" %>

<div class="page-head"><h1><c:out value="${pageTitle}"/></h1></div>

<section class="panel panel-form">
  <form method="post" action="${ctx}/expenses" class="form" novalidate>
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="hidden" name="action" value="save">
    <input type="hidden" name="id" value="<c:out value='${form.id}'/>">

    <div class="field">
      <label for="amount">Amount (&#8377;)</label>
      <input type="text" inputmode="decimal" id="amount" name="amount" value="<c:out value='${form.amount}'/>" placeholder="0.00" required autofocus>
      <c:if test="${not empty errors.amount}"><span class="field-error"><c:out value="${errors.amount}"/></span></c:if>
    </div>
    <div class="field">
      <label for="category">Category</label>
      <select id="category" name="category" required>
        <option value="">Choose a category</option>
        <c:forEach items="${categories}" var="cat">
          <option value="${cat}" ${cat eq form.category ? 'selected' : ''}><c:out value="${cat}"/></option>
        </c:forEach>
      </select>
      <c:if test="${not empty errors.category}"><span class="field-error"><c:out value="${errors.category}"/></span></c:if>
    </div>
    <div class="field">
      <label for="description">Description</label>
      <input type="text" id="description" name="description" value="<c:out value='${form.description}'/>" maxlength="255" required>
      <c:if test="${not empty errors.description}"><span class="field-error"><c:out value="${errors.description}"/></span></c:if>
    </div>
    <div class="field">
      <label for="expenseDate">Date</label>
      <input type="date" id="expenseDate" name="expenseDate" value="<c:out value='${form.expenseDate}'/>" max="${today}" min="2000-01-01" required>
      <c:if test="${not empty errors.expenseDate}"><span class="field-error"><c:out value="${errors.expenseDate}"/></span></c:if>
    </div>

    <div class="form-actions">
      <button type="submit" class="btn btn-primary"><c:out value="${editing ? 'Save changes' : 'Add expense'}"/></button>
      <a class="btn btn-quiet" href="${ctx}/expenses?month=${month}">Cancel</a>
    </div>
  </form>
</section>
<%@ include file="common/footer.jspf" %>
