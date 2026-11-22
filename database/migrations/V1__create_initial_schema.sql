
-- V1: Initial DecisionHub Database Schema

-- 1. ROLES TABLE
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default roles
INSERT INTO roles (role_name, description)
VALUES
('USER', 'Regular DecisionHub user'),
('ADMIN', 'Administrator'),
('COMMUNITY_MODERATOR', 'Community moderator');

-- 2. USERS TABLE
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'USER',
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
);

-- Automatically assign the default role when a user is inserted
CREATE OR REPLACE FUNCTION assign_default_user_role()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.role_id IS NULL THEN
        SELECT id INTO NEW.role_id
        FROM roles
        WHERE role_name = UPPER(NEW.role);
    END IF;

    IF NEW.role_id IS NULL THEN
        RAISE EXCEPTION 'Invalid user role';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_assign_user_role
BEFORE INSERT ON users
FOR EACH ROW
EXECUTE FUNCTION assign_default_user_role();

-- 3. DECISIONS TABLE
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

-- 4. OPTIONS TABLE
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

-- 5. VOTES TABLE
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

-- 6. COMMENTS TABLE
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

-- 7. COMMUNITIES TABLE
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

-- 8. NOTIFICATIONS TABLE
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

-- 9. REPORTS TABLE
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
