-- ============================================================
-- NexJob - Paso intermedio "Cotizacion aceptada" antes de Aceptado
--
-- Hoy, al aceptar el cliente una cotizacion, la contratacion salta
-- directo a ACEPTADO. Esto agrega un estado intermedio
-- (COTIZACION_ACEPTADA): el cliente confirma el precio, pero el
-- prestador todavia debe confirmar para agendar la visita antes de
-- que la contratacion llegue a ACEPTADO -- igual que en el flujo de
-- precio fijo, donde el prestador siempre da el visto bueno final.
--
-- Idempotente.
-- ============================================================

ALTER TABLE bookings DROP CONSTRAINT IF EXISTS bookings_status_check;
ALTER TABLE bookings ADD CONSTRAINT bookings_status_check
    CHECK (status IN ('SOLICITADO','COTIZADO','COTIZACION_ACEPTADA','ACEPTADO','EN_PROCESO','CONCLUIDO','APROBADO','RECHAZADO','CANCELADO'));
