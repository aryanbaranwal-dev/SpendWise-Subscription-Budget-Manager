<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="${editing ? 'Edit subscription' : 'Add subscription'}"/>
<c:set var="activePage" value="subscriptions"/>
<%@ include file="common/header.jspf" %>

<div class="page-head"><h1><c:out value="${pageTitle}"/></h1></div>

<section class="panel panel-form">
  <form method="post" action="${ctx}/subscriptions" class="form" novalidate>
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="hidden" name="action" value="save">
    <input type="hidden" name="id" value="<c:out value='${form.id}'/>">

    <div class="field">
      <label for="name">Name</label>
      <input type="text" id="name" name="name" value="<c:out value='${form.name}'/>" maxlength="100" required autofocus>
      <c:if test="${not empty errors.name}"><span class="field-error"><c:out value="${errors.name}"/></span></c:if>
    </div>
    <div class="field">
      <label for="amount">Amount per payment (&#8377;)</label>
      <input type="text" inputmode="decimal" id="amount" name="amount" value="<c:out value='${form.amount}'/>" placeholder="0.00" required>
      <c:if test="${not empty errors.amount}"><span class="field-error"><c:out value="${errors.amount}"/></span></c:if>
    </div>
    <div class="field">
      <label for="billingCycle">Billing cycle</label>
      <select id="billingCycle" name="billingCycle" required>
        <c:forEach items="${cycles}" var="cy">
          <option value="${cy.code}" ${cy.code eq form.billingCycle ? 'selected' : ''}><c:out value="${cy.label}"/></option>
        </c:forEach>
      </select>
      <c:if test="${not empty errors.billingCycle}"><span class="field-error"><c:out value="${errors.billingCycle}"/></span></c:if>
    </div>
    <div class="field">
      <label for="nextPaymentDate">Next payment date</label>
      <input type="date" id="nextPaymentDate" name="nextPaymentDate" value="<c:out value='${form.nextPaymentDate}'/>" min="2000-01-01" required>
      <c:if test="${not empty errors.nextPaymentDate}"><span class="field-error"><c:out value="${errors.nextPaymentDate}"/></span></c:if>
    </div>
    <div class="field">
      <label for="status">Status</label>
      <select id="status" name="status" required>
        <c:forEach items="${statuses}" var="st">
          <option value="${st.code}" ${st.code eq form.status ? 'selected' : ''}><c:out value="${st.label}"/></option>
        </c:forEach>
      </select>
      <span class="hint">Only active subscriptions count towards the monthly estimate and upcoming payments.</span>
      <c:if test="${not empty errors.status}"><span class="field-error"><c:out value="${errors.status}"/></span></c:if>
    </div>

    <div class="form-actions">
      <button type="submit" class="btn btn-primary"><c:out value="${editing ? 'Save changes' : 'Add subscription'}"/></button>
      <a class="btn btn-quiet" href="${ctx}/subscriptions">Cancel</a>
    </div>
  </form>
</section>
<%@ include file="common/footer.jspf" %>
