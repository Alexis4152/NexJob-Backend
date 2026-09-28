-- ============================================================
-- NexJob - Direccion/fecha de visita opcionales para servicios "a cotizar"
--
-- Para un servicio de precio fijo o por hora, el cliente agenda una visita concreta desde el
-- inicio. Para un servicio "a cotizar" (ej. mueble a la medida), el prestador normalmente
-- fabrica el mueble en su taller y solo visita al cliente para entregar/instalar una vez que la
-- cotizacion ya se acepto -- pedirle al cliente una direccion y fecha de visita antes de que
-- exista siquiera un precio acordado no tiene sentido. Estas columnas dejan de ser obligatorias
-- a nivel de base de datos; BookingServiceImpl sigue exigiendolas para servicios que no son
-- "a cotizar".
--
-- Idempotente.
-- ============================================================

ALTER TABLE bookings ALTER COLUMN address_line DROP NOT NULL;
ALTER TABLE bookings ALTER COLUMN city DROP NOT NULL;
ALTER TABLE bookings ALTER COLUMN scheduled_at DROP NOT NULL;
