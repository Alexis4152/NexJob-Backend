-- ============================================================
-- NexJob - Cuestionarios de intake por categoria ("Cotizacion a la Medida")
--
-- Reemplaza el mapa hardcodeado en el frontend (utils/categoryQuestions.js)
-- por datos reales editables desde el admin (Constructor de Cuestionarios).
-- La columna guarda un arreglo JSON de { id, label, type, options }.
-- Categorias sin definir (columna NULL) hacen que el formulario del cliente
-- se comporte exactamente igual que antes de esta funcionalidad.
--
-- Se siembra el contenido que ya vivia hardcodeado para Carpinteria,
-- Plomeria y Pintura, para no perder nada al migrar.
--
-- Idempotente: los UPDATE de seed solo tocan las 3 categorias por slug,
-- y se pueden re-ejecutar sin duplicar nada.
-- ============================================================

ALTER TABLE categories ADD COLUMN IF NOT EXISTS intake_fields_json TEXT;

UPDATE categories SET intake_fields_json = '[
  {"id":"furnitureType","label":"Tipo de mueble","type":"SELECT","options":["Mesa","Closet","Puerta","Repisa","Otro"]},
  {"id":"woodType","label":"Tipo de madera","type":"SELECT","options":["Encino","Pino","MDF / aglomerado","No estoy seguro, pide sugerencia"]},
  {"id":"dimensions","label":"Medidas aproximadas","type":"DIMENSIONS","options":null},
  {"id":"finish","label":"Acabado o color","type":"SELECT","options":["Natural (barniz transparente)","Barniz oscuro","Pintado","Sin definir"]}
]' WHERE slug = 'carpinteria';

UPDATE categories SET intake_fields_json = '[
  {"id":"problemType","label":"Tipo de problema","type":"SELECT","options":["Fuga de agua","Instalacion nueva","Destape de tuberia","Mantenimiento"]},
  {"id":"zone","label":"Zona afectada","type":"SELECT","options":["Cocina","Bano","Patio / azotea","Otro"]},
  {"id":"waterNow","label":"Hay agua acumulada o goteando ahora mismo","type":"CHECKBOX","options":null}
]' WHERE slug = 'plomeria';

UPDATE categories SET intake_fields_json = '[
  {"id":"place","label":"Tipo de espacio","type":"SELECT","options":["Interior","Exterior","Ambos"]},
  {"id":"area","label":"Metros cuadrados aproximados","type":"NUMBER","options":null},
  {"id":"color","label":"Color deseado","type":"TEXT","options":null}
]' WHERE slug = 'pintura';
