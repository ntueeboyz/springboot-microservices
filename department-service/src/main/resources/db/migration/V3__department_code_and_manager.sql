ALTER TABLE department.departments ADD COLUMN IF NOT EXISTS code VARCHAR(40);
ALTER TABLE department.departments ADD COLUMN IF NOT EXISTS manager_email VARCHAR(200);
UPDATE department.departments
SET code = LOWER(REGEXP_REPLACE(name, '[^a-zA-Z0-9]', '', 'g'))
WHERE code IS NULL;

ALTER TABLE department.departments ALTER COLUMN code SET NOT NULL;
ALTER TABLE department.departments ADD CONSTRAINT uk_department_code UNIQUE (code);
