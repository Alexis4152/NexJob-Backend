-- ============================================================
-- NexJob - Migracion: modalidad del servicio (a domicilio o en sitio del prestador)
-- Agrega la bandera at_client_location a service_offerings: indica si el prestador se
-- traslada al domicilio del cliente (true) o si el cliente debe acudir con el prestador
-- (false). Default TRUE porque la mayoria de los servicios del catalogo (carpinteria,
-- plomeria, electricidad, limpieza) son a domicilio.
-- Idempotente: se puede re-ejecutar sin error sobre una base ya migrada.
-- Ejecutar con: psql -U postgres -d nexjob -f db/07_migration_service_at_client_location.sql
-- ============================================================

ALTER TABLE service_offerings ADD COLUMN IF NOT EXISTS at_client_location BOOLEAN NOT NULL DEFAULT TRUE;
