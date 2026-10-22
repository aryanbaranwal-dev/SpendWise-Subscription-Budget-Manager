<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Subscriptions"/>
<c:set var="activePage" value="subscriptions"/>
<%@ include file="common/header.jspf" %>

<div class="page-head">
  <h1>Subscriptions</h1>
  <a class="btn btn-primary" href="${ctx}/subscriptions?action=new">Add subscription</a>
</div>

<section class="panel">
  <div class="panel-head">
    <h2>All subscriptions</h2>
    <span class="panel-total">Active, per month &#8377;&nbsp;<fmt:formatNumber value="${monthlyTotal}" minFractionDigits="2" maxFractionDigits="2"/></span>
  </div>
  <c:choose>
    <c:when test="${empty subscriptions}">
      <div class="empty">
        <p class="empty-title">No subscriptions yet</p>
        <p>Add Netflix, your phone plan or anything that renews, and SpendWise will show when each payment is due.</p>
        <a class="btn btn-primary" href="${ctx}/subscriptions?action=new">Add a subscription</a>
      </div>
    </c:when>
    <c:otherwise>
      <div class="table-wrap">
        <table>
          <thead>
            <tr><th>Name</th><th class="num">Amount</th><th>Cycle</th><th class="num">Per month</th><th>Next payment</th><th>Status</th><th class="actions-col">Actions</th></tr>
          </thead>
          <tbody>
            <c:forEach items="${subscriptions}" var="s">
              <tr class="${s.active ? '' : 'row-muted'}">
                <td><c:out value="${s.name}"/></td>
                <td class="num">&#8377;&nbsp;<fmt:formatNumber value="${s.amount}" minFractionDigits="2" maxFractionDigits="2"/></td>
                <td><c:out value="${s.billingCycle.label}"/></td>
                <td class="num">&#8377;&nbsp;<fmt:formatNumber value="${s.monthlyEquivalent}" minFractionDigits="2" maxFractionDigits="2"/></td>
                <td>
                  <c:out value="${s.nextPaymentDisplay}"/>
                  <c:if test="${s.active}"><div class="due-meta ${s.overdue ? 'is-over' : ''}"><c:out value="${s.dueText}"/></div></c:if>
                </td>
                <td><span class="badge badge-${s.status.cssClass}"><c:out value="${s.status.label}"/></span></td>
                <td class="actions">
                  <c:if test="${s.active}">
                    <form method="post" action="${ctx}/subscriptions" class="inline-form">
                      <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                      <input type="hidden" name="action" value="paid">
                      <input type="hidden" name="id" value="${s.id}">
                      <button type="submit" class="btn btn-small">Mark paid</button>
                    </form>
                  </c:if>
                  <a class="btn btn-small" href="${ctx}/subscriptions?action=edit&amp;id=${s.id}">Edit</a>
                  <form method="post" action="${ctx}/subscriptions" class="inline-form" data-confirm="Delete this subscription? This cannot be undone.">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="id" value="${s.id}">
                    <button type="submit" class="btn btn-small btn-danger">Delete</button>
                  </form>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
      <p class="footnote panel-foot">Weekly costs use 52 weeks &divide; 12 months, yearly costs are divided by 12. "Mark paid" moves the next payment date forward by one cycle; it does not create an expense.</p>
    </c:otherwise>
  </c:choose>
</section>
<%@ include file="common/footer.jspf" %>
