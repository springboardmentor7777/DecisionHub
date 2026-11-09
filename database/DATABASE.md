
# DecisionHub - Database Documentation

## 1. Overview

DecisionHub is a collaborative decision-making and community polling platform.

The database is designed using PostgreSQL to manage users, communities, decisions, voting, comments, notifications, and reports.

## 2. Database Details

- Database Name: decisionhub_db
- Database Management System: PostgreSQL
- Schema: public
- Total Tables: 8

## 3. Database Tables

| S.No | Table Name | Description |
|------|------------|-------------|
| 1 | users | Stores user account and role information |
| 2 | communities | Stores community details |
| 3 | decisions | Stores decisions created by users |
| 4 | options | Stores available options for each decision |
| 5 | votes | Stores users' voting records |
| 6 | comments | Stores comments and replies |
| 7 | notifications | Stores user notifications |
| 8 | reports | Stores reports submitted by users |

## 4. Relationships

- A user can create multiple communities.
- A community can contain multiple decisions.
- A user can create multiple decisions.
- A decision can have multiple options.
- Users can vote on decision options.
- A decision can have multiple comments.
- Comments can have replies.
- A user can receive multiple notifications.
- Users can submit multiple reports.

## 5. Constraints

The database uses:

- Primary Keys for unique record identification.
- Foreign Keys to maintain relationships between tables.
- UNIQUE constraints to prevent duplicate email addresses and ensure
  each user can vote only once per decision.
- CHECK constraints to validate roles, decision status, visibility, and report status.
- ON DELETE CASCADE and ON DELETE SET NULL to manage related records.

## 6. Indexes

The following indexes are created for frequently accessed columns:

- idx_decisions_community
- idx_options_decision
- idx_votes_user
- idx_comments_decision
- idx_notifications_user

## 7. Database Files

| File | Purpose |
|------|---------|
| schema.sql | Creates database tables, constraints, and indexes |
| sample_data.sql | Inserts 42 sample records |
| verification.sql | Contains database verification and JOIN queries |
| DATABASE.md | Documents the database structure |

## 8. Sample Data Summary

| Table | Records |
|-------|---------|
| Users | 3 |
| Communities | 3 |
| Decisions | 3 |
| Options | 9 |
| Votes | 9 |
| Comments | 5 |
| Notifications | 5 |
| Reports | 5 |
| **Total** | **42** |

## 9. Notes
- The voting system follows a single-choice voting rule.
- Each user can submit only one vote per decision.
- The UNIQUE constraint (decision_id, user_id) enforces this rule.
- The composite foreign key ensures that the selected option belongs
  to the corresponding decision.
- The reports table uses target_type and target_id to identify
  reported content.

## 10. Status

Database schema creation, sample data insertion, constraint verification, and JOIN testing have been completed.
