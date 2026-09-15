-- ============================================================
-- NexJob - Catalogo de codigos postales de Mexico con coordenadas
-- (para reemplazar las coordenadas aproximadas "por ciudad" de
-- 05_add_geo_columns.sql por una aproximacion mucho mas fina: por
-- codigo postal / colonia).
--
-- Fuente: GeoNames (http://download.geonames.org/export/zip/MX.zip),
-- que a su vez consolida el catalogo oficial de SEPOMEX. Licencia
-- Creative Commons Attribution 4.0 (CC-BY 4.0) - se requiere dar
-- credito a geonames.org si se redistribuye este dato.
--
-- 144,655 filas / 32,448 codigos postales distintos (un mismo CP
-- puede cubrir varias colonias, cada una con su propio centroide).
--
-- Requiere el archivo db/data/mx_postal_codes.txt (12 MB, TSV, no
-- se modifica: es la descarga tal cual de GeoNames). Se debe correr
-- con psql (el meta-comando \copy es de psql, no de SQL estandar),
-- desde la raiz del repo del backend:
--   psql -U postgres -d nexjob -f db/06_postal_codes.sql
--
-- Idempotente: recarga completa (DELETE + \copy) cada vez que se
-- corre, para que siempre quede igual al archivo fuente.
-- ============================================================

CREATE TABLE IF NOT EXISTS postal_codes (
    id                 BIGSERIAL PRIMARY KEY,
    country_code       VARCHAR(2)   NOT NULL,
    postal_code        VARCHAR(10)  NOT NULL,
    place_name         VARCHAR(180),          -- colonia / asentamiento
    admin_name1        VARCHAR(100),          -- estado
    admin_code1        VARCHAR(20),
    admin_name2        VARCHAR(100),          -- municipio / alcaldia
    admin_code2        VARCHAR(20),
    admin_name3        VARCHAR(100),          -- ciudad / localidad
    admin_code3        VARCHAR(20),
    latitude           NUMERIC(9,6),
    longitude          NUMERIC(9,6),
    accuracy           SMALLINT
);

DELETE FROM postal_codes;

\copy postal_codes(country_code, postal_code, place_name, admin_name1, admin_code1, admin_name2, admin_code2, admin_name3, admin_code3, latitude, longitude, accuracy) FROM 'db/data/mx_postal_codes.txt' WITH (FORMAT csv, DELIMITER E'\t', HEADER false, NULL '', ENCODING 'UTF8')

CREATE INDEX IF NOT EXISTS idx_postal_codes_postal_code ON postal_codes(postal_code);
