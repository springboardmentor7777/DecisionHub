-- 1. USERS TABLE

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255),
    role VARCHAR(30) NOT NULL DEFAULT 'USER',
    bio VARCHAR(500),
    interests VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- 2. DECISIONS TABLE

CREATE TABLE decisions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    visibility VARCHAR(20) DEFAULT 'PUBLIC',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_decision_user
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE CASCADE
);
-- 3. OPTIONS TABLE

CREATE TABLE options (
    id BIGSERIAL PRIMARY KEY,
    decision_id BIGINT NOT NULL,
    option_title VARCHAR(200) NOT NULL,
    description TEXT,
    estimated_cost DECIMAL(12,2),
    benefits TEXT,
    risks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_option_decision
        FOREIGN KEY (decision_id)
        REFERENCES decisions(id)
        ON DELETE CASCADE
);
-- 4. VOTES TABLE

CREATE TABLE votes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    decision_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    rating INTEGER CHECK (rating BETWEEN 1 AND 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_vote_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_vote_decision
        FOREIGN KEY (decision_id)
        REFERENCES decisions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_vote_option
        FOREIGN KEY (option_id)
        REFERENCES options(id)
        ON DELETE CASCADE,

    CONSTRAINT unique_user_vote
        UNIQUE (user_id, decision_id)
);
-- 5. COMMENTS TABLE

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    decision_id BIGINT NOT NULL,
    parent_comment_id BIGINT,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comment_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_comment_decision
        FOREIGN KEY (decision_id)
        REFERENCES decisions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_parent_comment
        FOREIGN KEY (parent_comment_id)
        REFERENCES comments(id)
        ON DELETE CASCADE
);
-- 6. COMMUNITIES TABLE

CREATE TABLE communities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    created_by BIGINT NOT NULL,
    visibility VARCHAR(20) DEFAULT 'PUBLIC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_community_creator
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE CASCADE
);
-- 7. NOTIFICATIONS TABLE

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
-- 8. REPORTS TABLE

CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,
    decision_id BIGINT NOT NULL,
    generated_by BIGINT NOT NULL,
    report_type VARCHAR(100),
    report_data JSONB,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_report_decision
        FOREIGN KEY (decision_id)
        REFERENCES decisions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_report_user
        FOREIGN KEY (generated_by)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Milestone 2: predefined categories used by decisions and communities.
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO categories (name) VALUES
    ('Career'), ('Education'), ('Technology'),
    ('Travel'), ('Finance'), ('Lifestyle')
ON CONFLICT (name) DO NOTHING;

-- Milestone 2: decision options use the existing options table.
ALTER TABLE options ADD COLUMN IF NOT EXISTS pros TEXT;
ALTER TABLE options ADD COLUMN IF NOT EXISTS cons TEXT;

CREATE TABLE IF NOT EXISTS decision_factors (
    id BIGSERIAL PRIMARY KEY,
    decision_id BIGINT NOT NULL REFERENCES decisions(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    weight NUMERIC(10,4) NOT NULL CHECK (weight > 0),
    CONSTRAINT uk_factor_board_name UNIQUE (decision_id, name)
);

CREATE TABLE IF NOT EXISTS option_factor_scores (
    id BIGSERIAL PRIMARY KEY,
    option_id BIGINT NOT NULL REFERENCES options(id) ON DELETE CASCADE,
    factor_id BIGINT NOT NULL REFERENCES decision_factors(id) ON DELETE CASCADE,
    score NUMERIC(5,2) NOT NULL CHECK (score BETWEEN 0 AND 100),
    CONSTRAINT uk_option_factor UNIQUE (option_id, factor_id)
);

CREATE TABLE IF NOT EXISTS polls (
    id BIGSERIAL PRIMARY KEY,
    decision_id BIGINT NOT NULL REFERENCES decisions(id) ON DELETE CASCADE,
    created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    question VARCHAR(250) NOT NULL,
    type VARCHAR(30) NOT NULL,
    anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS poll_options (
    id BIGSERIAL PRIMARY KEY,
    poll_id BIGINT NOT NULL REFERENCES polls(id) ON DELETE CASCADE,
    label VARCHAR(200) NOT NULL
);

CREATE TABLE IF NOT EXISTS poll_votes (
    id BIGSERIAL PRIMARY KEY,
    poll_id BIGINT NOT NULL REFERENCES polls(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INTEGER CHECK (rating BETWEEN 1 AND 5),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_poll_voter UNIQUE (poll_id, user_id)
);

CREATE TABLE IF NOT EXISTS vote_selections (
    id BIGSERIAL PRIMARY KEY,
    vote_id BIGINT NOT NULL REFERENCES poll_votes(id) ON DELETE CASCADE,
    option_id BIGINT NOT NULL REFERENCES poll_options(id) ON DELETE CASCADE,
    CONSTRAINT uk_vote_option UNIQUE (vote_id, option_id)
);

CREATE TABLE IF NOT EXISTS community_memberships (
    id BIGSERIAL PRIMARY KEY,
    community_id BIGINT NOT NULL REFERENCES communities(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_community_member UNIQUE (community_id, user_id)
);
