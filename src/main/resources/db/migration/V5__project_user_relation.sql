ALTER TABLE projects
    RENAME COLUMN created_by TO user_id;

-- add foreign key constraint
ALTER TABLE projects
    ADD CONSTRAINT fk_projects_user
        FOREIGN KEY (user_id)
            REFERENCES users(id);