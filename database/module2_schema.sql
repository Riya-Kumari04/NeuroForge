-- ─── Module 2: Organization & Team Workspace Schema ──────────────────────────

-- Organizations table
CREATE TABLE IF NOT EXISTS organizations (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)       NOT NULL,
    slug        VARCHAR(60)        NOT NULL UNIQUE,
    industry    VARCHAR(100),
    size        VARCHAR(50),
    plan        VARCHAR(50)        NOT NULL DEFAULT 'FREE',
    description TEXT,
    logo_url    VARCHAR(500),
    created_by  BIGINT             REFERENCES users(id) ON DELETE SET NULL,
    created_at  TIMESTAMP          NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP          NOT NULL DEFAULT NOW()
);

-- Teams table
CREATE TABLE IF NOT EXISTS teams (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100)   NOT NULL,
    description     TEXT,
    organization_id BIGINT         NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    lead_id         BIGINT         REFERENCES users(id) ON DELETE SET NULL,
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- Team members table (org-level membership)
CREATE TABLE IF NOT EXISTS team_members (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    team_id         BIGINT         REFERENCES teams(id) ON DELETE SET NULL,
    organization_id BIGINT         NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    role            VARCHAR(50)    NOT NULL DEFAULT 'DEVELOPER',
    joined_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, organization_id)
);

-- Invites table
CREATE TABLE IF NOT EXISTS invites (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255)   NOT NULL,
    token           VARCHAR(36)    NOT NULL UNIQUE,
    organization_id BIGINT         NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    invited_by      BIGINT         REFERENCES users(id) ON DELETE SET NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    role            VARCHAR(50)    NOT NULL DEFAULT 'DEVELOPER',
    expires_at      TIMESTAMP      NOT NULL DEFAULT (NOW() + INTERVAL '7 days'),
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    responded_at    TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_org_slug ON organizations(slug);
CREATE INDEX IF NOT EXISTS idx_team_org ON teams(organization_id);
CREATE INDEX IF NOT EXISTS idx_member_org ON team_members(organization_id);
CREATE INDEX IF NOT EXISTS idx_member_user ON team_members(user_id);
CREATE INDEX IF NOT EXISTS idx_invite_org ON invites(organization_id);
CREATE INDEX IF NOT EXISTS idx_invite_token ON invites(token);
CREATE INDEX IF NOT EXISTS idx_invite_status ON invites(status);
