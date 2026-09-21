-- ============================================================
-- NexJob - Dias y horario de servicio del prestador
--
-- Campos informativos que el propio prestador declara en su perfil
-- (no se calculan de reservas reales, es una declaracion propia,
-- igual que años de experiencia o bio). Ambos opcionales.
--
-- Idempotente.
-- ============================================================

ALTER TABLE provider_profiles ADD COLUMN IF NOT EXISTS service_days VARCHAR(20)
    CHECK (service_days IN ('LUN_VIE','LUN_SAB','LUN_DOM','SAB_DOM'));
ALTER TABLE provider_profiles ADD COLUMN IF NOT EXISTS service_hours VARCHAR(20)
    CHECK (service_hours IN ('H08_17','H09_18','H08_20','H24'));
