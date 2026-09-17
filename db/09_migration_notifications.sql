-- ============================================================
-- NexJob - Migracion: notificaciones in-app
-- Avisos dirigidos a un usuario (cliente o prestador) sobre cambios en sus
-- contrataciones: nueva solicitud, aceptada, rechazada, en proceso, concluida,
-- aprobada/pago liberado, cancelada, resena recibida.
-- Idempotente: se puede re-ejecutar sin error sobre una base ya migrada.
-- Ejecutar con: psql -U postgres -d nexjob -f db/09_migration_notifications.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS notifications (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id),
    booking_id  BIGINT REFERENCES bookings(id),
    type        VARCHAR(40) NOT NULL,
    title       VARCHAR(150) NOT NULL,
    body        VARCHAR(500) NOT NULL,
    link        VARCHAR(300),
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications(user_id) WHERE is_read = FALSE;
