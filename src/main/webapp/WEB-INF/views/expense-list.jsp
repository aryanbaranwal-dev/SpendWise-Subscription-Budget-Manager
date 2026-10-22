<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Expenses"/>
<c:set var="activePage" value="expenses"/>
<%@ include file="common/header.jspf" %>

<div class="page-head">
  <h1>Expenses</h1>
  <a class="btn btn-primary" href="${ctx}/expenses?action=new&amp;month=${month}">Add expense</a>
</div>

<form method="get" action="${ctx}/expenses" class="filters">
  <a class="btn btn-quiet" href="${ctx}/expenses?month=${prevMonth}&amp;category=<c:out value='${category}'/>" aria-label="Previous month">Prev</a>
  <div class="field-inline">
    <label for="month">Month</label>
    <input type="month" id="month" name="month" value="${month}" pattern="\d{4}-\d{2}" placeholder="YYYY-MM">
  </div>
  <div class="field-inline">
    <label for="category">Category</label>
    <select id="category" name="category">
      <option value="">All categories</option>
      <c:forEach items="${categories}" var="cat">
        <option value="${cat}" ${cat eq category ? 'selected' : ''}><c:out value="${cat}"/></option>
      </c:forEach>
    </select>
  </div>
  <button type="submit" class="btn btn-quiet">Apply</button>
  <a class="btn btn-quiet" href="${ctx}/expenses?month=${nextMonth}&amp;category=<c:out value='${category}'/>" aria-label="Next month">Next</a>
</form>

<section class="panel">
  <div class="panel-head">
    <h2><c:out value="${monthLabel}"/></h2>
    <span class="panel-total">Total &#8377;&nbsp;<fmt:formatNumber value="${total}" minFractionDigits="2" maxFractionDigits="2"/></span>
  </div>
  <c:choose>
    <c:when test="${empty expenses}">
      <div class="empty">
        <p class="empty-title">No expenses found</p>
        <p>Nothing recorded for <c:out value="${monthLabel}"/><c:if test="${not empty category}"> in <c:out value="${category}"/></c:if>.</p>
        <a class="btn btn-primary" href="${ctx}/expenses?action=new&amp;month=${month}">Add an expense</a>
      </div>
    </c:when>
    <c:otherwise>
      <div class="table-wrap">
        <table>
          <thead><tr><th>Date</th><th>Description</th><th>Category</th><th class="num">Amount</th><th class="actions-col">Actions</th></tr></thead>
          <tbody>
            <c:forEach items="${expenses}" var="e">
              <tr>
                <td><c:out value="${e.expenseDateDisplay}"/></td>
                <td><c:out value="${e.description}"/></td>
                <td><span class="tag"><c:out value="${e.category}"/></span></td>
                <td class="num">&#8377;&nbsp;<fmt:formatNumber value="${e.amount}" minFractionDigits="2" maxFractionDigits="2"/></td>
                <td class="actions">
                  <a class="btn btn-small" href="${ctx}/expenses?action=edit&amp;id=${e.id}&amp;month=${month}">Edit</a>
                  <form method="post" action="${ctx}/expenses" class="inline-form" data-confirm="Delete this expense? This cannot be undone.">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="id" value="${e.id}">
                    <input type="hidden" name="month" value="${month}">
                    <button type="submit" class="btn btn-small btn-danger">Delete</button>
                  </form>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </c:otherwise>
  </c:choose>
</section>
<%@ include file="common/footer.jspf" %>
