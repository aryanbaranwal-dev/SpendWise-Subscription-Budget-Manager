-- WARNING: deletes ALL SpendWise data in the current schema.
-- Errors like ORA-00942 / ORA-02289 just mean the object did not exist.
DROP TABLE sw_budgets        CASCADE CONSTRAINTS PURGE;
DROP TABLE sw_subscriptions  CASCADE CONSTRAINTS PURGE;
DROP TABLE sw_expenses       CASCADE CONSTRAINTS PURGE;
DROP TABLE sw_users          CASCADE CONSTRAINTS PURGE;
DROP SEQUENCE sw_budget_seq;
DROP SEQUENCE sw_sub_seq;
DROP SEQUENCE sw_expense_seq;
DROP SEQUENCE sw_user_seq;
