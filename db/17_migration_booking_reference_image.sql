-- ============================================================
-- NexJob - Foto de referencia del cliente al contratar
--
-- El cliente puede adjuntar una foto (ej. el mueble que le gusta, la
-- fuga que tiene) al solicitar el servicio, para complementar la
-- descripcion y las preguntas especificas de la categoria. Opcional.
--
-- Idempotente.
-- ============================================================

ALTER TABLE bookings ADD COLUMN IF NOT EXISTS reference_image_url VARCHAR(500);
