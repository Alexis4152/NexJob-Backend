-- ============================================================
-- NexJob - Script de inicializacion (schema + seed)
-- Combina 01_schema.sql + 02_seed.sql para correrlo de una sola vez
-- contra la base de datos 'nexjob' ya creada en PostgreSQL.
-- Idempotente: usa IF NOT EXISTS / ON CONFLICT, se puede re-ejecutar sin duplicar datos.
-- ============================================================

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

-- ============================================================
-- NexJob - Datos semilla (demo)
-- Password admin: Admin123!   |   Password cliente/prestadores demo: Cliente123!
-- Idempotente: usa ON CONFLICT / WHERE NOT EXISTS, se puede re-ejecutar sin duplicar datos.
-- ============================================================

INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('CLIENT') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('PROVIDER') ON CONFLICT (name) DO NOTHING;

-- ============ USUARIOS DEMO ============

INSERT INTO users (email, password_hash, first_name, last_name, phone, role_id, is_active)
SELECT 'admin@nexjob-demo.com', '$2b$10$nwHCWxjudXLk2xshHHyrpeyPamrIJ/TVxRCWEYHETsAcYf.koWqCO',
       'Admin', 'NexJob', '5555550100', r.id, TRUE
FROM roles r WHERE r.name = 'ADMIN'
AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@nexjob-demo.com');

INSERT INTO users (email, password_hash, first_name, last_name, phone, role_id, is_active)
SELECT 'cliente@demo.com', '$2b$10$1H9H17NH8WIGTbIVRurtQeXxVeCWExR0PNJCwCPHmJginz/g7K4fG',
       'Ana', 'Gonzalez', '5555550200', r.id, TRUE
FROM roles r WHERE r.name = 'CLIENT'
AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'cliente@demo.com');

INSERT INTO users (email, password_hash, first_name, last_name, phone, role_id, is_active)
SELECT v.email, '$2b$10$1H9H17NH8WIGTbIVRurtQeXxVeCWExR0PNJCwCPHmJginz/g7K4fG', v.first_name, v.last_name, v.phone, r.id, TRUE
FROM (VALUES
 ('carpinteria@demo.com', 'Juan', 'Perez', '5555550301'),
 ('plomeria@demo.com', 'Maria', 'Lopez', '5555550302'),
 ('electricidad@demo.com', 'Carlos', 'Ruiz', '5555550303'),
 ('limpieza@demo.com', 'Rosa', 'Martinez', '5555550304')
) AS v(email, first_name, last_name, phone)
JOIN roles r ON r.name = 'PROVIDER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = v.email);

-- ============ CATEGORIAS DE SERVICIO ============

INSERT INTO categories (name, slug, description, icon) VALUES
 ('Carpinteria', 'carpinteria', 'Muebles a medida, closets, puertas y reparaciones en madera', '🪚'),
 ('Plomeria', 'plomeria', 'Fugas, instalaciones y mantenimiento hidraulico', '🚰'),
 ('Electricidad', 'electricidad', 'Instalaciones electricas, cortos y mantenimiento', '💡'),
 ('Limpieza del hogar', 'limpieza-del-hogar', 'Limpieza profunda y de mantenimiento para el hogar', '🧹'),
 ('Jardineria', 'jardineria', 'Poda, diseno y mantenimiento de jardines', '🌿'),
 ('Pintura', 'pintura', 'Pintura de interiores, exteriores y acabados', '🎨'),
 ('Albanileria', 'albanileria', 'Construccion, remodelacion y acabados', '🧱'),
 ('Cerrajeria', 'cerrajeria', 'Apertura, cambio y reparacion de cerraduras', '🔑'),
 ('Aire acondicionado', 'aire-acondicionado', 'Instalacion y mantenimiento de clima y refrigeracion', '❄️'),
 ('Mudanzas y fletes', 'mudanzas-y-fletes', 'Mudanzas locales, fletes y transporte de muebles', '🚚'),
 ('Belleza y estetica', 'belleza-y-estetica', 'Peluqueria, manicure y servicios de estetica a domicilio', '💇'),
 ('Tecnologia y soporte', 'tecnologia-y-soporte', 'Soporte tecnico, reparacion de equipos y redes', '💻'),
 ('Clases particulares', 'clases-particulares', 'Clases y asesorias academicas personalizadas', '📚'),
 ('Mecanica automotriz', 'mecanica-automotriz', 'Reparacion y mantenimiento de autos y motocicletas', '🚗'),
 ('Fotografia y video', 'fotografia-y-video', 'Cobertura fotografica y de video para eventos y proyectos', '📷'),
 ('Eventos y catering', 'eventos-y-catering', 'Organizacion de eventos, banquetes y servicio de catering', '🎉'),
 ('Cuidado de mascotas', 'cuidado-de-mascotas', 'Paseo, cuidado y estetica para mascotas', '🐾'),
 ('Ninera y cuidado de personas', 'ninera-y-cuidado-de-personas', 'Cuidado de ninos, adultos mayores y personas dependientes', '🍼'),
 ('Fumigacion y control de plagas', 'fumigacion-y-control-de-plagas', 'Control y eliminacion de plagas en el hogar y negocios', '🐜'),
 ('Diseno grafico', 'diseno-grafico', 'Diseno de marca, publicidad y material grafico', '🎨')
ON CONFLICT (slug) DO NOTHING;

-- ============ PERFILES DE PRESTADOR DEMO ============

INSERT INTO provider_profiles (user_id, business_name, bio, years_experience, city, is_verified)
SELECT u.id, v.business_name, v.bio, v.years_experience, v.city, TRUE
FROM (VALUES
 ('carpinteria@demo.com', 'Carpinteria Perez', 'Especialistas en muebles a medida, closets e instalacion de puertas con mas de 8 anos de experiencia.', 8, 'Ciudad de Mexico'),
 ('plomeria@demo.com', 'Plomeria Express', 'Servicio de plomeria rapido y confiable para el hogar: fugas, instalaciones y mantenimiento.', 5, 'Guadalajara'),
 ('electricidad@demo.com', 'Electricidad Ruiz', 'Instalaciones electricas seguras y certificadas, mantenimiento residencial y comercial.', 10, 'Ciudad de Mexico'),
 ('limpieza@demo.com', 'Limpieza Total Martinez', 'Limpieza profunda y de mantenimiento para casas y departamentos, equipo propio.', 4, 'Monterrey')
) AS v(email, business_name, bio, years_experience, city)
JOIN users u ON u.email = v.email
WHERE NOT EXISTS (SELECT 1 FROM provider_profiles pp WHERE pp.user_id = u.id);

INSERT INTO provider_categories (provider_profile_id, category_id)
SELECT pp.id, c.id
FROM provider_profiles pp
JOIN users u ON u.id = pp.user_id
JOIN categories c ON (
    (u.email = 'carpinteria@demo.com' AND c.slug = 'carpinteria') OR
    (u.email = 'plomeria@demo.com' AND c.slug = 'plomeria') OR
    (u.email = 'electricidad@demo.com' AND c.slug = 'electricidad') OR
    (u.email = 'electricidad@demo.com' AND c.slug = 'aire-acondicionado') OR
    (u.email = 'limpieza@demo.com' AND c.slug = 'limpieza-del-hogar')
)
ON CONFLICT DO NOTHING;

-- ============ SERVICIOS OFRECIDOS DEMO ============

INSERT INTO service_offerings (provider_profile_id, category_id, title, description, price, price_type, estimated_duration_value)
SELECT pp.id, c.id, v.title, v.description, v.price, v.price_type, v.duration
FROM (VALUES
 ('carpinteria@demo.com', 'carpinteria', 'Instalacion de closet a medida',
  'Diseno, fabricacion e instalacion de closet a medida segun el espacio disponible.', 4500.00, 'COTIZACION', 480),
 ('carpinteria@demo.com', 'carpinteria', 'Reparacion de puertas y ventanas',
  'Ajuste, cambio de bisagras y reparacion general de puertas y ventanas de madera.', 350.00, 'POR_HORA', 60),
 ('plomeria@demo.com', 'plomeria', 'Reparacion de fugas de agua',
  'Deteccion y reparacion de fugas en tuberia, llaves y conexiones.', 400.00, 'POR_HORA', 60),
 ('plomeria@demo.com', 'plomeria', 'Instalacion de calentador de agua',
  'Instalacion completa de calentador de paso o de deposito.', 1200.00, 'FIJO', 120),
 ('electricidad@demo.com', 'electricidad', 'Revision e instalacion electrica residencial',
  'Diagnostico de cortos, cambio de contactos, apagadores e instalacion de circuitos.', 450.00, 'POR_HORA', 60),
 ('electricidad@demo.com', 'aire-acondicionado', 'Instalacion de minisplit',
  'Instalacion de equipo de aire acondicionado tipo minisplit, incluye materiales basicos.', 2200.00, 'FIJO', 180),
 ('limpieza@demo.com', 'limpieza-del-hogar', 'Limpieza profunda de casa habitacion',
  'Limpieza profunda de cocina, banos, recamaras y areas comunes.', 900.00, 'FIJO', 240)
) AS v(email, category_slug, title, description, price, price_type, duration)
JOIN users u ON u.email = v.email
JOIN provider_profiles pp ON pp.user_id = u.id
JOIN categories c ON c.slug = v.category_slug
WHERE NOT EXISTS (
    SELECT 1 FROM service_offerings so WHERE so.provider_profile_id = pp.id AND so.title = v.title
);

-- ============ CONFIGURACION DE PLATAFORMA (fila unica) ============

INSERT INTO platform_config (platform_name, legal_name, contact_email, contact_phone,
                              primary_color, secondary_color, welcome_message, footer_text)
SELECT 'NexJob', 'NexJob Plataforma de Servicios S.A. de C.V.', 'contacto@nexjob-demo.com', '5555550000',
       '#155DEA', '#0F172A',
       'Encuentra al prestador de servicios ideal para tu hogar',
       'NexJob - Todos los derechos reservados'
WHERE NOT EXISTS (SELECT 1 FROM platform_config);

-- ============ CONFIGURACION DE CORREO (fila unica, deshabilitada por default) ============

INSERT INTO email_config (enabled, smtp_host, smtp_port)
SELECT FALSE, 'smtp.gmail.com', 587
WHERE NOT EXISTS (SELECT 1 FROM email_config);
