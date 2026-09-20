-- ============================================================
-- NexJob - Datos adicionales del cliente en su cuenta
--
-- Hoy "users" solo guarda nombre/apellido/telefono; el cliente tiene
-- que volver a escribir su ciudad/codigo postal cada vez que agenda
-- un servicio o pide cotizaciones. Estas columnas los guardan en su
-- cuenta para reutilizarlos, mas edad y foto de perfil (ambos
-- opcionales).
--
-- Idempotente.
-- ============================================================

ALTER TABLE users ADD COLUMN IF NOT EXISTS city VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS postal_code VARCHAR(5);
ALTER TABLE users ADD COLUMN IF NOT EXISTS age INTEGER;
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(255);
