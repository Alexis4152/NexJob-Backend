-- ============================================================
-- NexJob - Migracion: verificacion de confianza del prestador
-- Agrega 3 banderas que solo un administrador puede autorizar desde el panel admin:
-- correo verificado, telefono verificado y perfil completo. (La identidad verificada ya
-- existia como is_verified). Alimentan la seccion "Confianza" del perfil publico.
-- Idempotente: se puede re-ejecutar sin error sobre una base ya migrada.
-- Ejecutar con: psql -U postgres -d nexjob -f db/10_migration_provider_verification.sql
-- ============================================================

ALTER TABLE provider_profiles ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE provider_profiles ADD COLUMN IF NOT EXISTS phone_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE provider_profiles ADD COLUMN IF NOT EXISTS profile_complete BOOLEAN NOT NULL DEFAULT FALSE;
