-- ==========================================
-- DECISIONHUB SAMPLE DATA
-- Database: decisionhub_db
-- DBMS: PostgreSQL
-- Total records: 43
--
-- !! IMPORTANT - READ BEFORE RUNNING !!
--
-- FRESH DATABASE REQUIRED:
--   This script assumes a completely empty database where schema.sql
--   has already been executed successfully. Run schema.sql first.
--
-- ID ASSUMPTIONS:
--   All tables use GENERATED ALWAYS AS IDENTITY sequences that start
--   at 1 on a fresh database. The IDs used in FK references below are
--   based strictly on insert order:
--     roles:       USER=1, ADMIN=2, MODERATOR=3
--     users:       Arun=1, Priya=2, Rahul=3
--     communities: Technology=1, Education=2, Sports=3
--     decisions:   BestLang=1, BestMethod=2, RateCommunity=3
--     options:     Python=1, Java=2, JavaScript=3,
--                  Online=4, Classroom=5, Hybrid=6
--   If any other data has been inserted before this script, the
--   sequences will be at different values and FK references will break.
--
-- RUN ONCE ONLY:
--   This script is NOT safe to run more than once. Unique constraints
--   will cause failures on a second run:
--     * roles.role_name UNIQUE
--     * users.email UNIQUE
--     * options UNIQUE(decision_id, option_id)
--     * votes UNIQUE(decision_id, user_id, option_id)
--     * votes partial index (decision_id, user_id) WHERE rating IS NOT NULL
--   To reload sample data: drop and recreate the database, then run
--   schema.sql followed by this file.
-- ==========================================


-- 1. ROLES (3 records)
-- Must be inserted before users (FK dependency).
INSERT INTO roles (role_name) VALUES
('USER'),
('ADMIN'),
('MODERATOR');


-- 2. USERS (3 records)
INSERT INTO users (full_name, email, password_hash, role_id)
VALUES
('Arun Kumar',   'arun@example.com',  'hash_001', 1),
('Priya Sharma', 'priya@example.com', 'hash_002', 2),
('Rahul Kumar',  'rahul@example.com', 'hash_003', 3);


-- 3. COMMUNITIES (3 records)
INSERT INTO communities (community_name, description, created_by)
VALUES
('Technology Community', 'A community for technology discussions',     1),
('Education Community',  'A community for educational decisions',      2),
('Sports Community',     'A community for sports-related discussions', 3);


-- 4. DECISIONS (3 records)
-- One of each poll type.
INSERT INTO decisions
    (community_id, created_by, title, description, poll_type, status, visibility, ends_at)
VALUES
(1, 1,
 'Best Programming Language',
 'Which programming language should beginners learn first?',
 'SINGLE_CHOICE', 'OPEN', 'PUBLIC', '2026-12-01 23:59:00'),

(2, 2,
 'Best Learning Methods',
 'Select all learning methods that work well for you.',
 'MULTIPLE_CHOICE', 'OPEN', 'PUBLIC', '2026-12-05 23:59:00'),

(3, 3,
 'Rate Our Community Experience',
 'How would you rate your overall experience? (1 = Poor, 5 = Excellent)',
 'RATING', 'OPEN', 'PUBLIC', '2026-12-10 23:59:00');


-- 5. OPTIONS (6 records)
-- Only decisions 1 and 2 have options.
-- Decision 3 is a RATING poll - options are not applicable.
INSERT INTO options (decision_id, option_text)
VALUES
(1, 'Python'),              -- option_id 1
(1, 'Java'),                -- option_id 2
(1, 'JavaScript'),          -- option_id 3
(2, 'Online Learning'),     -- option_id 4
(2, 'Classroom Learning'),  -- option_id 5
(2, 'Hybrid Learning');     -- option_id 6


-- 6. VOTES (10 records)
-- Demonstrates all three poll types.
-- trg_validate_vote_poll_type fires on each INSERT and validates correctness.
INSERT INTO votes (decision_id, option_id, user_id, rating)
VALUES
-- Decision 1: SINGLE_CHOICE - one option per user (trigger enforced)
(1, 1, 1, NULL),   -- Arun  -> Python
(1, 2, 2, NULL),   -- Priya -> Java
(1, 3, 3, NULL),   -- Rahul -> JavaScript

-- Decision 2: MULTIPLE_CHOICE - multiple options per user allowed
(2, 4, 1, NULL),   -- Arun  -> Online Learning
(2, 5, 1, NULL),   -- Arun  -> Classroom Learning (second option, same decision - valid)
(2, 4, 2, NULL),   -- Priya -> Online Learning
(2, 6, 3, NULL),   -- Rahul -> Hybrid Learning

-- Decision 3: RATING - option_id IS NULL; rating 1-5
(3, NULL, 1, 5),   -- Arun  -> 5 (Excellent)
(3, NULL, 2, 4),   -- Priya -> 4
(3, NULL, 3, 3);   -- Rahul -> 3


-- 7. COMMENTS (5 records)
INSERT INTO comments (decision_id, user_id, parent_comment_id, comment_text)
VALUES
(1, 1, NULL, 'Python is a good choice for beginners.'),
(1, 2, 1,    'I agree. Python is easy to learn.'),
(2, 3, NULL, 'Online learning offers great flexibility.'),
(2, 1, 3,    'Yes, it is convenient for students.'),
(3, 2, NULL, 'The community has been very welcoming!');


-- 8. NOTIFICATIONS (5 records)
INSERT INTO notifications (user_id, message, is_read)
VALUES
(1, 'A new decision was created in Technology Community.', FALSE),
(2, 'Your decision received a new vote.',                  FALSE),
(3, 'A new comment was added to your decision.',           TRUE),
(1, 'Someone replied to your comment.',                    FALSE),
(2, 'Your report status has been updated.',                TRUE);


-- 9. REPORTS (5 records)
-- target_id refers to the PK of the reported object in its own table.
INSERT INTO reports (reported_by, target_type, target_id, reason, status)
VALUES
(1, 'DECISION', 1, 'Inappropriate decision content.',  'PENDING'),
(2, 'COMMENT',  1, 'Inappropriate comment.',           'REVIEWED'),
(3, 'OPTION',   6, 'Incorrect option information.',    'RESOLVED'),
(1, 'COMMENT',  3, 'Spam comment.',                    'REJECTED'),
(2, 'DECISION', 2, 'Misleading decision description.', 'PENDING');


-- ==========================================
-- END OF SAMPLE DATA
-- Total: 43 records
-- ==========================================
