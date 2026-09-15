-- ============================================================
-- NexJob - Migracion: codigo postal del prestador.
-- Ya viene incluida en 01_schema.sql para instalaciones nuevas;
-- este script es para bases de datos que ya existian antes de esto.
-- Requiere haber corrido antes 06_postal_codes.sql para que el
-- lookup de codigo postal -> coordenadas funcione (ver
-- PostalCodeLookupService). Idempotente: ALTER ... IF NOT EXISTS.
-- ============================================================

ALTER TABLE provider_profiles ADD COLUMN IF NOT EXISTS postal_code VARCHAR(10);
