-- ============================================================
-- NexJob - Ubicacion del cliente en las solicitudes de cotizacion
--
-- Hoy QuoteRequestServiceImpl.create() elige a los 5 prestadores
-- mejor calificados de la categoria sin importar en que ciudad o
-- estado esten. Estas columnas guardan el codigo postal capturado
-- en el formulario y su coordenada (via PostalCodeLookupService,
-- mismo catalogo de db/06_postal_codes.sql que ya usan los
-- prestadores) para poder filtrar/ordenar por distancia real
-- (formula de Haversine, ver GeoUtils) antes de notificar a nadie.
--
-- Idempotente.
-- ============================================================

ALTER TABLE quote_requests ADD COLUMN IF NOT EXISTS postal_code VARCHAR(5);
ALTER TABLE quote_requests ADD COLUMN IF NOT EXISTS latitude NUMERIC(9,6);
ALTER TABLE quote_requests ADD COLUMN IF NOT EXISTS longitude NUMERIC(9,6);
