-- ============================================================
-- NexJob - Cotizacion de ida y vuelta para servicios "a cotizar"
--
-- Hoy, al contratar cualquier servicio, el precio se copia directo
-- de service_offerings.price aunque el servicio sea "a cotizar"
-- (ej. un mueble a la medida). Esto agrega un paso intermedio, solo
-- para esos servicios: el prestador arma un desglose real de
-- materiales/costos (nuevo estado COTIZADO) y el cliente lo acepta
-- o lo rechaza antes de que la contratacion avance a ACEPTADO.
--
-- Idempotente.
-- ============================================================

ALTER TABLE bookings DROP CONSTRAINT IF EXISTS bookings_status_check;
ALTER TABLE bookings ADD CONSTRAINT bookings_status_check
    CHECK (status IN ('SOLICITADO','COTIZADO','ACEPTADO','EN_PROCESO','CONCLUIDO','APROBADO','RECHAZADO','CANCELADO'));

ALTER TABLE bookings ADD COLUMN IF NOT EXISTS quote_items_json TEXT;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS quote_note VARCHAR(1000);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS quote_total NUMERIC(12,2);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS quote_sent_at TIMESTAMP;
