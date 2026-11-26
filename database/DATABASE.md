# DecisionHub - Database Documentation

## 1. Overview

DecisionHub is a collaborative decision-making and community polling platform.

The database is designed using PostgreSQL (hosted on Neon) to manage roles, users, communities, decisions, options, voting, comments, notifications, and reports.

## 2. Database Details

- **Database Management System:** PostgreSQL
- **Schema:** `public`
- **Migration Tool:** Flyway (`backend/src/main/resources/db/migration/`)
- **Total Tables:** 9

## 3. Database Tables

| S.No | Table Name | Description |
|------|------------|-------------|
| 1 | `roles` | Stores system and user role definitions (e.g., ADMIN, MEMBER) |
| 2 | `users` | Stores user account details linked to roles |
| 3 | `communities` | Stores community details and metadata |
| 4 | `decisions` | Stores polls/decisions (`SINGLE_CHOICE`, `MULTIPLE_CHOICE`, `RATING`) |
| 5 | `options` | Stores available choices or rating options for each decision |
| 6 | `votes` | Stores users' voting records and ratings |
| 7 | `comments` | Stores comments and nested replies on decisions |
| 8 | `notifications` | Stores user notifications |
| 9 | `reports` | Stores moderation reports submitted by users |

## 4. Relationships

- A role can be assigned to multiple users.
- A user can create multiple communities.
- A community can contain multiple decisions.
- A user can create multiple decisions.
- A decision can have multiple options.
- Users can submit votes/ratings on decision options.
- A decision can have multiple comments, and comments can have replies.
- A user can receive multiple notifications.
- Users can submit multiple reports.

## 5. Constraints

- **Primary Keys:** Unique identification (`BIGSERIAL` / `BIGINT`) for all records.
- **Foreign Keys:** Enforce referential integrity across all 9 tables (`ON DELETE CASCADE` / `ON DELETE SET NULL`).
- **UNIQUE Constraints:** Prevent duplicate emails/role names and enforce voting uniqueness rules per decision/option.
- **CHECK Constraints:** Validate poll types (`SINGLE_CHOICE`, `MULTIPLE_CHOICE`, `RATING`), decision status, visibility, and report status.

## 6. Schema & Backend Source Files

| Location / File | Purpose |
|-----------------|---------|
| `backend/src/main/resources/db/migration/` | Flyway SQL migration scripts defining the 9-table schema and constraints |
| `backend/src/main/java/com/decisionhub/entity/` | Spring Data JPA entity classes mapped to the 9 database tables |
| `backend/src/main/java/com/decisionhub/dto/` | Request/Response DTOs (`PollRequest`, `PollResponse`, etc.) |
| `DATABASE.md` | Project database structure documentation |

## 7. Poll & Voting Rules

- **Supported Poll Types:** `SINGLE_CHOICE`, `MULTIPLE_CHOICE`, and `RATING`.
- **Single-Choice Polls:** Each user can select only one option per decision.
- **Multiple-Choice Polls:** Users can select multiple options within the same decision without duplicate votes on the same option.
- **Rating Polls:** Users submit numeric ratings for options/decisions.
- **Moderation:** The `reports` table uses `target_type` and `target_id` to identify reported decisions or comments.

## 8. Status

- 9-table PostgreSQL schema deployed and verified on Neon via Flyway migrations.
- JPA entities, repositories, and poll DTOs (`PollRequest`, `PollResponse`) synced on `team-one`.
- REST API endpoints verified via Postman.