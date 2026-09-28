-- ============================================================
-- NexJob - Verificacion de correo (opcional, no bloqueante)
--
-- El usuario (cliente o prestador) puede verificar su correo desde su perfil, en cualquier
-- momento despues de registrarse; nunca es un requisito para usar la plataforma, solo una senal
-- de confianza que se muestra en su perfil. Ver EmailVerificationCode / UserServiceImpl.
--
-- Idempotente.
-- ============================================================

ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS email_verification_codes (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id),
    code                VARCHAR(100) NOT NULL UNIQUE,
    attempts            INTEGER NOT NULL DEFAULT 0,
    expires_at          TIMESTAMP NOT NULL,
    used_at             TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_email_verification_codes_user_id ON email_verification_codes (user_id);
