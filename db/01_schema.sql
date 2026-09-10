-- ============================================================
-- NexJob - Esquema de base de datos (PostgreSQL)
-- Idempotente: usa IF NOT EXISTS, se puede re-ejecutar sin romper nada.
-- ============================================================

-- ============ ROLES Y USUARIOS ============

CREATE TABLE IF NOT EXISTS roles (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(30) NOT NULL UNIQUE CHECK (name IN ('ADMIN', 'CLIENT', 'PROVIDER')),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS users (
    id                  BIGSERIAL PRIMARY KEY,
    email               VARCHAR(150) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    phone               VARCHAR(30),
    role_id             BIGINT NOT NULL REFERENCES roles(id),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_user_id  BIGINT REFERENCES users(id),
    updated_at          TIMESTAMP,
    updated_by_user_id  BIGINT REFERENCES users(id),
    deleted_at          TIMESTAMP,
    deleted_by_user_id  BIGINT REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);

-- ============ CATEGORIAS DE SERVICIO ============

CREATE TABLE IF NOT EXISTS categories (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    slug                VARCHAR(120) NOT NULL UNIQUE,
    description         VARCHAR(500),
    icon                VARCHAR(10),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_user_id  BIGINT REFERENCES users(id),
    updated_at          TIMESTAMP,
    updated_by_user_id  BIGINT REFERENCES users(id),
    deleted_at          TIMESTAMP,
    deleted_by_user_id  BIGINT REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_categories_slug ON categories(slug);

-- ============ PERFILES DE PRESTADOR DE SERVICIO ============

CREATE TABLE IF NOT EXISTS provider_profiles (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL UNIQUE REFERENCES users(id),
    business_name       VARCHAR(150) NOT NULL,
    bio                 VARCHAR(1000),
    years_experience    INTEGER,
    city                VARCHAR(100) NOT NULL,
    profile_image_url   VARCHAR(500),
    average_rating      NUMERIC(3,2) NOT NULL DEFAULT 0,
    total_reviews       INTEGER NOT NULL DEFAULT 0,
    is_verified         BOOLEAN NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_user_id  BIGINT REFERENCES users(id),
    updated_at          TIMESTAMP,
    updated_by_user_id  BIGINT REFERENCES users(id),
    deleted_at          TIMESTAMP,
    deleted_by_user_id  BIGINT REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_provider_profiles_user_id ON provider_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_provider_profiles_city ON provider_profiles(city);

CREATE TABLE IF NOT EXISTS provider_categories (
    provider_profile_id BIGINT NOT NULL REFERENCES provider_profiles(id),
    category_id         BIGINT NOT NULL REFERENCES categories(id),
    PRIMARY KEY (provider_profile_id, category_id)
);
CREATE INDEX IF NOT EXISTS idx_provider_categories_category_id ON provider_categories(category_id);

-- ============ SERVICIOS OFRECIDOS ============

CREATE TABLE IF NOT EXISTS service_offerings (
    id                          BIGSERIAL PRIMARY KEY,
    provider_profile_id         BIGINT NOT NULL REFERENCES provider_profiles(id),
    category_id                 BIGINT NOT NULL REFERENCES categories(id),
    title                       VARCHAR(200) NOT NULL,
    description                 VARCHAR(2000),
    price                       NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    price_type                  VARCHAR(20) NOT NULL CHECK (price_type IN ('FIJO', 'POR_HORA', 'COTIZACION')),
    estimated_duration_value    INTEGER,
    estimated_duration_unit     VARCHAR(20) NOT NULL DEFAULT 'MINUTOS' CHECK (estimated_duration_unit IN ('MINUTOS', 'DIAS', 'SEMANAS', 'MESES')),
    is_active                   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                  TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_user_id          BIGINT REFERENCES users(id),
    updated_at                  TIMESTAMP,
    updated_by_user_id          BIGINT REFERENCES users(id),
    deleted_at                  TIMESTAMP,
    deleted_by_user_id          BIGINT REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_service_offerings_provider_id ON service_offerings(provider_profile_id);
CREATE INDEX IF NOT EXISTS idx_service_offerings_category_id ON service_offerings(category_id);

CREATE TABLE IF NOT EXISTS service_images (
    id                  BIGSERIAL PRIMARY KEY,
    service_offering_id BIGINT NOT NULL REFERENCES service_offerings(id),
    url                 VARCHAR(500) NOT NULL,
    sort_order          INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_service_images_service_id ON service_images(service_offering_id);

-- ============ CONTRATACIONES ============

CREATE TABLE IF NOT EXISTS bookings (
    id                      BIGSERIAL PRIMARY KEY,
    folio                   VARCHAR(30) NOT NULL UNIQUE,
    client_id               BIGINT NOT NULL REFERENCES users(id),
    provider_profile_id     BIGINT NOT NULL REFERENCES provider_profiles(id),
    service_offering_id     BIGINT NOT NULL REFERENCES service_offerings(id),
    agreed_price            NUMERIC(12,2) NOT NULL CHECK (agreed_price >= 0),
    description             VARCHAR(1000),
    address_line            VARCHAR(255) NOT NULL,
    city                    VARCHAR(100) NOT NULL,
    scheduled_at            TIMESTAMP NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'SOLICITADO'
                              CHECK (status IN ('SOLICITADO','ACEPTADO','EN_PROCESO','CONCLUIDO','APROBADO','RECHAZADO','CANCELADO')),
    payment_method          VARCHAR(20) NOT NULL CHECK (payment_method IN ('EFECTIVO','TARJETA','TRANSFERENCIA')),
    cancelled_reason        VARCHAR(500),
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_user_id      BIGINT REFERENCES users(id),
    updated_at              TIMESTAMP,
    updated_by_user_id      BIGINT REFERENCES users(id),
    deleted_at              TIMESTAMP,
    deleted_by_user_id      BIGINT REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_bookings_folio ON bookings(folio);
CREATE INDEX IF NOT EXISTS idx_bookings_client_id ON bookings(client_id);
CREATE INDEX IF NOT EXISTS idx_bookings_provider_id ON bookings(provider_profile_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_bookings_created_at ON bookings(created_at);

CREATE TABLE IF NOT EXISTS booking_status_history (
    id                  BIGSERIAL PRIMARY KEY,
    booking_id          BIGINT NOT NULL REFERENCES bookings(id),
    previous_status     VARCHAR(20),
    new_status          VARCHAR(20) NOT NULL,
    changed_by_user_id  BIGINT REFERENCES users(id),
    changed_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    note                VARCHAR(500)
);
CREATE INDEX IF NOT EXISTS idx_booking_status_history_booking_id ON booking_status_history(booking_id);

CREATE TABLE IF NOT EXISTS booking_evidences (
    id                  BIGSERIAL PRIMARY KEY,
    booking_id          BIGINT NOT NULL REFERENCES bookings(id),
    url                 VARCHAR(500) NOT NULL,
    description         VARCHAR(500),
    created_by_user_id  BIGINT REFERENCES users(id),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_booking_evidences_booking_id ON booking_evidences(booking_id);

CREATE TABLE IF NOT EXISTS payments (
    id                  BIGSERIAL PRIMARY KEY,
    booking_id          BIGINT NOT NULL UNIQUE REFERENCES bookings(id),
    method              VARCHAR(20) NOT NULL CHECK (method IN ('EFECTIVO','TARJETA','TRANSFERENCIA')),
    amount              NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    status              VARCHAR(20) NOT NULL CHECK (status IN ('PENDIENTE','LIBERADO','RECHAZADO')),
    card_last4          VARCHAR(4),
    proof_url           VARCHAR(500),
    transaction_id      VARCHAR(60),
    released_at         TIMESTAMP,
    released_by_user_id BIGINT REFERENCES users(id),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_payments_booking_id ON payments(booking_id);

CREATE TABLE IF NOT EXISTS reviews (
    id                      BIGSERIAL PRIMARY KEY,
    booking_id              BIGINT NOT NULL UNIQUE REFERENCES bookings(id),
    client_id               BIGINT NOT NULL REFERENCES users(id),
    provider_profile_id     BIGINT NOT NULL REFERENCES provider_profiles(id),
    rating                  INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment                 VARCHAR(1000),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_reviews_provider_id ON reviews(provider_profile_id);

-- ============ MODULO DE AYUDA ============

CREATE TABLE IF NOT EXISTS support_tickets (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT REFERENCES users(id),
    contact_email       VARCHAR(150) NOT NULL,
    subject             VARCHAR(200) NOT NULL,
    category            VARCHAR(30) NOT NULL CHECK (category IN ('PROBLEMA_TECNICO','QUEJA_SERVICIO','SUGERENCIA','OTRO')),
    message             VARCHAR(2000) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'ABIERTO' CHECK (status IN ('ABIERTO','EN_REVISION','RESUELTO','CERRADO')),
    admin_response      VARCHAR(2000),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    resolved_at         TIMESTAMP,
    resolved_by_user_id BIGINT REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_support_tickets_status ON support_tickets(status);
CREATE INDEX IF NOT EXISTS idx_support_tickets_user_id ON support_tickets(user_id);

-- ============ CONFIGURACION DE PLATAFORMA ============

CREATE TABLE IF NOT EXISTS platform_config (
    id                  BIGSERIAL PRIMARY KEY,
    platform_name       VARCHAR(150) NOT NULL,
    legal_name          VARCHAR(150),
    contact_email       VARCHAR(150),
    contact_phone       VARCHAR(30),
    logo_url            VARCHAR(500),
    primary_color       VARCHAR(10) NOT NULL DEFAULT '#155DEA',
    secondary_color     VARCHAR(10) NOT NULL DEFAULT '#0F172A',
    welcome_message     VARCHAR(500),
    footer_text         VARCHAR(500),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by_user_id  BIGINT REFERENCES users(id)
);

-- ============ CONFIGURACION DE CORREO (SMTP) ============

CREATE TABLE IF NOT EXISTS email_config (
    id                  BIGSERIAL PRIMARY KEY,
    enabled             BOOLEAN NOT NULL DEFAULT FALSE,
    smtp_host           VARCHAR(255) NOT NULL DEFAULT 'smtp.gmail.com',
    smtp_port           INTEGER NOT NULL DEFAULT 587,
    smtp_username       VARCHAR(150),
    smtp_password       VARCHAR(255),
    from_address        VARCHAR(150),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by_user_id  BIGINT REFERENCES users(id)
);
