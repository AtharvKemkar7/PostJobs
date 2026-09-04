CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

CREATE TABLE jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    recruiter_user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    requirements TEXT,
    responsibilities TEXT,
    location_city VARCHAR(100),
    location_state VARCHAR(100),
    location_country VARCHAR(100),
    employment_type VARCHAR(20) NOT NULL CHECK (employment_type IN ('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'FREELANCE')),
    work_mode VARCHAR(10) NOT NULL CHECK (work_mode IN ('REMOTE', 'ONSITE', 'HYBRID')),
    experience_min_years INTEGER DEFAULT 0,
    experience_max_years INTEGER,
    salary_min DECIMAL(12,2),
    salary_max DECIMAL(12,2),
    salary_currency VARCHAR(3) DEFAULT 'USD',
    category VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'CLOSED', 'EXPIRED')),
    published_at TIMESTAMP,
    closes_at TIMESTAMP,
    expired_at TIMESTAMP,
    application_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_salary CHECK (salary_min IS NULL OR salary_max IS NULL OR salary_min <= salary_max),
    CONSTRAINT chk_experience CHECK (experience_min_years IS NULL OR experience_max_years IS NULL OR experience_min_years <= experience_max_years)
);

CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_company ON jobs(company_id);
CREATE INDEX idx_jobs_recruiter ON jobs(recruiter_user_id);
CREATE INDEX idx_jobs_employment_type ON jobs(employment_type);
CREATE INDEX idx_jobs_work_mode ON jobs(work_mode);
CREATE INDEX idx_jobs_country ON jobs(location_country);
CREATE INDEX idx_jobs_category ON jobs(category);
CREATE INDEX idx_jobs_published_at ON jobs(published_at);
CREATE INDEX idx_jobs_status_published ON jobs(status, published_at DESC);
CREATE INDEX idx_jobs_salary ON jobs(salary_min, salary_max);
CREATE INDEX idx_jobs_experience ON jobs(experience_min_years, experience_max_years);
CREATE INDEX idx_jobs_title_trgm ON jobs USING gin(title gin_trgm_ops);
CREATE INDEX idx_jobs_description_trgm ON jobs USING gin(description gin_trgm_ops);

CREATE TABLE job_skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    skill_name VARCHAR(100) NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_job_skills UNIQUE (job_id, skill_name)
);
CREATE INDEX idx_job_skills_job ON job_skills(job_id);
CREATE INDEX idx_job_skills_name ON job_skills(skill_name);

CREATE TABLE saved_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    candidate_user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_saved_jobs UNIQUE (job_id, candidate_user_id)
);
CREATE INDEX idx_saved_jobs_candidate ON saved_jobs(candidate_user_id);
CREATE INDEX idx_saved_jobs_job ON saved_jobs(job_id);
