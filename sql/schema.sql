-- =====================================================================
-- SpendWise - Oracle schema
-- Run this while connected as YOUR APPLICATION USER (not SYS / SYSTEM).
-- Works on Oracle 11g and later: uses sequences, no identity columns.
-- Re-running it on an existing schema will fail with ORA-00955;
-- use sql/drop_schema.sql first if you want a clean start.
-- =====================================================================

-- ---------- Sequences (primary-key generators) -----------------------
CREATE SEQUENCE sw_user_seq    START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sw_expense_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sw_sub_seq     START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sw_budget_seq  START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------- Users -----------------------------------------------------
-- Email is stored in lower case by the application and must be unique.
CREATE TABLE sw_users (
  user_id        NUMBER(10)     NOT NULL,
  full_name      VARCHAR2(100)  NOT NULL,
  email          VARCHAR2(150)  NOT NULL,
  password_hash  VARCHAR2(255)  NOT NULL,
  created_at     TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
  CONSTRAINT pk_sw_users PRIMARY KEY (user_id),
  CONSTRAINT uq_sw_users_email UNIQUE (email)
);

-- ---------- Expenses --------------------------------------------------
CREATE TABLE sw_expenses (
  expense_id    NUMBER(10)     NOT NULL,
  user_id       NUMBER(10)     NOT NULL,
  amount        NUMBER(12,2)   NOT NULL,
  category      VARCHAR2(40)   NOT NULL,
  description   VARCHAR2(255)  NOT NULL,
  expense_date  DATE           NOT NULL,
  created_at    TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
  CONSTRAINT pk_sw_expenses PRIMARY KEY (expense_id),
  CONSTRAINT fk_sw_expenses_user FOREIGN KEY (user_id)
    REFERENCES sw_users (user_id) ON DELETE CASCADE,
  CONSTRAINT ck_sw_expenses_amount CHECK (amount > 0)
);

CREATE INDEX idx_sw_expenses_user_date ON sw_expenses (user_id, expense_date);

-- ---------- Subscriptions ---------------------------------------------
CREATE TABLE sw_subscriptions (
  sub_id             NUMBER(10)     NOT NULL,
  user_id            NUMBER(10)     NOT NULL,
  sub_name           VARCHAR2(100)  NOT NULL,
  amount             NUMBER(12,2)   NOT NULL,
  billing_cycle      VARCHAR2(10)   NOT NULL,
  next_payment_date  DATE           NOT NULL,
  status             VARCHAR2(10)   DEFAULT 'ACTIVE' NOT NULL,
  created_at         TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
  CONSTRAINT pk_sw_subscriptions PRIMARY KEY (sub_id),
  CONSTRAINT fk_sw_subscriptions_user FOREIGN KEY (user_id)
    REFERENCES sw_users (user_id) ON DELETE CASCADE,
  CONSTRAINT ck_sw_subscriptions_amount CHECK (amount > 0),
  CONSTRAINT ck_sw_subscriptions_cycle CHECK (billing_cycle IN ('WEEKLY','MONTHLY','YEARLY')),
  CONSTRAINT ck_sw_subscriptions_status CHECK (status IN ('ACTIVE','PAUSED','CANCELLED'))
);

CREATE INDEX idx_sw_subs_user_status_due ON sw_subscriptions (user_id, status, next_payment_date);

-- ---------- Monthly budgets -------------------------------------------
-- budget_month always holds the FIRST day of the month (enforced by the app).
-- The unique constraint prevents two budgets for the same user and month.
CREATE TABLE sw_budgets (
  budget_id     NUMBER(10)    NOT NULL,
  user_id       NUMBER(10)    NOT NULL,
  budget_month  DATE          NOT NULL,
  amount        NUMBER(12,2)  NOT NULL,
  created_at    TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
  CONSTRAINT pk_sw_budgets PRIMARY KEY (budget_id),
  CONSTRAINT fk_sw_budgets_user FOREIGN KEY (user_id)
    REFERENCES sw_users (user_id) ON DELETE CASCADE,
  CONSTRAINT uq_sw_budgets_user_month UNIQUE (user_id, budget_month),
  CONSTRAINT ck_sw_budgets_amount CHECK (amount > 0)
);

-- Verify (optional): should list 4 tables and 4 sequences
-- SELECT table_name FROM user_tables WHERE table_name LIKE 'SW\_%' ESCAPE '\';
-- SELECT sequence_name FROM user_sequences WHERE sequence_name LIKE 'SW\_%' ESCAPE '\';
