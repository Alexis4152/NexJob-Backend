-- ============================================================
-- NexJob - Datos de prueba para el buscador con filtros
-- (verificado, anos de experiencia, precio maximo, con fotos,
--  calificacion minima, categoria, ubicacion, orden)
-- Password para todas las cuentas nuevas: Cliente123!
-- Idempotente: se puede re-ejecutar sin duplicar nada.
-- ============================================================

-- ============ USUARIOS PRESTADOR DE PRUEBA ============

INSERT INTO users (email, password_hash, first_name, last_name, phone, role_id, is_active)
SELECT v.email, '$2b$10$1H9H17NH8WIGTbIVRurtQeXxVeCWExR0PNJCwCPHmJginz/g7K4fG', v.first_name, v.last_name, v.phone, r.id, TRUE
FROM (VALUES
 ('jardineria1@demo.com',    'Luis',    'Hernandez', '5555550401'),
 ('pintura1@demo.com',       'Karla',   'Sanchez',   '5555550402'),
 ('pintura2@demo.com',       'Mario',   'Lopez',     '5555550403'),
 ('cerrajeria1@demo.com',    'Diana',   'Torres',    '5555550404'),
 ('limpieza2@demo.com',      'Sofia',   'Ramirez',   '5555550405'),
 ('plomeria2@demo.com',      'Jorge',   'Castillo',  '5555550406'),
 ('electricidad2@demo.com',  'Paola',   'Vargas',    '5555550407'),
 ('carpinteria2@demo.com',   'Hector',  'Mendoza',   '5555550408')
) AS v(email, first_name, last_name, phone)
JOIN roles r ON r.name = 'PROVIDER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = v.email);

-- ============ PERFILES DE PRESTADOR DE PRUEBA ============
-- years_experience y city cubren todos los umbrales de los filtros (1+/3+/5+/10+ anios,
-- ciudades distintas para probar "Ubicacion"). is_verified mezcla true/false.

INSERT INTO provider_profiles (user_id, business_name, bio, years_experience, city, is_verified)
SELECT u.id, v.business_name, v.bio, v.years_experience, v.city, v.is_verified
FROM (VALUES
 ('jardineria1@demo.com',   'Jardines Verdes SA',           'Diseno y mantenimiento de jardines residenciales.',              2,  'Ciudad de Mexico', FALSE),
 ('pintura1@demo.com',      'Pintores Profesionales CDMX',  'Pintura de interiores y exteriores con acabados finos.',         12, 'Ciudad de Mexico', TRUE),
 ('pintura2@demo.com',      'Acabados y Pintura Lopez',     'Pintura y acabados decorativos para hogar y oficina.',           3,  'Puebla',           FALSE),
 ('cerrajeria1@demo.com',   'Cerrajeria 24 Horas',          'Apertura, cambio y reparacion de cerraduras.',                   6,  'Guadalajara',      TRUE),
 ('limpieza2@demo.com',     'EcoLimpieza Hogar',            'Limpieza de mantenimiento con productos ecologicos.',            1,  'Monterrey',        FALSE),
 ('plomeria2@demo.com',     'Plomeros Unidos Queretaro',    'Servicio de plomeria de alta gama para casas y negocios.',       15, 'Queretaro',        TRUE),
 ('electricidad2@demo.com', 'ElectroSoluciones Express',    'Instalaciones y mantenimiento electrico residencial.',           4,  'Ciudad de Mexico', TRUE),
 ('carpinteria2@demo.com',  'Muebles y Mas Carpinteria',    'Carpinteria general, apenas iniciando en la plataforma.',        0,  'Toluca',           FALSE)
) AS v(email, business_name, bio, years_experience, city, is_verified)
JOIN users u ON u.email = v.email
WHERE NOT EXISTS (SELECT 1 FROM provider_profiles pp WHERE pp.user_id = u.id);

-- Calificacion y numero de resenas (para probar 3+/4+/4.5+ estrellas y los ordenamientos).
-- El flujo normal de la app las calcula al recibir resenas; aqui se fijan directo para pruebas.
UPDATE provider_profiles pp SET average_rating = v.rating, total_reviews = v.reviews
FROM (VALUES
 ('jardineria1@demo.com',   3.8,  12),
 ('pintura1@demo.com',      4.8,  210),
 ('pintura2@demo.com',      4.1,  18),
 ('cerrajeria1@demo.com',   4.5,  95),
 ('limpieza2@demo.com',     3.2,  6),
 ('plomeria2@demo.com',     4.9,  320),
 ('electricidad2@demo.com', 4.3,  40),
 ('carpinteria2@demo.com',  0,    0)
) AS v(email, rating, reviews)
JOIN users u ON u.email = v.email
WHERE pp.user_id = u.id;

-- ============ CATEGORIA DE CADA PRESTADOR DE PRUEBA ============

INSERT INTO provider_categories (provider_profile_id, category_id)
SELECT pp.id, c.id
FROM provider_profiles pp
JOIN users u ON u.id = pp.user_id
JOIN categories c ON (
    (u.email = 'jardineria1@demo.com'   AND c.slug = 'jardineria') OR
    (u.email = 'pintura1@demo.com'      AND c.slug = 'pintura') OR
    (u.email = 'pintura2@demo.com'      AND c.slug = 'pintura') OR
    (u.email = 'cerrajeria1@demo.com'   AND c.slug = 'cerrajeria') OR
    (u.email = 'limpieza2@demo.com'     AND c.slug = 'limpieza-del-hogar') OR
    (u.email = 'plomeria2@demo.com'     AND c.slug = 'plomeria') OR
    (u.email = 'electricidad2@demo.com' AND c.slug = 'electricidad') OR
    (u.email = 'carpinteria2@demo.com'  AND c.slug = 'carpinteria')
)
ON CONFLICT DO NOTHING;

-- ============ SERVICIOS OFRECIDOS (precios cruzan los umbrales $300 y $2000) ============

INSERT INTO service_offerings (provider_profile_id, category_id, title, description, price, price_type, estimated_duration_value)
SELECT pp.id, c.id, v.title, v.description, v.price, v.price_type, v.duration
FROM (VALUES
 ('jardineria1@demo.com',   'jardineria',         'Mantenimiento mensual de jardin',   'Corte de pasto, poda y riego de jardines residenciales.', 350.00,  'FIJO',       120),
 ('pintura1@demo.com',      'pintura',            'Pintura de interiores por cuarto',  'Pintura de interiores con acabado profesional, incluye materiales.', 280.00, 'POR_HORA', 90),
 ('pintura2@demo.com',      'pintura',            'Pintura de fachada completa',       'Pintura de exteriores y fachada, incluye andamios.', 1800.00, 'COTIZACION', 480),
 ('cerrajeria1@demo.com',   'cerrajeria',         'Cambio de cerradura',               'Cambio de cerradura de puerta principal, servicio a domicilio.', 250.00, 'FIJO', 45),
 ('limpieza2@demo.com',     'limpieza-del-hogar', 'Limpieza ecologica basica',         'Limpieza de mantenimiento con productos biodegradables.', 600.00, 'FIJO', 180),
 ('plomeria2@demo.com',     'plomeria',           'Remodelacion de bano completa',     'Cambio de tuberia, muebles de bano e instalacion hidraulica completa.', 3500.00, 'COTIZACION', 480),
 ('electricidad2@demo.com', 'electricidad',       'Instalacion electrica por circuito','Instalacion y certificacion de circuitos electricos nuevos.', 700.00, 'POR_HORA', 90),
 ('carpinteria2@demo.com',  'carpinteria',        'Reparacion de mueble sencillo',     'Reparaciones menores de muebles de madera.', 500.00, 'POR_HORA', 60)
) AS v(email, category_slug, title, description, price, price_type, duration)
JOIN users u ON u.email = v.email
JOIN provider_profiles pp ON pp.user_id = u.id
JOIN categories c ON c.slug = v.category_slug
WHERE NOT EXISTS (
    SELECT 1 FROM service_offerings so WHERE so.provider_profile_id = pp.id AND so.title = v.title
);

-- ============ FOTOS DE TRABAJOS (para probar el filtro "Con fotos") ============
-- Solo se les agrega foto a la mitad: pintura1, cerrajeria1, plomeria2, electricidad2.
-- Las URLs son de prueba (no apuntan a un archivo real); el filtro solo verifica que exista el registro.

INSERT INTO service_images (service_offering_id, url, sort_order)
SELECT so.id, v.url, 0
FROM (VALUES
 ('pintura1@demo.com',      'Pintura de interiores por cuarto',   '/uploads/test/pintura1-trabajo1.jpg'),
 ('cerrajeria1@demo.com',   'Cambio de cerradura',                '/uploads/test/cerrajeria1-trabajo1.jpg'),
 ('plomeria2@demo.com',     'Remodelacion de bano completa',      '/uploads/test/plomeria2-trabajo1.jpg'),
 ('electricidad2@demo.com', 'Instalacion electrica por circuito', '/uploads/test/electricidad2-trabajo1.jpg')
) AS v(email, title, url)
JOIN users u ON u.email = v.email
JOIN provider_profiles pp ON pp.user_id = u.id
JOIN service_offerings so ON so.provider_profile_id = pp.id AND so.title = v.title
WHERE NOT EXISTS (
    SELECT 1 FROM service_images si WHERE si.service_offering_id = so.id
);
