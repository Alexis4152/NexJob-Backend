-- ============================================================
-- NexJob - Migracion: columnas de geolocalizacion para el filtro
-- de "Distancia" (aproximada, a nivel ciudad).
-- Ya viene incluida en 01_schema.sql para instalaciones nuevas;
-- este script es para bases de datos que ya existian antes de esto.
-- Idempotente: ALTER ... IF NOT EXISTS.
-- ============================================================

ALTER TABLE provider_profiles ADD COLUMN IF NOT EXISTS latitude  NUMERIC(9,6);
ALTER TABLE provider_profiles ADD COLUMN IF NOT EXISTS longitude NUMERIC(9,6);

-- ============ COORDENADAS APROXIMADAS POR CIUDAD (centro de la ciudad) ============
-- Solo rellena donde todavia no hay coordenadas capturadas (no pisa datos reales futuros).
-- Es una aproximacion: todos los prestadores de la misma ciudad comparten el mismo punto,
-- ya que no existe una direccion geocodificada por prestador.

UPDATE provider_profiles SET latitude = 19.432608, longitude = -99.133209
WHERE latitude IS NULL AND city ILIKE 'Ciudad de Mexico';

UPDATE provider_profiles SET latitude = 20.659699, longitude = -103.349609
WHERE latitude IS NULL AND city ILIKE 'Guadalajara';

UPDATE provider_profiles SET latitude = 25.686613, longitude = -100.316116
WHERE latitude IS NULL AND city ILIKE 'Monterrey';

UPDATE provider_profiles SET latitude = 19.041297, longitude = -98.206566
WHERE latitude IS NULL AND city ILIKE 'Puebla';

UPDATE provider_profiles SET latitude = 20.588793, longitude = -100.389888
WHERE latitude IS NULL AND city ILIKE 'Queretaro';

UPDATE provider_profiles SET latitude = 19.292330, longitude = -99.653832
WHERE latitude IS NULL AND city ILIKE 'Toluca';
