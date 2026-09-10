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
