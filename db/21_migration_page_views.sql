-- ============================================================
-- NexJob - Registro de visitas anonimas (analitica del sitio)
--
-- Para que el admin pueda saber cuanta gente llega a la plataforma sin depender de que se
-- registren: cada carga de pagina publica crea una fila con un visitorId anonimo (generado y
-- guardado en el navegador, ver AnalyticsTracker.jsx), y al salir/navegar el navegador reporta
-- cuantos segundos permanecio (duration_seconds, aproximado).
--
-- Idempotente.
-- ============================================================

CREATE TABLE IF NOT EXISTS page_views (
    id                  BIGSERIAL PRIMARY KEY,
    visitor_id          VARCHAR(64) NOT NULL,
    path                VARCHAR(255) NOT NULL,
    referrer            VARCHAR(500),
    duration_seconds    INTEGER,
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_page_views_created_at ON page_views (created_at);
CREATE INDEX IF NOT EXISTS idx_page_views_visitor_id ON page_views (visitor_id);
