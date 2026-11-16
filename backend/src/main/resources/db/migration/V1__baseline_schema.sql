-- ==========================================
-- FLYWAY BASELINE MIGRATION — V1
--
-- Purpose:
--   This file represents the complete DecisionHub schema as it existed
--   before Flyway was introduced.
--
-- Behaviour:
--   Existing Neon database (non-empty, no flyway_schema_history table):
--     baseline-on-migrate=true causes Flyway to create the
--     flyway_schema_history table, insert a baseline row at version 1,
--     and SKIP executing this V1 script. All existing data is preserved.
--
--   Fresh empty database:
--     Flyway executes this script normally to create the complete schema.
--     No baseline row exists yet, so V1 runs as a standard migration.
--
--   Future migrations (V2, V3, ...):
--     Always executed normally on both existing and fresh databases.
--
-- Safety:
--   All CREATE TABLE and CREATE INDEX statements use IF NOT EXISTS.
--   Both trigger functions use CREATE OR REPLACE FUNCTION.
--   DROP TRIGGER IF EXISTS is added before each CREATE TRIGGER.
--   This script is safe to run multiple times on an empty database.
--   It will NOT drop, truncate, or alter existing tables or data.
-- ==========================================


-- ==========================================
-- 1. ROLES
-- ==========================================
CREATE TABLE IF NOT EXISTS roles (
    role_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL
);


-- ==========================================
-- 2. USERS
-- ==========================================
-- CHANGED: role VARCHAR(30) replaced by role_id FK -> roles.
CREATE TABLE IF NOT EXISTS users (
    user_id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    role_id       BIGINT NOT NULL REFERENCES roles(role_id),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- ==========================================
-- 3. COMMUNITIES
-- ==========================================
-- UNCHANGED.
CREATE TABLE IF NOT EXISTS communities (
    community_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    community_name VARCHAR(150) NOT NULL,
    description    TEXT,
    created_by     BIGINT REFERENCES users(user_id),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- ==========================================
-- 4. DECISIONS
-- ==========================================
-- ADDED: poll_type column.
CREATE TABLE IF NOT EXISTS decisions (
    decision_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    community_id BIGINT REFERENCES communities(community_id)
                     ON DELETE SET NULL,
    created_by   BIGINT NOT NULL REFERENCES users(user_id),
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    poll_type    VARCHAR(30) NOT NULL DEFAULT 'SINGLE_CHOICE'
                     CHECK (poll_type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'RATING')),
    status       VARCHAR(30) DEFAULT 'OPEN'
                     CHECK (status IN ('OPEN', 'CLOSED', 'DRAFT')),
    visibility   VARCHAR(30) DEFAULT 'PUBLIC'
                     CHECK (visibility IN ('PUBLIC', 'PRIVATE')),
    ends_at      TIMESTAMP,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- ==========================================
-- 5. OPTIONS
-- ==========================================
-- UNCHANGED.
-- UNIQUE(decision_id, option_id) is required by the composite FK in votes.
CREATE TABLE IF NOT EXISTS options (
    option_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    decision_id BIGINT NOT NULL
                    REFERENCES decisions(decision_id) ON DELETE CASCADE,
    option_text VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (decision_id, option_id)
);


-- ==========================================
-- 6. VOTES
-- ==========================================
-- KEY DESIGN NOTES:
--
--   option_id - nullable; NULL for RATING polls.
--   rating    - nullable; NULL for option-based polls; 1-5 for RATING polls.
--
--   UNIQUE(decision_id, user_id, option_id)
--     -> Prevents the same user voting for the same option twice.
--     -> Allows multiple different options per user (MULTIPLE_CHOICE).
--     -> PostgreSQL treats NULLs as distinct, so RATING rows are covered
--        by the partial unique index below instead.
--
--   Composite FK (decision_id, option_id) -> options(decision_id, option_id)
--     -> Ensures an option belongs to the correct decision.
--     -> PostgreSQL automatically skips the FK check when option_id IS NULL
--        (standard SQL composite FK behaviour), making RATING votes safe.
--
--   Trigger trg_validate_vote_poll_type (defined below):
--     -> Locks the decisions row (FOR UPDATE) to serialise concurrent
--        inserts and prevent concurrent poll_type changes.
--     -> Validates that the vote type matches the decision's poll_type.
--     -> Enforces one-vote-per-user for SINGLE_CHOICE polls.
--
--   Trigger trg_prevent_poll_type_change on decisions (defined below):
--     -> Prevents changing poll_type after votes have been cast.
--
--   Deadlock prevention: both triggers always access tables in the same
--   order (decisions first, votes second) - no circular lock dependency.

CREATE TABLE IF NOT EXISTS votes (
    vote_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    decision_id BIGINT NOT NULL REFERENCES decisions(decision_id) ON DELETE CASCADE,
    option_id   BIGINT,
    user_id     BIGINT NOT NULL REFERENCES users(user_id)         ON DELETE CASCADE,
    rating      INTEGER,
    voted_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Composite FK: ensures option belongs to this decision.
    -- Automatically skipped by PostgreSQL when option_id IS NULL.
    FOREIGN KEY (decision_id, option_id)
        REFERENCES options(decision_id, option_id)
        ON DELETE CASCADE,

    -- Prevents duplicate votes for the same option by the same user.
    -- Allows MULTIPLE_CHOICE (different option_ids for same decision+user).
    CONSTRAINT votes_one_option_per_user
        UNIQUE (decision_id, user_id, option_id),

    -- Either option_id or rating must be set - never both, never neither.
    CONSTRAINT votes_valid_type CHECK (
        (option_id IS NOT NULL AND rating IS NULL)
        OR
        (option_id IS NULL     AND rating IS NOT NULL)
    ),

    -- Rating must be 1-5 when present.
    CONSTRAINT votes_rating_range CHECK (
        rating IS NULL OR (rating >= 1 AND rating <= 5)
    )
);

-- Partial unique index: prevents a user from submitting more than one
-- RATING vote for the same decision.
-- Required because UNIQUE(decision_id, user_id, option_id) treats
-- NULL option_id as distinct and would otherwise allow duplicates.
CREATE UNIQUE INDEX IF NOT EXISTS votes_one_rating_per_decision
    ON votes(decision_id, user_id)
    WHERE rating IS NOT NULL;


-- ==========================================
-- 7. COMMENTS
-- ==========================================
-- UNCHANGED.
CREATE TABLE IF NOT EXISTS comments (
    comment_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    decision_id       BIGINT NOT NULL REFERENCES decisions(decision_id) ON DELETE CASCADE,
    user_id           BIGINT NOT NULL REFERENCES users(user_id),
    parent_comment_id BIGINT REFERENCES comments(comment_id) ON DELETE CASCADE,
    comment_text      TEXT NOT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- ==========================================
-- 8. NOTIFICATIONS
-- ==========================================
-- UNCHANGED.
CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    message         TEXT NOT NULL,
    is_read         BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- ==========================================
-- 9. REPORTS
-- ==========================================
-- UNCHANGED.
CREATE TABLE IF NOT EXISTS reports (
    report_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reported_by BIGINT NOT NULL REFERENCES users(user_id),
    target_type VARCHAR(30) NOT NULL,
    target_id   BIGINT NOT NULL,
    reason      TEXT NOT NULL,
    status      VARCHAR(30) DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'REVIEWED', 'RESOLVED', 'REJECTED')),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- ==========================================
-- TRIGGER 1: Validate vote type and enforce
--            SINGLE_CHOICE one-vote-per-user rule
-- ==========================================
-- CREATE OR REPLACE handles reruns for the function.
-- DROP TRIGGER IF EXISTS handles reruns for the trigger binding.
CREATE OR REPLACE FUNCTION validate_vote_poll_type()
RETURNS TRIGGER AS $$
DECLARE
    v_poll_type VARCHAR(30);
BEGIN
    -- Lock decisions row to serialise concurrent votes and
    -- prevent race conditions with concurrent poll_type updates.
    SELECT poll_type INTO v_poll_type
    FROM   decisions
    WHERE  decision_id = NEW.decision_id
    FOR UPDATE;

    IF v_poll_type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE') THEN

        IF NEW.option_id IS NULL THEN
            RAISE EXCEPTION
                'Vote on a % decision must include an option_id.', v_poll_type;
        END IF;

        IF NEW.rating IS NOT NULL THEN
            RAISE EXCEPTION
                'Vote on a % decision must not include a rating.', v_poll_type;
        END IF;

        -- SINGLE_CHOICE: only one option-based vote per user per decision.
        -- MULTIPLE_CHOICE: skip - multiple different options are allowed.
        IF v_poll_type = 'SINGLE_CHOICE' THEN

            IF TG_OP = 'INSERT' THEN
                -- New row not yet in table; check all existing votes for this user+decision.
                IF EXISTS (
                    SELECT 1 FROM votes
                    WHERE  decision_id = NEW.decision_id
                      AND  user_id     = NEW.user_id
                ) THEN
                    RAISE EXCEPTION
                        'A SINGLE_CHOICE decision allows only one vote per user.';
                END IF;

            ELSIF TG_OP = 'UPDATE' THEN
                -- Row already exists in table; exclude it via OLD.vote_id
                -- so the row does not flag itself as a duplicate.
                IF EXISTS (
                    SELECT 1 FROM votes
                    WHERE  decision_id = NEW.decision_id
                      AND  user_id     = NEW.user_id
                      AND  vote_id    != OLD.vote_id
                ) THEN
                    RAISE EXCEPTION
                        'A SINGLE_CHOICE decision allows only one vote per user.';
                END IF;

            END IF;
        END IF;

    ELSIF v_poll_type = 'RATING' THEN

        IF NEW.rating IS NULL THEN
            RAISE EXCEPTION
                'Vote on a RATING decision must include a rating (1-5).';
        END IF;

        IF NEW.option_id IS NOT NULL THEN
            RAISE EXCEPTION
                'Vote on a RATING decision must not include an option_id.';
        END IF;

    ELSE
        RAISE EXCEPTION 'Unknown poll_type: %.', v_poll_type;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_validate_vote_poll_type ON votes;
CREATE TRIGGER trg_validate_vote_poll_type
    BEFORE INSERT OR UPDATE ON votes
    FOR EACH ROW
    EXECUTE FUNCTION validate_vote_poll_type();


-- ==========================================
-- TRIGGER 2: Prevent poll_type change after
--            votes have been cast
-- ==========================================
CREATE OR REPLACE FUNCTION prevent_poll_type_change()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.poll_type IS DISTINCT FROM OLD.poll_type THEN
        IF EXISTS (
            SELECT 1 FROM votes
            WHERE  decision_id = OLD.decision_id
            LIMIT  1
        ) THEN
            RAISE EXCEPTION
                'Cannot change poll_type on decision % - votes have already been cast.',
                OLD.decision_id;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_prevent_poll_type_change ON decisions;
CREATE TRIGGER trg_prevent_poll_type_change
    BEFORE UPDATE ON decisions
    FOR EACH ROW
    EXECUTE FUNCTION prevent_poll_type_change();


-- ==========================================
-- INDEXES
-- ==========================================

-- Existing indexes - preserved
CREATE INDEX IF NOT EXISTS idx_decisions_community ON decisions(community_id);
CREATE INDEX IF NOT EXISTS idx_options_decision    ON options(decision_id);
CREATE INDEX IF NOT EXISTS idx_votes_user          ON votes(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_decision   ON comments(decision_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user  ON notifications(user_id);

-- New indexes
CREATE INDEX IF NOT EXISTS idx_votes_option        ON votes(option_id);
CREATE INDEX IF NOT EXISTS idx_decisions_poll_type ON decisions(poll_type);

-- ==========================================
-- END OF SCHEMA
-- ==========================================
