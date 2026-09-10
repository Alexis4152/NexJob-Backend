-- ============================================================
-- NexJob - Migracion: unidad de duracion estimada del servicio
-- Antes solo se podia expresar la duracion en minutos; ahora tambien admite
-- dias, semanas y meses (columna estimated_duration_unit).
-- Idempotente: se puede re-ejecutar sin error sobre una base ya migrada.
-- Ejecutar con: psql -U postgres -d nexjob -f db/03_migration_duration_unit.sql
-- ============================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'service_offerings' AND column_name = 'estimated_duration_minutes'
    ) THEN
        ALTER TABLE service_offerings RENAME COLUMN estimated_duration_minutes TO estimated_duration_value;
    END IF;
END $$;

ALTER TABLE service_offerings
    ADD COLUMN IF NOT EXISTS estimated_duration_unit VARCHAR(20) NOT NULL DEFAULT 'MINUTOS';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'service_offerings_estimated_duration_unit_check'
    ) THEN
        ALTER TABLE service_offerings
            ADD CONSTRAINT service_offerings_estimated_duration_unit_check
            CHECK (estimated_duration_unit IN ('MINUTOS', 'DIAS', 'SEMANAS', 'MESES'));
    END IF;
END $$;
