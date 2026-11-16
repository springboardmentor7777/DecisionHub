-- ==========================================
-- DECISIONHUB DATABASE SCHEMA
-- Database: decisionhub_db
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

-- 3. COMMUNITY MEMBERS
CREATE TABLE IF NOT EXISTS community_members (
    membership_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    community_id BIGINT NOT NULL REFERENCES communities(community_id)
        ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(user_id)
        ON DELETE CASCADE,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (community_id, user_id)
);

-- 4. DECISIONS
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

-- 5. OPTIONS
CREATE TABLE IF NOT EXISTS options (
    option_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    decision_id BIGINT NOT NULL REFERENCES decisions(decision_id)
        ON DELETE CASCADE,
    option_text VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (decision_id, option_id)
);

-- 6. VOTES
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
    UNIQUE (decision_id, option_id, user_id)
);

-- 7. COMMENTS
CREATE TABLE IF NOT EXISTS comments (
    comment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    decision_id BIGINT NOT NULL REFERENCES decisions(decision_id)
        ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    parent_comment_id BIGINT REFERENCES comments(comment_id)
        ON DELETE CASCADE,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. NOTIFICATIONS
CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id)
        ON DELETE CASCADE,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. REPORTS
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
-- INDEXES FOR FASTER SEARCHING
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
-- END OF DATABASE SCHEMA
-- ==========================================

SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;

DROP TABLE IF EXISTS community_members;

SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
AND table_type = 'BASE TABLE'
ORDER BY table_name;

SELECT
    table_name,
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
ORDER BY table_name, ordinal_position;

SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS referenced_table,
    ccu.column_name AS referenced_column
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
AND tc.table_schema = 'public';

SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS referenced_table,
    ccu.column_name AS referenced_column
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
    ON tc.constraint_catalog = kcu.constraint_catalog
    AND tc.constraint_schema = kcu.constraint_schema
    AND tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage ccu
    ON tc.constraint_catalog = ccu.constraint_catalog
    AND tc.constraint_schema = ccu.constraint_schema
    AND tc.constraint_name = ccu.constraint_name
    AND tc.table_schema = ccu.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema = 'public'
ORDER BY tc.table_name, kcu.constraint_name,
         kcu.ordinal_position;

SELECT
    conname AS constraint_name,
    conrelid::regclass AS table_name,
    pg_get_constraintdef(oid) AS definition
FROM pg_constraint
WHERE contype = 'f'
  AND connamespace = 'public'::regnamespace
ORDER BY conrelid::regclass::text, conname;		

SELECT
    tc.table_name,
    tc.constraint_type,
    tc.constraint_name,
    string_agg(kcu.column_name, ', ' ORDER BY kcu.ordinal_position) AS columns
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
    ON tc.constraint_catalog = kcu.constraint_catalog
    AND tc.constraint_schema = kcu.constraint_schema
    AND tc.constraint_name = kcu.constraint_name
    AND tc.table_name = kcu.table_name
WHERE tc.table_schema = 'public'
    AND tc.constraint_type IN ('PRIMARY KEY', 'UNIQUE')
GROUP BY
    tc.table_name,
    tc.constraint_type,
    tc.constraint_name
ORDER BY tc.table_name, tc.constraint_type;

SELECT
    tc.table_name,
    tc.constraint_name,
    cc.check_clause
FROM information_schema.table_constraints tc
JOIN information_schema.check_constraints cc
    ON tc.constraint_name = cc.constraint_name
    AND tc.constraint_schema = cc.constraint_schema
WHERE tc.constraint_type = 'CHECK'
  AND tc.table_schema = 'public'
ORDER BY tc.table_name;

INSERT INTO users (full_name, email, password_hash, role)
VALUES
('Arun Kumar', 'arun@example.com', 'hash_001', 'USER'),
('Priya Sharma', 'priya@example.com', 'hash_002', 'ADMIN'),
('Rahul Kumar', 'rahul@example.com', 'hash_003', 'MODERATOR');
SELECT * FROM users ORDER BY user_id;
INSERT INTO communities (community_name, description, created_by)
VALUES
('Technology Community', 'A community for technology discussions', 1),
('Education Community', 'A community for educational decisions', 2),
('Sports Community', 'A community for sports-related discussions', 3);
SELECT * FROM communities ORDER BY community_id;
INSERT INTO decisions
(community_id, created_by, title, description, status, visibility, ends_at)
VALUES
(1, 1, 'Best Programming Language',
 'Which programming language should beginners learn first?',
 'OPEN', 'PUBLIC', '2026-12-01 23:59:00'),

(2, 2, 'Best Learning Method',
 'Which learning method is more effective for students?',
 'OPEN', 'PUBLIC', '2026-12-05 23:59:00'),

(3, 3, 'Best Team Sport',
 'Which team sport is most popular?',
 'DRAFT', 'PRIVATE', '2026-12-10 23:59:00');
 SELECT * FROM decisions ORDER BY decision_id;

 INSERT INTO options (decision_id, option_text)
VALUES
(1, 'Python'),
(1, 'Java'),
(1, 'JavaScript'),
(2, 'Online Learning'),
(2, 'Classroom Learning'),
(2, 'Hybrid Learning'),
(3, 'Cricket'),
(3, 'Football'),
(3, 'Basketball');
SELECT * FROM options ORDER BY option_id;

INSERT INTO votes (decision_id, option_id, user_id)
VALUES
(1, 1, 1),
(1, 2, 2),
(1, 3, 3),
(2, 4, 1),
(2, 5, 2),
(2, 6, 3),
(3, 7, 1),
(3, 8, 2),
(3, 9, 3);
SELECT * FROM votes ORDER BY vote_id;

INSERT INTO comments
(decision_id, user_id, parent_comment_id, comment_text)
VALUES
(1, 1, NULL, 'Python is a good choice for beginners.'),
(1, 2, 1, 'I agree. Python is easy to learn.'),
(2, 3, NULL, 'Online learning offers flexibility.'),
(2, 1, 3, 'Yes, it is convenient for students.'),
(3, 2, NULL, 'Cricket is very popular.');
SELECT * FROM comments ORDER BY comment_id;

INSERT INTO notifications (user_id, message, is_read)
VALUES
(1, 'A new decision was created in Technology Community.', FALSE),
(2, 'Your decision received a new vote.', FALSE),
(3, 'A new comment was added to Best Team Sport.', TRUE),
(1, 'Someone replied to your comment.', FALSE),
(2, 'Your report status has been updated.', TRUE);
SELECT * FROM notifications ORDER BY notification_id;

INSERT INTO reports
(reported_by, target_type, target_id, reason, status)
VALUES
(1, 'DECISION', 1, 'Inappropriate decision content.', 'PENDING'),
(2, 'COMMENT', 1, 'Inappropriate comment.', 'REVIEWED'),
(3, 'OPTION', 7, 'Incorrect option information.', 'RESOLVED'),
(1, 'COMMENT', 3, 'Spam comment.', 'REJECTED'),
(2, 'DECISION', 2, 'Misleading decision description.', 'PENDING');
SELECT * FROM reports ORDER BY report_id;

SELECT
    d.decision_id,
    d.title AS decision_title,
    c.community_name,
    u.full_name AS created_by,
    d.status,
    o.option_text
FROM decisions d
JOIN communities c
    ON d.community_id = c.community_id
JOIN users u
    ON d.created_by = u.user_id
JOIN options o
    ON d.decision_id = o.decision_id
ORDER BY d.decision_id, o.option_id;
SELECT
    v.vote_id,
    d.title AS decision_title,
    o.option_text AS selected_option,
    u.full_name AS voter_name,
    v.voted_at
FROM votes v
JOIN decisions d
    ON v.decision_id = d.decision_id
JOIN options o
    ON v.option_id = o.option_id
   AND v.decision_id = o.decision_id
JOIN users u
    ON v.user_id = u.user_id
ORDER BY v.vote_id;

SELECT
    c.comment_id,
    d.title AS decision_title,
    u.full_name AS commenter,
    c.comment_text,
    c.parent_comment_id,
    p.comment_text AS parent_comment
FROM comments c
JOIN decisions d
    ON c.decision_id = d.decision_id
JOIN users u
    ON c.user_id = u.user_id
LEFT JOIN comments p
    ON c.parent_comment_id = p.comment_id
ORDER BY c.comment_id;

SELECT
    n.notification_id,
    u.full_name AS recipient,
    u.email,
    n.message,
    n.is_read,
    n.created_at
FROM notifications n
JOIN users u
    ON n.user_id = u.user_id
ORDER BY n.notification_id;

SELECT
    r.report_id,
    u.full_name AS reported_by,
    u.email,
    r.target_type,
    r.target_id,
    r.reason,
    r.status,
    r.created_at
FROM reports r
JOIN users u
    ON r.reported_by = u.user_id
ORDER BY r.report_id;

SELECT 'users' AS table_name, COUNT(*) AS total_records FROM users
UNION ALL
SELECT 'communities', COUNT(*) FROM communities
UNION ALL
SELECT 'decisions', COUNT(*) FROM decisions
UNION ALL
SELECT 'options', COUNT(*) FROM options
UNION ALL
SELECT 'votes', COUNT(*) FROM votes
UNION ALL
SELECT 'comments', COUNT(*) FROM comments
UNION ALL
SELECT 'notifications', COUNT(*) FROM notifications
UNION ALL
SELECT 'reports', COUNT(*) FROM reports;

BEGIN;

ALTER TABLE votes
DROP CONSTRAINT votes_decision_id_option_id_user_id_key;

ALTER TABLE votes
ADD CONSTRAINT votes_one_vote_per_decision
UNIQUE (decision_id, user_id);

COMMIT;

SELECT
    conname,
    pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'votes'::regclass
  AND contype = 'u';

 CREATE TABLE roles (
    role_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL
);
INSERT INTO roles (role_name)
VALUES
    ('USER'),
    ('MODERATOR'),
    ('ADMIN');
	SELECT * FROM roles ORDER BY role_id;
	ALTER TABLE users
	ADD COLUMN role_id BIGINT;
	UPDATE users
	SET role_id = roles.role_id
	FROM roles
	WHERE users.role = roles.role_name;
	SELECT
    u.user_id,
    u.full_name,
    u.role,
    u.role_id,
    r.role_name
FROM users u
JOIN roles r
ON u.role_id = r.role_id
ORDER BY u.user_id;
ALTER TABLE users
ADD CONSTRAINT fk_users_roles
FOREIGN KEY (role_id)
REFERENCES roles(role_id);
ALTER TABLE users
ALTER COLUMN role_id SET NOT NULL;
SELECT
    conname,
    pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'users'::regclass
AND contype = 'f';
SELECT
    u.user_id,
    u.full_name,
    u.email,
    r.role_name
FROM users u
JOIN roles r
    ON u.role_id = r.role_id
ORDER BY u.user_id;
ALTER TABLE users
DROP COLUMN role;
SELECT * FROM users;
SELECT
    conname,
    pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'votes'::regclass
ORDER BY conname;
SELECT
    COUNT(*) AS total_votes,
    COUNT(option_id) AS option_votes,
    COUNT(*) - COUNT(option_id) AS null_options
FROM votes;
SELECT
    decision_id,
    user_id,
    option_id,
    COUNT(*) AS total
FROM votes
GROUP BY decision_id, user_id, option_id
HAVING COUNT(*) > 1;
SELECT
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'decisions'
ORDER BY ordinal_position;
SELECT
    table_name,
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name IN ('decisions', 'votes')
ORDER BY table_name, ordinal_position;
SELECT
    conname AS constraint_name,
    pg_get_constraintdef(oid) AS definition
FROM pg_constraint
WHERE conrelid = 'votes'::regclass
ORDER BY conname;
BEGIN;

ALTER TABLE decisions
ADD COLUMN poll_type VARCHAR(30)
NOT NULL DEFAULT 'SINGLE_CHOICE';

ALTER TABLE decisions
ADD CONSTRAINT decisions_poll_type_check
CHECK (
    poll_type IN (
        'SINGLE_CHOICE',
        'MULTIPLE_CHOICE',
        'RATING'
    )
);

COMMIT;
SELECT decision_id, title, poll_type
FROM decisions
ORDER BY decision_id;
SELECT
    decision_id,
    user_id,
    option_id,
    COUNT(*) AS vote_count
FROM votes
GROUP BY decision_id, user_id, option_id
HAVING COUNT(*) > 1;
BEGIN;

-- Remove the old restriction: one vote per decision
ALTER TABLE votes
DROP CONSTRAINT votes_one_vote_per_decision;

-- Allow NULL option_id for rating-based polls
ALTER TABLE votes
ALTER COLUMN option_id DROP NOT NULL;

-- Add rating column
ALTER TABLE votes
ADD COLUMN rating INTEGER;

-- A vote must contain either an option or a rating
ALTER TABLE votes
ADD CONSTRAINT votes_option_or_rating_check
CHECK (
    (option_id IS NOT NULL AND rating IS NULL)
    OR
    (option_id IS NULL AND rating IS NOT NULL)
);

-- Rating must be between 1 and 5
ALTER TABLE votes
ADD CONSTRAINT votes_rating_range_check
CHECK (
    rating IS NULL OR rating BETWEEN 1 AND 5
);

-- Prevent duplicate option selections by the same user
ALTER TABLE votes
ADD CONSTRAINT votes_unique_option_selection
UNIQUE (decision_id, user_id, option_id);

-- Allow only one rating per user per decision
CREATE UNIQUE INDEX votes_one_rating_per_decision
ON votes (decision_id, user_id)
WHERE rating IS NOT NULL;

COMMIT;
SELECT
    vote_id,
    decision_id,
    option_id,
    user_id,
    rating
FROM votes
ORDER BY vote_id;
CREATE OR REPLACE FUNCTION validate_vote_type()
RETURNS TRIGGER AS $$
DECLARE
    v_poll_type VARCHAR(30);
BEGIN
    -- Lock the decision row to prevent concurrent voting
    SELECT poll_type
    INTO v_poll_type
    FROM decisions
    WHERE decision_id = NEW.decision_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Decision does not exist';
    END IF;

    -- Single-choice poll
    IF v_poll_type = 'SINGLE_CHOICE' THEN

        IF NEW.option_id IS NULL OR NEW.rating IS NOT NULL THEN
            RAISE EXCEPTION
                'Single-choice polls require an option selection';
        END IF;

        IF EXISTS (
            SELECT 1
            FROM votes
            WHERE decision_id = NEW.decision_id
              AND user_id = NEW.user_id
              AND vote_id <> NEW.vote_id
        ) THEN
            RAISE EXCEPTION
                'Only one option is allowed in a single-choice poll';
        END IF;

    -- Multiple-choice poll
    ELSIF v_poll_type = 'MULTIPLE_CHOICE' THEN

        IF NEW.option_id IS NULL OR NEW.rating IS NOT NULL THEN
            RAISE EXCEPTION
                'Multiple-choice polls require option selections';
        END IF;

    -- Rating-based poll
    ELSIF v_poll_type = 'RATING' THEN

        IF NEW.option_id IS NOT NULL OR NEW.rating IS NULL THEN
            RAISE EXCEPTION
                'Rating polls require a rating and no option';
        END IF;

    ELSE
        RAISE EXCEPTION 'Invalid poll type';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_validate_vote_type
BEFORE INSERT OR UPDATE ON votes
FOR EACH ROW
EXECUTE FUNCTION validate_vote_type();
INSERT INTO votes (decision_id, option_id, user_id)
VALUES (1, 2, 1);
UPDATE decisions
SET poll_type = 'MULTIPLE_CHOICE'
WHERE decision_id = 2;
INSERT INTO votes (decision_id, option_id, user_id)
VALUES (2, 5, 1);
UPDATE decisions
SET poll_type = 'RATING'
WHERE decision_id = 3;
BEGIN;

DELETE FROM votes
WHERE decision_id = 3
AND user_id = 1;

INSERT INTO votes (decision_id, user_id, rating)
VALUES (3, 1, 4);

COMMIT;
SELECT
    d.decision_id,
    d.title,
    d.poll_type,
    v.vote_id,
    v.user_id,
    v.option_id,
    v.rating
FROM decisions d
LEFT JOIN votes v
    ON d.decision_id = v.decision_id
ORDER BY d.decision_id, v.user_id, v.vote_id;
DELETE FROM votes
WHERE decision_id = 3
AND option_id IS NOT NULL;
SELECT
    d.title,
    d.poll_type,
    v.user_id,
    v.option_id,
    v.rating
FROM decisions d
LEFT JOIN votes v
    ON d.decision_id = v.decision_id
ORDER BY d.decision_id, v.user_id;
SELECT
    table_name,
    column_name,
    data_type
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name IN ('users', 'roles')
ORDER BY table_name, ordinal_position;
SELECT
    u.user_id,
    u.full_name,
    u.email,
    u.role_id,
    r.role_name
FROM users u
JOIN roles r
    ON u.role_id = r.role_id
ORDER BY u.user_id;
SELECT
    decision_id,
    title,
    poll_type
FROM decisions
ORDER BY decision_id;
SELECT
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'votes'
ORDER BY ordinal_position;
SELECT
    conname,
    pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'public.votes'::regclass
ORDER BY conname;
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename = 'votes';
  SELECT
    trigger_name,
    event_manipulation,
    action_timing,
    action_statement
FROM information_schema.triggers
WHERE event_object_schema = 'public'
  AND event_object_table = 'votes';
  SELECT
    routine_name,
    routine_type,
    data_type
FROM information_schema.routines
WHERE routine_schema = 'public'
  AND routine_name = 'validate_vote_type';
  SELECT
    table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;
SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS referenced_table,
    ccu.column_name AS referenced_column,
    rc.delete_rule
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name
   AND tc.constraint_schema = kcu.constraint_schema
JOIN information_schema.constraint_column_usage ccu
    ON ccu.constraint_name = tc.constraint_name
   AND ccu.constraint_schema = tc.constraint_schema
JOIN information_schema.referential_constraints rc
    ON rc.constraint_name = tc.constraint_name
   AND rc.constraint_schema = tc.constraint_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema = 'public'
ORDER BY tc.table_name, kcu.column_name;
SELECT
    conrelid::regclass AS table_name,
    conname AS constraint_name,
    confrelid::regclass AS referenced_table,
    pg_get_constraintdef(oid) AS relationship
FROM pg_constraint
WHERE contype = 'f'
  AND connamespace = 'public'::regnamespace
ORDER BY conrelid::regclass::text;
SELECT
    conrelid::regclass AS table_name,
    conname AS constraint_name,
    pg_get_constraintdef(oid) AS constraint_definition
FROM pg_constraint
WHERE contype IN ('p', 'u')
  AND connamespace = 'public'::regnamespace
ORDER BY conrelid::regclass::text, contype;
SELECT
    conrelid::regclass AS table_name,
    conname AS constraint_name,
    pg_get_constraintdef(oid) AS constraint_definition
FROM pg_constraint
WHERE contype = 'c'
  AND connamespace = 'public'::regnamespace
ORDER BY conrelid::regclass::text, conname;
SELECT
    table_name,
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public'
ORDER BY table_name, ordinal_position;
SELECT
    table_name,
    column_name,
    is_identity,
    identity_generation,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND column_name IN (
      'user_id',
      'community_id',
      'decision_id',
      'option_id',
      'vote_id',
      'comment_id',
      'notification_id',
      'report_id',
      'role_id'
  )
ORDER BY table_name, column_name;
SELECT
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;
SELECT pg_get_functiondef(
    'public.validate_vote_type()'::regprocedure
);
SELECT
    trigger_name,
    event_manipulation,
    action_timing,
    action_statement
FROM information_schema.triggers
WHERE event_object_schema = 'public'
  AND event_object_table = 'decisions';
  CREATE OR REPLACE FUNCTION prevent_poll_type_change_with_votes()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.poll_type IS DISTINCT FROM OLD.poll_type
       AND EXISTS (
           SELECT 1
           FROM votes
           WHERE decision_id = OLD.decision_id
       )
    THEN
        RAISE EXCEPTION
            'Cannot change poll type after votes have been submitted';
    END IF;

    RETURN NEW;
END;
$$;
CREATE TRIGGER trg_prevent_poll_type_change
BEFORE UPDATE OF poll_type
ON decisions
FOR EACH ROW
EXECUTE FUNCTION prevent_poll_type_change_with_votes();
SELECT
    trigger_name,
    event_manipulation,
    action_timing
FROM information_schema.triggers
WHERE event_object_schema = 'public'
  AND event_object_table = 'decisions'
ORDER BY trigger_name;
BEGIN;
UPDATE decisions
SET poll_type = 'RATING'
WHERE decision_id = 1;
ROLLBACK;
SELECT decision_id, title, poll_type
FROM decisions
ORDER BY decision_id;
SELECT
    table_name,
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public'
ORDER BY table_name, ordinal_position;
SELECT
    tc.table_name,
    tc.constraint_name,
    CASE tc.constraint_type
        WHEN 'PRIMARY KEY' THEN 'PRIMARY KEY'
        WHEN 'FOREIGN KEY' THEN 'FOREIGN KEY'
        WHEN 'UNIQUE' THEN 'UNIQUE'
        WHEN 'CHECK' THEN 'CHECK'
    END AS constraint_type,
    pg_get_constraintdef(c.oid) AS constraint_definition
FROM information_schema.table_constraints tc
JOIN pg_constraint c
    ON c.conname = tc.constraint_name
JOIN pg_class t
    ON t.oid = c.conrelid
WHERE tc.table_schema = 'public'
  AND t.relname = tc.table_name
ORDER BY tc.table_name, tc.constraint_type, tc.constraint_name;
SELECT
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;
SELECT
    trigger_name,
    event_object_table,
    action_timing,
    event_manipulation,
    action_statement
FROM information_schema.triggers
WHERE trigger_schema = 'public'
ORDER BY event_object_table, trigger_name;
SELECT
    proname,
    pg_get_functiondef(oid)
FROM pg_proc
WHERE proname IN (
    'validate_vote_type',
    'prevent_poll_type_change_with_votes'
);
