-- V15: Add missing audit columns to buildings table
-- The Building entity extends BaseEntity which requires created_by and updated_by

ALTER TABLE buildings ADD COLUMN created_by BIGINT;
ALTER TABLE buildings ADD COLUMN updated_by BIGINT;
