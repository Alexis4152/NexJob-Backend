-- ============================================================
-- NexJob - Reservas de prueba para el filtro de "Disponibilidad"
-- (Hoy / Manana / Esta semana), calculado en vivo contra la tabla
-- bookings (ver ProviderSpecifications.search -> parametro availability).
-- Requiere haber corrido antes 03_test_data_filtros.sql.
-- Idempotente: se puede re-ejecutar sin duplicar (folio unico).
-- ============================================================

-- Bloquea a "Cerrajeria 24 Horas" HOY -> no debe aparecer con Disponibilidad=Hoy ni Urgente.
INSERT INTO bookings (folio, client_id, provider_profile_id, service_offering_id, agreed_price,
                       description, address_line, city, scheduled_at, status, payment_method)
SELECT 'TEST-DISP-0001', cu.id, pp.id, so.id, so.price,
       'Reserva de prueba: bloquea "Hoy" para probar el filtro de disponibilidad.',
       'Calle de prueba 123', pp.city,
       date_trunc('day', now()) + interval '11 hours',
       'ACEPTADO', 'EFECTIVO'
FROM users cu
JOIN users pu ON pu.email = 'cerrajeria1@demo.com'
JOIN provider_profiles pp ON pp.user_id = pu.id
JOIN service_offerings so ON so.provider_profile_id = pp.id AND so.title = 'Cambio de cerradura'
WHERE cu.email = 'cliente@demo.com'
AND NOT EXISTS (SELECT 1 FROM bookings b WHERE b.folio = 'TEST-DISP-0001');

-- Bloquea a "Plomeros Unidos Queretaro" MANANA -> no debe aparecer con Disponibilidad=Manana.
INSERT INTO bookings (folio, client_id, provider_profile_id, service_offering_id, agreed_price,
                       description, address_line, city, scheduled_at, status, payment_method)
SELECT 'TEST-DISP-0002', cu.id, pp.id, so.id, so.price,
       'Reserva de prueba: bloquea "Manana" para probar el filtro de disponibilidad.',
       'Avenida de prueba 456', pp.city,
       date_trunc('day', now()) + interval '1 day 10 hours',
       'ACEPTADO', 'EFECTIVO'
FROM users cu
JOIN users pu ON pu.email = 'plomeria2@demo.com'
JOIN provider_profiles pp ON pp.user_id = pu.id
JOIN service_offerings so ON so.provider_profile_id = pp.id AND so.title = 'Remodelacion de bano completa'
WHERE cu.email = 'cliente@demo.com'
AND NOT EXISTS (SELECT 1 FROM bookings b WHERE b.folio = 'TEST-DISP-0002');
