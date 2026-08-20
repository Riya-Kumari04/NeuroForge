-- Module 4: AI Requirements & Specification Studio
-- This schema adds specifications and specification versions tables
-- Also adds traceability columns to tasks table

-- Create specifications table
CREATE TABLE IF NOT EXISTS specifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    specification_key VARCHAR(30) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    current_version INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for specifications
CREATE INDEX IF NOT EXISTS idx_spec_key ON specifications(specification_key);
CREATE INDEX IF NOT EXISTS idx_status ON specifications(status);
CREATE INDEX IF NOT EXISTS idx_deleted ON specifications(deleted);

-- Create specification_versions table
CREATE TABLE IF NOT EXISTS specification_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    specification_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    description TEXT NOT NULL,
    user_stories TEXT,
    acceptance_criteria TEXT,
    functional_requirements TEXT,
    non_functional_requirements TEXT,
    status VARCHAR(30) NOT NULL,
    generated_by VARCHAR(255),
    generated_at TIMESTAMP,
    reviewed_by VARCHAR(255),
    reviewed_at TIMESTAMP,
    approved_by VARCHAR(255),
    approved_at TIMESTAMP,
    review_comments TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_specification_version FOREIGN KEY (specification_id) REFERENCES specifications(id) ON DELETE CASCADE,
    CONSTRAINT uk_specification_version UNIQUE (specification_id, version_number)
);

-- Create indexes for specification_versions
CREATE INDEX IF NOT EXISTS idx_specification_id ON specification_versions(specification_id);
CREATE INDEX IF NOT EXISTS idx_version_status ON specification_versions(status);

-- Add specification traceability columns to tasks table
ALTER TABLE tasks 
ADD COLUMN IF NOT EXISTS specification_id UUID,
ADD COLUMN IF NOT EXISTS specification_version_id UUID;

-- Create sequence for specification key generation
CREATE SEQUENCE IF NOT EXISTS specification_key_seq;
