-- 1. Add Poll Type to Decisions
ALTER TABLE decisions 
ADD COLUMN IF NOT EXISTS poll_type VARCHAR(30) DEFAULT 'SINGLE_CHOICE' 
CHECK (poll_type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'RATING_BASED'));

-- 2. Create table for Pros and Cons
CREATE TABLE IF NOT EXISTS pros_cons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    option_id UUID NOT NULL REFERENCES options(id) ON DELETE CASCADE,
    type VARCHAR(10) NOT NULL CHECK (type IN ('PRO', 'CON')),
    text TEXT NOT NULL,
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. Create table for Comparison Factors
CREATE TABLE IF NOT EXISTS comparison_factors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    decision_id UUID NOT NULL REFERENCES decisions(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    weight DECIMAL(5,2) DEFAULT 1.0
);

-- 4. Create table to store Option Scores
CREATE TABLE IF NOT EXISTS option_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    option_id UUID NOT NULL REFERENCES options(id) ON DELETE CASCADE,
    factor_id UUID NOT NULL REFERENCES comparison_factors(id) ON DELETE CASCADE,
    score INTEGER NOT NULL CHECK (score >= 0 AND score <= 10),
    UNIQUE (option_id, factor_id)
);
