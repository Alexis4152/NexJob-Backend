# NexJob Backend

API REST (Spring Boot 3.2.5 + Java 17 + PostgreSQL + JWT) para una plataforma que conecta
clientes con prestadores de servicio (carpinteros, plomeros, electricistas, etc.).

## Requisitos

- Java 17
- Maven (o usa el wrapper si lo agregas con `mvn -N io.takari:maven:wrapper`)
- PostgreSQL 14+

## 1. Crear la base de datos

Crea manualmente una base de datos vacia llamada `nexjob` en tu PostgreSQL local:

```sql
CREATE DATABASE nexjob;
```

Luego ejecuta el script de inicializacion (crea las tablas y carga datos demo, es
idempotente por lo que se puede re-ejecutar sin duplicar nada):

```bash
psql -U postgres -d nexjob -f db/init.sql
```

(`db/01_schema.sql` y `db/02_seed.sql` son las mismas piezas por separado, por si prefieres
correrlas una por una.)

## 2. Configurar variables de entorno (opcional)

Por default `application.properties` usa:

- `DB_URL=jdbc:postgresql://localhost:5432/nexjob`, usuario `postgres`, password `admin`
- Puerto del backend: `8082`
- CORS permitido para `http://localhost:5175` (el frontend en dev)

Sobreescribe con variables de entorno (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`,
`APP_JWT_SECRET`, `APP_CORS_ALLOWED_ORIGINS`, etc.) segun tu entorno.

## 3. Levantar el backend

```bash
mvn spring-boot:run
```

## Cuentas demo (password para todas: ver detalle abajo)

| Rol       | Correo                     | Password      |
|-----------|----------------------------|---------------|
| ADMIN     | admin@nexjob-demo.com      | Admin123!     |
| CLIENT    | cliente@demo.com           | Cliente123!   |
| PROVIDER  | carpinteria@demo.com       | Cliente123!   |
| PROVIDER  | plomeria@demo.com          | Cliente123!   |
| PROVIDER  | electricidad@demo.com      | Cliente123!   |
| PROVIDER  | limpieza@demo.com          | Cliente123!   |

## Modulos principales

- Registro de clientes (`/api/auth/register`) y de prestadores (`/api/auth/register-provider`)
- Catalogo publico de categorias y busqueda de prestadores con filtros (`/api/public/**`)
- Contratacion de un servicio con fecha de visita y metodo de pago (`/api/bookings`)
- Tablero Kanban del prestador para mover sus trabajos de estado (`/api/provider/bookings`)
- Subida de evidencias del servicio realizado y liberacion de pago por el cliente
- Resenas y calificacion del prestador
- Modulo de ayuda / reporte de problemas (`/api/support/tickets`)
- Panel de administracion: categorias, prestadores, clientes, contrataciones, soporte y
  configuracion de la plataforma (`/api/admin/**`)
