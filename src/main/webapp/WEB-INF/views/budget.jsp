<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Budget"/>
<c:set var="activePage" value="budget"/>
<%@ include file="common/header.jspf" %>

<div class="page-head">
  <h1>Monthly budget</h1>
  <div class="month-nav">
    <a class="btn btn-quiet" href="${ctx}/budget?month=${prevMonth}" aria-label="Previous month">Prev</a>
    <form method="get" action="${ctx}/budget" class="inline-form">
      <input type="month" name="month" value="${month}" pattern="\d{4}-\d{2}" placeholder="YYYY-MM" aria-label="Month">
      <button type="submit" class="btn btn-quiet">Show</button>
    </form>
    <a class="btn btn-quiet" href="${ctx}/budget?month=${nextMonth}" aria-label="Next month">Next</a>
  </div>
</div>

<div class="two-col">
  <section class="panel panel-form">
    <div class="panel-head"><h2><c:out value="${summary.budgetSet ? 'Update budget' : 'Set budget'}"/> for <c:out value="${monthLabel}"/></h2></div>
    <form method="post" action="${ctx}/budget" class="form" novalidate>
      <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
      <input type="hidden" name="month" value="${month}">
      <div class="field">
        <label for="amount">Budget amount (&#8377;)</label>
        <input type="text" inputmode="decimal" id="amount" name="amount" value="<c:out value='${amountValue}'/>" placeholder="0.00" required autofocus>
        <span class="hint">One budget per month. Saving again replaces the amount.</span>
        <c:if test="${not empty errors.amount}"><span class="field-error"><c:out value="${errors.amount}"/></span></c:if>
      </div>
      <div class="form-actions">
        <button type="submit" class="btn btn-primary"><c:out value="${summary.budgetSet ? 'Update budget' : 'Save budget'}"/></button>
      </div>
    </form>
  </section>

  <section class="panel">
    <div class="panel-head"><h2>Position for <c:out value="${monthLabel}"/></h2></div>
    <dl class="facts">
      <div><dt>Budget</dt><dd>
        <c:choose>
          <c:when test="${summary.budgetSet}">&#8377;&nbsp;<fmt:formatNumber value="${summary.budget}" minFractionDigits="2" maxFractionDigits="2"/></c:when>
          <c:otherwise>Not set</c:otherwise>
        </c:choose></dd></div>
      <div><dt>Recorded expenses</dt><dd>&#8377;&nbsp;<fmt:formatNumber value="${summary.totalExpenses}" minFractionDigits="2" maxFractionDigits="2"/></dd></div>
      <c:if test="${summary.budgetSet}">
        <div><dt>Remaining</dt><dd class="${summary.overBudget ? 'is-over' : 'is-ok'}">&#8377;&nbsp;<fmt:formatNumber value="${summary.remaining}" minFractionDigits="2" maxFractionDigits="2"/></dd></div>
        <div><dt>Used</dt><dd><fmt:formatNumber value="${summary.utilizationPercent}" minFractionDigits="1" maxFractionDigits="1"/>%</dd></div>
      </c:if>
      <div><dt>Subscriptions per month (estimate)</dt><dd>&#8377;&nbsp;<fmt:formatNumber value="${summary.monthlySubscriptionCost}" minFractionDigits="2" maxFractionDigits="2"/></dd></div>
    </dl>
    <c:if test="${summary.budgetSet}">
      <div class="meter meter-slim" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="${summary.progressWidth}" aria-label="Budget used">
        <div class="meter-fill ${summary.overBudget ? 'is-over' : (summary.nearLimit ? 'is-near' : 'is-ok')}" style="width:${summary.progressWidth}%"></div>
      </div>
    </c:if>
    <p class="footnote panel-foot">Remaining = budget minus recorded expenses. The subscription estimate is shown for information and is not deducted.</p>
  </section>
</div>
<%@ include file="common/footer.jspf" %>
