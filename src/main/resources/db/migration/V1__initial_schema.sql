CREATE SCHEMA IF NOT EXISTS vivid_data;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Table: users
-- Stores user account information
CREATE TABLE vivid_data.users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id SERIAL UNIQUE NOT NULL,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    registration_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    default_currency VARCHAR(3) DEFAULT 'USD',
    default_language VARCHAR(10) DEFAULT 'en',
    notification_preferences JSONB
);

-- Table: accounts
-- Stores user's financial accounts (manual or linked via Plaid)
CREATE TABLE vivid_data.accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id INTEGER NOT NULL REFERENCES vivid_data.users(user_id),
    account_name VARCHAR(255) NOT NULL,
    account_nickname VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL, -- ex. CREDIT_CARD, PROPERTY, INVESTMENT
    subcategory VARCHAR(50), -- OTHER, VEHICLE, BROKERAGE, RETIREMENT, HEALTH
    plaid_account_id VARCHAR(255) UNIQUE,
    plaid_item_id VARCHAR(255),
    institution_name VARCHAR(255),
    current_balance INTEGER NOT NULL DEFAULT 0,
    available_balance INTEGER NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table: categories
-- Stores income and expense categories for transactions
CREATE TABLE vivid_data.transaction_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id INTEGER NOT NULL REFERENCES vivid_data.users(user_id),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, name, type)
);

-- Table: transactions
-- Stores individual income and expense transactions
CREATE TABLE vivid_data.transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id UUID NOT NULL REFERENCES vivid_data.accounts(id),
    transaction_type VARCHAR(50) NOT NULL,
    amount DECIMAL NOT NULL,
    description VARCHAR(255),
    category_id UUID NOT NULL references vivid_data.transaction_categories(id),
    transaction_date DATE NOT NULL,
    status VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    plaid_transaction_id VARCHAR(255) UNIQUE,
    notes TEXT
);

-- Table: recurring_transactions
-- Stores details for recurring income and expense transactions
CREATE TABLE vivid_data.recurring_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id INTEGER NOT NULL REFERENCES vivid_data.users(user_id),
    account_id UUID NOT NULL REFERENCES vivid_data.accounts(id),
    transaction_type VARCHAR(50) NOT NULL,
    amount DECIMAL NOT NULL,
    description VARCHAR(255),
    category_id UUID NOT NULL REFERENCES vivid_data.transaction_categories(id),
    start_date DATE NOT NULL,
    end_date DATE,
    frequency VARCHAR(50) NOT NULL,
    interval INTEGER DEFAULT 1,
    day_of_month INTEGER,
    day_of_week VARCHAR(10),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table: budgets
-- Stores budget amounts for different categories per period
CREATE TABLE vivid_data.budgets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id INTEGER NOT NULL REFERENCES vivid_data.users(user_id),
    category_id UUID NOT NULL REFERENCES vivid_data.transaction_categories(id),
    budget_amount DECIMAL NOT NULL,
    budget_period_start DATE NOT NULL,
    budget_period_end DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, category_id, budget_period_start, budget_period_end)
);

-- Table: financial_goals
-- Stores user-defined financial goals
CREATE TABLE vivid_data.financial_goals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id INTEGER NOT NULL REFERENCES vivid_data.users(user_id),
    goal_name VARCHAR(255) NOT NULL,
    goal_type VARCHAR(50) NOT NULL,
    target_amount DECIMAL NOT NULL,
    current_amount DECIMAL NOT NULL DEFAULT 0.00,
    target_date DATE,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table: net_worth_snapshots
-- Stores historical net worth snapshots for users
CREATE TABLE vivid_data.accounts_performance_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id INTEGER NOT NULL REFERENCES vivid_data.users(user_id),
    snapshot_date DATE NOT NULL,
    total_assets DECIMAL NOT NULL,
    total_liabilities DECIMAL NOT NULL,
    net_worth DECIMAL NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, snapshot_date)
);

CREATE INDEX idx_accounts_performance_snapshots_user_id ON vivid_data.accounts_performance_snapshots(user_id);