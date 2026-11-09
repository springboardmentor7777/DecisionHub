
-- ==========================================
-- DECISIONHUB SAMPLE DATA
-- Database: decisionhub_db
-- DBMS: PostgreSQL
-- Total sample records: 42
-- ==========================================

-- 1. USERS (3 records)
INSERT INTO users (full_name, email, password_hash, role)
VALUES
('Arun Kumar', 'arun@example.com', 'hash_001', 'USER'),
('Priya Sharma', 'priya@example.com', 'hash_002', 'ADMIN'),
('Rahul Kumar', 'rahul@example.com', 'hash_003', 'MODERATOR');


-- 2. COMMUNITIES (3 records)
INSERT INTO communities (community_name, description, created_by)
VALUES
('Technology Community', 'A community for technology discussions', 1),
('Education Community', 'A community for educational decisions', 2),
('Sports Community', 'A community for sports-related discussions', 3);


-- 3. DECISIONS (3 records)
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


-- 4. OPTIONS (9 records)
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


-- 5. VOTES (9 records)
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


-- 6. COMMENTS (5 records)
INSERT INTO comments
(decision_id, user_id, parent_comment_id, comment_text)
VALUES
(1, 1, NULL, 'Python is a good choice for beginners.'),
(1, 2, 1, 'I agree. Python is easy to learn.'),
(2, 3, NULL, 'Online learning offers flexibility.'),
(2, 1, 3, 'Yes, it is convenient for students.'),
(3, 2, NULL, 'Cricket is very popular.');


-- 7. NOTIFICATIONS (5 records)
INSERT INTO notifications (user_id, message, is_read)
VALUES
(1, 'A new decision was created in Technology Community.', FALSE),
(2, 'Your decision received a new vote.', FALSE),
(3, 'A new comment was added to Best Team Sport.', TRUE),
(1, 'Someone replied to your comment.', FALSE),
(2, 'Your report status has been updated.', TRUE);


-- 8. REPORTS (5 records)
INSERT INTO reports
(reported_by, target_type, target_id, reason, status)
VALUES
(1, 'DECISION', 1, 'Inappropriate decision content.', 'PENDING'),
(2, 'COMMENT', 1, 'Inappropriate comment.', 'REVIEWED'),
(3, 'OPTION', 7, 'Incorrect option information.', 'RESOLVED'),
(1, 'COMMENT', 3, 'Spam comment.', 'REJECTED'),
(2, 'DECISION', 2, 'Misleading decision description.', 'PENDING');


-- ==========================================
-- END OF SAMPLE DATA
-- Total: 42 records
-- ==========================================
