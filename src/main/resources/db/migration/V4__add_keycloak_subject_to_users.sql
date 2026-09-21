ALTER TABLE users
ADD COLUMN keycloak_subject VARCHAR(255);

ALTER TABLE users
ADD CONSTRAINT uk_users_keycloak_subject
UNIQUE (keycloak_subject);

-- For now it is nullable because you may already have old users in your development database.

-- The idea is:
--
-- users
--
-- id | name   | email               | keycloak_subject
-- ---+--------+---------------------+-------------------------
-- 12 | Milena | milena@example.com  | 7d3a2e4c-...

-- The keycloak_subject corresponds to the JWT:
-- {
--   "sub": "7d3a2e4c-..."
-- }

-- OBS! We should identify authenticated users by sub, not by username or email, because
-- usernames/emails can potentially change while the subject is intended to identify the identity.