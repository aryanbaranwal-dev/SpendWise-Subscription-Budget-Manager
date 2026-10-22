<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Dashboard"/>
<c:set var="activePage" value="dashboard"/>
<%@ include file="common/header.jspf" %>

<div class="page-head">
  <h1>Dashboard</h1>
  <div class="month-nav">
    <a class="btn btn-quiet" href="${ctx}/dashboard?month=${prevMonth}" aria-label="Previous month">Prev</a>
    <form method="get" action="${ctx}/dashboard" class="inline-form">
      <input type="month" name="month" value="${month}" pattern="\d{4}-\d{2}" placeholder="YYYY-MM" aria-label="Month">
      <button type="submit" class="btn btn-quiet">Show</button>
    </form>
    <a class="btn btn-quiet" href="${ctx}/dashboard?month=${nextMonth}" aria-label="Next month">Next</a>
  </div>
</div>

<section class="meter-panel" aria-label="Budget for ${monthLabel}">
  <c:choose>
    <c:when test="${summary.budgetSet}">
      <div class="meter-top">
        <div>
          <div class="meter-caption">
            <c:choose>
              <c:when test="${summary.overBudget}">Over budget in <c:out value="${monthLabel}"/> by</c:when>
              <c:otherwise>Left to spend in <c:out value="${monthLabel}"/></c:otherwise>
            </c:choose>
          </div>
          <div class="meter-figure ${summary.overBudget ? 'is-over' : (summary.nearLimit ? 'is-near' : 'is-ok')}">
            &#8377;&nbsp;<fmt:formatNumber value="${summary.overBudget ? -summary.remaining : summary.remaining}" minFractionDigits="2" maxFractionDigits="2"/>
          </div>
        </div>
        <div class="meter-side">
          <div>Budget <strong>&#8377;&nbsp;<fmt:formatNumber value="${summary.budget}" minFractionDigits="2" maxFractionDigits="2"/></strong></div>
          <div>Spent <strong>&#8377;&nbsp;<fmt:formatNumber value="${summary.totalExpenses}" minFractionDigits="2" maxFractionDigits="2"/></strong></div>
        </div>
      </div>
      <div class="meter" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="${summary.progressWidth}" aria-label="Budget used">
        <div class="meter-fill ${summary.overBudget ? 'is-over' : (summary.nearLimit ? 'is-near' : 'is-ok')}" style="width:${summary.progressWidth}%"></div>
      </div>
      <div class="meter-foot"><fmt:formatNumber value="${summary.utilizationPercent}" minFractionDigits="1" maxFractionDigits="1"/>% of the budget used</div>
    </c:when>
    <c:otherwise>
      <div class="empty empty-flat">
        <p class="empty-title">No budget set for <c:out value="${monthLabel}"/></p>
        <p>Set a monthly budget to see how much you have left to spend.</p>
        <a class="btn btn-primary" href="${ctx}/budget?month=${month}">Set budget</a>
      </div>
    </c:otherwise>
  </c:choose>
</section>

<section class="stat-row" aria-label="Summary">
  <div class="stat">
    <div class="stat-label">Recorded expenses</div>
    <div class="stat-value">&#8377;&nbsp;<fmt:formatNumber value="${summary.totalExpenses}" minFractionDigits="2" maxFractionDigits="2"/></div>
    <div class="stat-note">in <c:out value="${monthLabel}"/></div>
  </div>
  <div class="stat">
    <div class="stat-label">Subscriptions per month (estimate)</div>
    <div class="stat-value">&#8377;&nbsp;<fmt:formatNumber value="${summary.monthlySubscriptionCost}" minFractionDigits="2" maxFractionDigits="2"/></div>
    <div class="stat-note">across <c:out value="${summary.activeSubscriptionCount}"/> active</div>
  </div>
  <div class="stat">
    <div class="stat-label">Budget used</div>
    <div class="stat-value"><fmt:formatNumber value="${summary.utilizationPercent}" minFractionDigits="1" maxFractionDigits="1"/>%</div>
    <div class="stat-note">of <c:out value="${monthLabel}"/></div>
  </div>
</section>
<p class="footnote">Subscription costs are shown separately. They are not added to your expenses and are not subtracted from the remaining budget.</p>

<div class="two-col">
  <section class="panel">
    <div class="panel-head"><h2>Recent expenses</h2><a href="${ctx}/expenses?month=${month}">View all</a></div>
    <c:choose>
      <c:when test="${empty summary.recentExpenses}">
        <div class="empty">
          <p class="empty-title">No expenses in <c:out value="${monthLabel}"/></p>
          <a class="btn btn-primary" href="${ctx}/expenses?action=new">Add an expense</a>
        </div>
      </c:when>
      <c:otherwise>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Date</th><th>Description</th><th>Category</th><th class="num">Amount</th></tr></thead>
            <tbody>
              <c:forEach items="${summary.recentExpenses}" var="e">
                <tr>
                  <td><c:out value="${e.expenseDateDisplay}"/></td>
                  <td><c:out value="${e.description}"/></td>
                  <td><span class="tag"><c:out value="${e.category}"/></span></td>
                  <td class="num">&#8377;&nbsp;<fmt:formatNumber value="${e.amount}" minFractionDigits="2" maxFractionDigits="2"/></td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </c:otherwise>
    </c:choose>
  </section>

  <section class="panel">
    <div class="panel-head"><h2>Upcoming payments</h2><a href="${ctx}/subscriptions">Manage</a></div>
    <c:choose>
      <c:when test="${empty summary.upcomingSubscriptions}">
        <div class="empty">
          <p class="empty-title">Nothing due in the next 30 days</p>
          <a class="btn btn-primary" href="${ctx}/subscriptions?action=new">Add a subscription</a>
        </div>
      </c:when>
      <c:otherwise>
        <ul class="due-list">
          <c:forEach items="${summary.upcomingSubscriptions}" var="s">
            <li>
              <div>
                <div class="due-name"><c:out value="${s.name}"/></div>
                <div class="due-meta ${s.overdue ? 'is-over' : ''}"><c:out value="${s.dueText}"/> (<c:out value="${s.nextPaymentDisplay}"/>)</div>
              </div>
              <div class="due-amount">&#8377;&nbsp;<fmt:formatNumber value="${s.amount}" minFractionDigits="2" maxFractionDigits="2"/>
                <span class="due-cycle"><c:out value="${s.billingCycle.label}"/></span></div>
            </li>
          </c:forEach>
        </ul>
      </c:otherwise>
    </c:choose>
  </section>
</div>
<%@ include file="common/footer.jspf" %>
