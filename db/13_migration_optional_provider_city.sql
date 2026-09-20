-- ============================================================
-- NexJob - Migracion: simplificar el registro de prestador
-- El formulario publico de registro ahora solo pide lo esencial (nombre, correo, telefono
-- opcional, contrasena, categoria y nombre del negocio); ciudad, codigo postal, anos de
-- experiencia y semblanza se completan despues desde "Mi perfil" para no abrumar al usuario
-- con un formulario largo. Bio/anos de experiencia/codigo postal ya eran opcionales; solo
-- faltaba liberar la restriccion NOT NULL de la ciudad.
-- Idempotente: se puede re-ejecutar sin error sobre una base ya migrada.
-- Ejecutar con: psql -U postgres -d nexjob -f db/13_migration_optional_provider_city.sql
-- ============================================================

ALTER TABLE provider_profiles ALTER COLUMN city DROP NOT NULL;
