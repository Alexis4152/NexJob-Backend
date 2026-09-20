-- ============================================================
-- NexJob - Migracion: "Solicitar varias cotizaciones" (punto 18)
-- El cliente describe lo que necesita una vez, se envia automaticamente a hasta 5
-- prestadores recomendados de esa categoria, y compara precio/disponibilidad antes de
-- elegir uno (lo que crea una contratacion real en `bookings`).
-- Idempotente: se puede re-ejecutar sin error sobre una base ya migrada.
-- Ejecutar con: psql -U postgres -d nexjob -f db/12_migration_quote_requests.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS quote_requests (
    id                      BIGSERIAL PRIMARY KEY,
    client_id               BIGINT NOT NULL REFERENCES users(id),
    category_id             BIGINT NOT NULL REFERENCES categories(id),
    description             VARCHAR(1000),
    address_line            VARCHAR(255) NOT NULL,
    city                    VARCHAR(100) NOT NULL,
    scheduled_at            TIMESTAMP NOT NULL,
    payment_method          VARCHAR(20) NOT NULL CHECK (payment_method IN ('EFECTIVO','TARJETA','TRANSFERENCIA')),
    urgency                 VARCHAR(20) NOT NULL CHECK (urgency IN ('URGENTE','PRONTO','PROGRAMADO')),
    status                  VARCHAR(20) NOT NULL DEFAULT 'ABIERTA' CHECK (status IN ('ABIERTA','CERRADA','EXPIRADA')),
    resulting_booking_id    BIGINT REFERENCES bookings(id),
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_user_id      BIGINT REFERENCES users(id),
    updated_at              TIMESTAMP,
    updated_by_user_id      BIGINT REFERENCES users(id),
    deleted_at              TIMESTAMP,
    deleted_by_user_id      BIGINT REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_quote_requests_client_id ON quote_requests(client_id);
CREATE INDEX IF NOT EXISTS idx_quote_requests_status ON quote_requests(status);
CREATE INDEX IF NOT EXISTS idx_quote_requests_created_at ON quote_requests(created_at);

CREATE TABLE IF NOT EXISTS quote_request_recipients (
    id                      BIGSERIAL PRIMARY KEY,
    quote_request_id        BIGINT NOT NULL REFERENCES quote_requests(id),
    provider_profile_id     BIGINT NOT NULL REFERENCES provider_profiles(id),
    declined                BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (quote_request_id, provider_profile_id)
);
CREATE INDEX IF NOT EXISTS idx_qrr_quote_request_id ON quote_request_recipients(quote_request_id);
CREATE INDEX IF NOT EXISTS idx_qrr_provider_id ON quote_request_recipients(provider_profile_id);

CREATE TABLE IF NOT EXISTS quotes (
    id                      BIGSERIAL PRIMARY KEY,
    quote_request_id        BIGINT NOT NULL REFERENCES quote_requests(id),
    provider_profile_id     BIGINT NOT NULL REFERENCES provider_profiles(id),
    service_offering_id     BIGINT NOT NULL REFERENCES service_offerings(id),
    price                   NUMERIC(12,2) NOT NULL CHECK (price > 0),
    available_at            TIMESTAMP NOT NULL,
    note                    VARCHAR(500),
    status                  VARCHAR(20) NOT NULL DEFAULT 'ENVIADA' CHECK (status IN ('ENVIADA','ELEGIDA','DESCARTADA')),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (quote_request_id, provider_profile_id)
);
CREATE INDEX IF NOT EXISTS idx_quotes_quote_request_id ON quotes(quote_request_id);
