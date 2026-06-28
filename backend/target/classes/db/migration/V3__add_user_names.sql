-- ============================================================
-- ForgeMind — Authentication Schema Updates
-- Migration  : V3__add_user_names.sql
-- Sprint     : 2 / Task 2.2
-- Description: Adds first_name and last_name to users table
-- ============================================================

ALTER TABLE users 
ADD COLUMN first_name VARCHAR(100) NOT NULL DEFAULT '',
ADD COLUMN last_name VARCHAR(100) NOT NULL DEFAULT '';
