-- ============================================================
-- NexJob - Migracion: recuperacion de contrasena
-- Agrega la tabla password_reset_tokens usada por "olvide mi contrasena".
-- Idempotente: se puede re-ejecutar sin error sobre una base ya migrada.
-- Ejecutar con: psql -U postgres -d nexjob -f db/05_migration_password_reset.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id),
    token               VARCHAR(100) NOT NULL UNIQUE,
    expires_at          TIMESTAMP NOT NULL,
    used_at             TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
