
-- ==========================================
-- DECISIONHUB DATABASE VERIFICATION
-- Database: decisionhub_db
-- DBMS: PostgreSQL
-- ==========================================

-- 1. VERIFY ALL TABLES
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;


-- 2. VERIFY TABLE COLUMNS
SELECT
    table_name,
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
ORDER BY table_name, ordinal_position;


-- 3. VERIFY FOREIGN KEYS
SELECT
    conname AS constraint_name,
    conrelid::regclass AS table_name,
    pg_get_constraintdef(oid) AS definition
FROM pg_constraint
WHERE contype = 'f'
  AND connamespace = 'public'::regnamespace
ORDER BY conrelid::regclass::text, conname;


-- 4. VERIFY PRIMARY KEY AND UNIQUE CONSTRAINTS
SELECT
    tc.table_name,
    tc.constraint_type,
    tc.constraint_name,
    string_agg(
        kcu.column_name,
        ', ' ORDER BY kcu.ordinal_position
    ) AS columns
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


-- 5. VERIFY CHECK CONSTRAINTS
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


-- 6. VERIFY INDEXES
SELECT
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;


-- 7. VERIFY SAMPLE RECORD COUNTS
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


-- 8. VERIFY DECISIONS AND THEIR OPTIONS
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


-- 9. VERIFY VOTES
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
    ON v.decision_id = o.decision_id
   AND v.option_id = o.option_id
JOIN users u
    ON v.user_id = u.user_id
ORDER BY v.vote_id;


-- 10. VERIFY COMMENTS AND REPLIES
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


-- 11. VERIFY NOTIFICATIONS
SELECT
    n.notification_id,
    u.full_name AS recipient,
    n.message,
    n.is_read,
    n.created_at
FROM notifications n
JOIN users u
    ON n.user_id = u.user_id
ORDER BY n.notification_id;


-- 12. VERIFY REPORTS
SELECT
    r.report_id,
    u.full_name AS reported_by,
    r.target_type,
    r.target_id,
    r.reason,
    r.status
FROM reports r
JOIN users u
    ON r.reported_by = u.user_id
ORDER BY r.report_id;


-- ==========================================
-- END OF VERIFICATION
-- ==========================================
