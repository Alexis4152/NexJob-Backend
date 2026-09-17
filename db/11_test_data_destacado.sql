-- ============================================================
-- NexJob - Datos de prueba: 2 prestadores que alcanzan "Profesional
-- destacado" (punto 15). Requiere ya tener corrido 03_test_data_filtros.sql
-- (crea a Pintores Profesionales CDMX y Cerrajeria 24 Horas) y
-- 10_migration_provider_verification.sql (columnas de verificacion).
--
-- "Profesional destacado" exige (ver ProviderServiceImpl.trustTierOf):
--   correo + telefono verificados, identidad verificada, 3+ anios de
--   experiencia, 5+ resenas, 10+ trabajos APROBADO y calificacion 4.5+.
-- Ambos prestadores ya cumplian anios/resenas/calificacion/identidad con
-- datos reales; aqui solo se autoriza correo+telefono y se agregan 10
-- contrataciones reales en estado APROBADO (que antes no existian) para
-- que "trabajos realizados" tambien sea cierto, no solo la resena agregada.
--
-- Idempotente: se puede re-ejecutar sin duplicar (folio unico).
-- Ejecutar con: psql -U postgres -d nexjob -f db/11_test_data_destacado.sql
-- ============================================================

UPDATE provider_profiles pp SET email_verified = true, phone_verified = true
FROM users u
WHERE pp.user_id = u.id AND u.email IN ('pintura1@demo.com', 'cerrajeria1@demo.com');

INSERT INTO bookings (folio, client_id, provider_profile_id, service_offering_id, agreed_price,
                       description, address_line, city, scheduled_at, status, payment_method)
SELECT 'TEST-DEST-P13-' || lpad(n::text, 2, '0'), cu.id, pp.id, so.id, so.price,
       'Contratacion de prueba concluida y aprobada (nivel Profesional destacado).',
       'Calle de prueba ' || n, pp.city,
       now() - (n || ' days')::interval,
       'APROBADO', 'EFECTIVO'
FROM generate_series(1, 10) AS n
JOIN users cu ON cu.email = 'cliente@demo.com'
JOIN users pu ON pu.email = 'pintura1@demo.com'
JOIN provider_profiles pp ON pp.user_id = pu.id
JOIN service_offerings so ON so.provider_profile_id = pp.id AND so.title = 'Pintura de interiores por cuarto'
WHERE NOT EXISTS (SELECT 1 FROM bookings b WHERE b.folio = 'TEST-DEST-P13-' || lpad(n::text, 2, '0'));

INSERT INTO bookings (folio, client_id, provider_profile_id, service_offering_id, agreed_price,
                       description, address_line, city, scheduled_at, status, payment_method)
SELECT 'TEST-DEST-P14-' || lpad(n::text, 2, '0'), cu.id, pp.id, so.id, so.price,
       'Contratacion de prueba concluida y aprobada (nivel Profesional destacado).',
       'Avenida de prueba ' || n, pp.city,
       now() - (n || ' days')::interval,
       'APROBADO', 'EFECTIVO'
FROM generate_series(1, 10) AS n
JOIN users cu ON cu.email = 'cliente@demo.com'
JOIN users pu ON pu.email = 'cerrajeria1@demo.com'
JOIN provider_profiles pp ON pp.user_id = pu.id
JOIN service_offerings so ON so.provider_profile_id = pp.id AND so.title = 'Cambio de cerradura'
WHERE NOT EXISTS (SELECT 1 FROM bookings b WHERE b.folio = 'TEST-DEST-P14-' || lpad(n::text, 2, '0'));
