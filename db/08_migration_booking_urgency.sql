-- ============================================================
-- NexJob - Migracion: urgencia de la contratacion
-- Agrega "que tan pronto necesita el cliente el servicio" (URGENTE / PRONTO /
-- PROGRAMADO) a cada contratacion, capturado en el formulario de "Contratar
-- servicio". Sirve para que el prestador priorice sus solicitudes.
-- Las contrataciones existentes quedan como PROGRAMADO (valor neutro, no se
-- puede inferir su urgencia real retroactivamente).
-- Idempotente: se puede re-ejecutar sin error sobre una base ya migrada.
-- Ejecutar con: psql -U postgres -d nexjob -f db/08_migration_booking_urgency.sql
-- ============================================================

ALTER TABLE bookings ADD COLUMN IF NOT EXISTS urgency VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADO';

ALTER TABLE bookings DROP CONSTRAINT IF EXISTS bookings_urgency_check;
ALTER TABLE bookings ADD CONSTRAINT bookings_urgency_check CHECK (urgency IN ('URGENTE','PRONTO','PROGRAMADO'));
