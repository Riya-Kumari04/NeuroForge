-- ─── Module 3: Projects, Sprints, Tasks & Project Members Schema ─────────────
-- Depends on: users (Module 1), organizations, team_members (Module 2).
-- Note: the earlier Milestones feature has been retired in favor of
-- ProjectMember-based task assignment; there is no milestones table.

-- Projects table
CREATE TABLE IF NOT EXISTS projects (
    id              BIGSERIAL PRIMARY KEY,
    project_name    VARCHAR(150)   NOT NULL,
    description     TEXT,
    status          VARCHAR(30)    NOT NULL DEFAULT 'ACTIVE',
    start_date      TIMESTAMP,
    end_date        TIMESTAMP,
    organization_id BIGINT         NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- Sprints table
CREATE TABLE IF NOT EXISTS sprints (
    id          BIGSERIAL PRIMARY KEY,
    sprint_name VARCHAR(150)   NOT NULL,
    goal        TEXT,
    status      VARCHAR(30)    NOT NULL DEFAULT 'PLANNED',
    start_date  TIMESTAMP,
    end_date    TIMESTAMP,
    project_id  BIGINT         NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- Project members table — assigns a Module 2 team_member onto a project,
-- with a role scoped to that project (e.g. MEMBER, LEAD).
CREATE TABLE IF NOT EXISTS project_members (
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT         NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    team_member_id  BIGINT         NOT NULL REFERENCES team_members(id) ON DELETE CASCADE,
    role            VARCHAR(50)    NOT NULL DEFAULT 'MEMBER',
    UNIQUE (project_id, team_member_id)
);

-- Tasks table — assigned_to references a project_member, not a free-text
-- string, so an assignee is always a real member of the project.
CREATE TABLE IF NOT EXISTS tasks (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200)   NOT NULL,
    description     TEXT,
    priority        VARCHAR(20)    NOT NULL DEFAULT 'MEDIUM',
    status          VARCHAR(30)    NOT NULL DEFAULT 'TODO',
    project_id      BIGINT         NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    sprint_id       BIGINT         REFERENCES sprints(id) ON DELETE SET NULL,
    assigned_to_id  BIGINT         REFERENCES project_members(id) ON DELETE SET NULL,
    due_date        TIMESTAMP,
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_project_org        ON projects(organization_id);
CREATE INDEX IF NOT EXISTS idx_sprint_project      ON sprints(project_id);
CREATE INDEX IF NOT EXISTS idx_project_member_proj ON project_members(project_id);
CREATE INDEX IF NOT EXISTS idx_project_member_tm    ON project_members(team_member_id);
CREATE INDEX IF NOT EXISTS idx_task_project        ON tasks(project_id);
CREATE INDEX IF NOT EXISTS idx_task_sprint         ON tasks(sprint_id);
CREATE INDEX IF NOT EXISTS idx_task_assigned_to    ON tasks(assigned_to_id);
