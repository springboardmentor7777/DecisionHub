-- Align the upstream initial schema with the existing users table name.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'users'
          AND column_name = 'full_name'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'users'
          AND column_name = 'name'
    ) THEN
        ALTER TABLE users RENAME COLUMN full_name TO name;
    END IF;
END
$$;

ALTER TABLE users ADD COLUMN IF NOT EXISTS bio VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS interests VARCHAR(255);

CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO categories (name) VALUES
    ('Career'), ('Education'), ('Technology'),
    ('Travel'), ('Finance'), ('Lifestyle')
ON CONFLICT (name) DO NOTHING;

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
