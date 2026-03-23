UPDATE task_assignments
SET is_pinned = false
WHERE is_pinned IS NULL;

UPDATE task_assignments
SET is_favorited = false
WHERE is_favorited IS NULL;

ALTER TABLE task_assignments
    ALTER COLUMN is_pinned SET NOT NULL;
ALTER TABLE task_assignments
    ALTER COLUMN is_pinned SET DEFAULT false;

ALTER TABLE task_assignments
    ALTER COLUMN is_favorited SET NOT NULL;
ALTER TABLE task_assignments
    ALTER COLUMN is_favorited SET DEFAULT false;

UPDATE project_members
SET is_pinned = false
WHERE is_pinned IS NULL;

UPDATE project_members
SET is_favorited = false
WHERE is_favorited IS NULL;

ALTER TABLE project_members
    ALTER COLUMN is_pinned SET NOT NULL;
ALTER TABLE project_members
    ALTER COLUMN is_pinned SET DEFAULT false;

ALTER TABLE project_members
    ALTER COLUMN is_favorited SET NOT NULL;
ALTER TABLE project_members
    ALTER COLUMN is_favorited SET DEFAULT false;