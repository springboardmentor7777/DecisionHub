
-- ==========================================
-- DECISIONHUB DATABASE SCHEMA
-- Database: decisionhub_db
-- DBMS: PostgreSQL
-- ==========================================

-- 1. USERS
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    role VARCHAR(30) DEFAULT 'USER'
        CHECK (role IN ('USER', 'ADMIN', 'MODERATOR')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. COMMUNITIES
CREATE TABLE IF NOT EXISTS communities (
    community_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    community_name VARCHAR(150) NOT NULL,
    description TEXT,
    created_by BIGINT REFERENCES users(user_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. DECISIONS
CREATE TABLE IF NOT EXISTS decisions (
    decision_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    community_id BIGINT REFERENCES communities(community_id)
        ON DELETE SET NULL,
    created_by BIGINT NOT NULL REFERENCES users(user_id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(30) DEFAULT 'OPEN'
        CHECK (status IN ('OPEN', 'CLOSED', 'DRAFT')),
    visibility VARCHAR(30) DEFAULT 'PUBLIC'
        CHECK (visibility IN ('PUBLIC', 'PRIVATE')),
    ends_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. OPTIONS
CREATE TABLE IF NOT EXISTS options (
    option_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    decision_id BIGINT NOT NULL
        REFERENCES decisions(decision_id) ON DELETE CASCADE,
    option_text VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (decision_id, option_id)
);

-- 5. VOTES (SINGLE-CHOICE)
CREATE TABLE IF NOT EXISTS votes (
    vote_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    decision_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(user_id)
        ON DELETE CASCADE,
    voted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (decision_id, option_id)
        REFERENCES options(decision_id, option_id)
        ON DELETE CASCADE,

    CONSTRAINT votes_one_vote_per_decision
        UNIQUE (decision_id, user_id)
);
-- 6. COMMENTS
CREATE TABLE IF NOT EXISTS comments (
    comment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    decision_id BIGINT NOT NULL
        REFERENCES decisions(decision_id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    parent_comment_id BIGINT
        REFERENCES comments(comment_id) ON DELETE CASCADE,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. NOTIFICATIONS
CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id)
        ON DELETE CASCADE,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. REPORTS
CREATE TABLE IF NOT EXISTS reports (
    report_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reported_by BIGINT NOT NULL REFERENCES users(user_id),
    target_type VARCHAR(30) NOT NULL,
    target_id BIGINT NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'REVIEWED', 'RESOLVED', 'REJECTED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- INDEXES
-- ==========================================

CREATE INDEX IF NOT EXISTS idx_decisions_community
    ON decisions(community_id);

CREATE INDEX IF NOT EXISTS idx_options_decision
    ON options(decision_id);

CREATE INDEX IF NOT EXISTS idx_votes_user
    ON votes(user_id);

CREATE INDEX IF NOT EXISTS idx_comments_decision
    ON comments(decision_id);

CREATE INDEX IF NOT EXISTS idx_notifications_user
    ON notifications(user_id);

-- ==========================================
-- END OF SCHEMA
-- ==========================================
