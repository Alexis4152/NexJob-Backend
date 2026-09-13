-- ============================================================
-- NexJob - Migracion: endurecimiento de recuperacion de contrasena
-- Agrega el contador de intentos fallidos por codigo (para bloquearlo tras varios
-- intentos incorrectos). El codigo en si ahora se guarda hasheado (SHA-256) desde
-- el codigo Java, no requiere cambio de esquema adicional (columna token ya soporta
-- el largo del hash).
-- Idempotente: se puede re-ejecutar sin error sobre una base ya migrada.
-- Ejecutar con: psql -U postgres -d nexjob -f db/06_migration_password_reset_hardening.sql
-- ============================================================

ALTER TABLE password_reset_tokens ADD COLUMN IF NOT EXISTS attempts INTEGER NOT NULL DEFAULT 0;

-- Los codigos generados antes de este cambio quedaban en texto plano; se invalidan
-- (no se pueden migrar a hash porque no conocemos el valor original en texto plano).
UPDATE password_reset_tokens SET used_at = NOW() WHERE used_at IS NULL;
