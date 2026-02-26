ALTER TABLE project_members
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'VIEWER';

ALTER TABLE project_members
    ADD CONSTRAINT chk_project_members_role
        CHECK (role IN ('MANAGER','CONTRIBUTOR','VIEWER'));