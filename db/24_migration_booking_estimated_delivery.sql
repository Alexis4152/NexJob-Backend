-- ============================================================
-- NexJob - Fecha estimada de entrega en la cotizacion
--
-- Al armar una cotizacion para un servicio "a cotizar", el prestador puede indicar (opcional)
-- para cuando estima terminar/entregar el trabajo -- ayuda al cliente a decidir si le sirve el
-- tiempo antes de aceptar. No es la fecha real de la visita, esa se agenda hasta que se acepta
-- la cotizacion (ver 23_migration_optional_visit_fields_cotizacion.sql).
--
-- Idempotente.
-- ============================================================

ALTER TABLE bookings ADD COLUMN IF NOT EXISTS estimated_delivery_date DATE;
