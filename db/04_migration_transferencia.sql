-- ============================================================
-- NexJob - Migracion: metodo de pago TRANSFERENCIA + comprobante
-- Agrega TRANSFERENCIA a los metodos de pago aceptados y una columna para
-- guardar la foto del comprobante que el cliente adjunta al liberar el pago.
-- Idempotente: se puede re-ejecutar sin error sobre una base ya migrada.
-- Ejecutar con: psql -U postgres -d nexjob -f db/04_migration_transferencia.sql
-- ============================================================

ALTER TABLE bookings DROP CONSTRAINT IF EXISTS bookings_payment_method_check;
ALTER TABLE bookings ADD CONSTRAINT bookings_payment_method_check
    CHECK (payment_method IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA'));

ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_method_check;
ALTER TABLE payments ADD CONSTRAINT payments_method_check
    CHECK (method IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA'));

ALTER TABLE payments ADD COLUMN IF NOT EXISTS proof_url VARCHAR(500);
