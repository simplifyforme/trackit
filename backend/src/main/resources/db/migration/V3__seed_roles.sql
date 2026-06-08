-- Seed the two baseline roles.
-- The dev admin user is created at runtime by DevDataSeeder (profile=dev only).
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');
