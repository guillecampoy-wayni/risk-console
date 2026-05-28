![Java 21](https://img.shields.io/badge/Java-21-blue?logo=openjdk)
![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.3.5-brightgreen?logo=spring)
![React](https://img.shields.io/badge/React-18-61DAFB?logo=react)
![TypeScript](https://img.shields.io/badge/TypeScript-5-blue?logo=typescript)
![JaCoCo Coverage](https://img.shields.io/badge/Coverage-%E2%89%A580%25-brightgreen?logo=codecov)
![Tests](https://img.shields.io/badge/Tests-47_passing-brightgreen)
![Build](https://img.shields.io/badge/Build-Maven_+_Vite-success?logo=apache-maven)

# Risk Console

Prototipo para consolidar usuarios y cuentas digitales de **Pomelo**, orientado a analistas de riesgo y fraude. Consulta usuarios bloqueados, revisa cuentas asociadas, exporta reportes, persiste snapshots y audita las operaciones realizadas.

---

## Estado del proyecto

| MVP | Capacidad | Estado |
|-----|-----------|--------|
| 1 | Reporte de usuarios bloqueados, filtros (userStatus/accountStatus/country), exportación CSV, vista detalle de cuentas, logs con correlation ID | ✅ Completo |
| 2 | Snapshots on-demand, histórico de snapshots, persistencia en memoria (reemplazable por PostgreSQL), botón Refresh Data en frontend | ✅ Completo |
| 3 | Auditoría automática de consultas y exportaciones, endpoint `GET /api/risk/audit`, orden cronológico inverso | ✅ Completo |
| 4 | SSO/OIDC, roles (Risk Analyst / Risk Lead / Admin) | ⬜ Pendiente |
| 5 | Integración con motor interno de casos/fraude | ⬜ Pendiente |

---

## Quick start

### Backend (modo desarrollo)

El perfil `dev` usa datos de prueba embebidos, no necesita credenciales Pomelo ni base de datos.

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Backend (modo productivo)

Requiere credenciales Pomelo reales y PostgreSQL corriendo.

```bash
docker compose up -d postgres
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Vite redirige `/api/*` a `localhost:8080`.

### Variables de entorno

**Backend:**

| Variable | Default (dev) | Descripción |
|----------|---------------|-------------|
| `POMELO_BASE_URL` | — | URL base de la API Pomelo |
| `POMELO_API_KEY` | — | API key para autenticar contra Pomelo |
| `DATABASE_URL` | — | JDBC URL de PostgreSQL (no usada en MVP 1–3) |
| `DATABASE_USER` | — | Usuario de base de datos |
| `DATABASE_PASSWORD` | — | Contraseña de base de datos |
| `APP_INTERNAL_API_KEY` | `dev-local-key` | API key para comunicación frontend→backend |

**Frontend:**

| Variable | Default | Descripción |
|----------|---------|-------------|
| `VITE_API_BASE_URL` | — | URL base del backend (vite proxy por defecto) |
| `VITE_INTERNAL_API_KEY` | `dev-local-key` | API key enviada en cada request |

---

## API reference

Todos los endpoints requieren el header `X-Internal-Api-Key`.

### `GET /api/risk/customers`

Reporte de clientes filtrado. Parámetros:

| Parámetro | Ejemplo | Descripción |
|-----------|---------|-------------|
| `userStatus` | `BLOCKED` | Estado del usuario |
| `accountStatus` | `ACTIVE,FROZEN,DISABLED,DELETED` | Estados de cuenta (separados por coma) |
| `country` | `ARG` | País |
| `page` | `1` | Número de página |
| `pageSize` | `50` | Registros por página |

Con `Accept: text/plain` descarga CSV.

### `POST /api/risk/snapshots`

Crea un snapshot del reporte actual. Retorna `201` con `{ "id", "createdAt" }`.

### `GET /api/risk/snapshots`

Lista todos los snapshots disponibles.

### `GET /api/risk/audit`

Devuelve el historial de operaciones (consultas, exportaciones, snapshots) en orden cronológico inverso.

```
[
  { "id": "uuid", "createdAt": "2026-…", "action": "QUERY", "details": "userStatus=…" },
  { "id": "uuid", "createdAt": "2026-…", "action": "CSV_EXPORT", "details": "…" }
]
```

**Acciones auditadas:**

| Acción | Dispara |
|--------|---------|
| `QUERY` | GET /api/risk/customers (JSON) |
| `CSV_EXPORT` | GET /api/risk/customers con Accept: text/plain |
| `SNAPSHOT_TAKE` | POST /api/risk/snapshots |
| `SNAPSHOT_LIST` | GET /api/risk/snapshots |

### `GET /api/risk/customers/{userId}`

Detalle de un usuario con todas sus cuentas.

---

## Arquitectura lógica

```text
[Risk Analyst]
     |
     v
[React Frontend] ──HTTP──> [Spring Boot Backend]
                                |
                  ┌─────────────┼─────────────┐
                  v             v             v
          [Pomelo API]   [InMemory]     [InMemory]
                         [Snapshot]     [AuditLog]
                  (productivo)   (MVP 2)     (MVP 3)
```

El backend expone una capa REST que orquesta:

1. **RiskReportService** — consulta Pomelo, normaliza datos, aplica filtros.
2. **SnapshotService** — persiste reportes completos para consulta/historial.
3. **AuditWebFilter** — captura automáticamente cada operación sobre los endpoints auditables.

Todas las respuestas incluyen `X-Correlation-Id`.

---

## Stack

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.3.5 (WebFlux + WebClient) |
| Calidad | JaCoCo 0.8.12 (≥80% cobertura), JUnit 5, AssertJ, Reactor Test |
| Frontend | React 18, TypeScript 5, Vite |
| Infra local | Docker Compose (PostgreSQL 16, opcional para MVP 1–3) |
| Diseño | Design tokens + sistema de componentes (ver `docs/Styles/`) |
| Testing | Sin Mockito — fakes/stubs propios |

---

## Testing y cobertura

```bash
# Backend: 47 tests, cobertura mínima 80%
cd backend
mvn clean verify

# Frontend
cd frontend
npm run build
```

Los lineamientos agenticos de ingeniería se documentan en [`docs/agentic-engineering-guidelines.md`](docs/agentic-engineering-guidelines.md): TDD fuera→dentro, OOP mandatorio, cobertura ≥80%, validación local antes del push, sin GitHub Actions.

### Scripts de validación

```bash
scripts/validate-coverage.sh          # umbral 80%, modo bloqueante
scripts/validate-coverage.sh --mode warn   # solo advierte si baja
scripts/push-origin-interactive.sh    # coverage + build + commit + push
```

---

## Datos de desarrollo

El perfil `dev` usa JSON embebidos en `backend/src/main/resources/devdata/`. Ver [`docs/dev-fixtures.md`](docs/dev-fixtures.md).

Para usar los mismos datos desde el frontend como mock, los archivos están disponibles en la misma ruta del backend.

---

## Documentación técnica

| Archivo | Contenido |
|---------|-----------|
| [`docs/adr-001-stack.md`](docs/adr-001-stack.md) | Decisión de stack: Java 21 + Spring Boot 3 + React + TypeScript |
| [`docs/agentic-engineering-guidelines.md`](docs/agentic-engineering-guidelines.md) | Lineamientos de TDD, cobertura, commits y push |
| [`docs/dev-fixtures.md`](docs/dev-fixtures.md) | Formato y ciclo de vida de los datos de prueba |
| [`docs/diagrams/context.puml`](docs/diagrams/context.puml) | Diagrama de contexto C4 |
| [`docs/Styles/*.pdf`](docs/Styles/) | Design tokens del sistema de componentes |

---

## Roadmap futuro

| MVP | Capacidad |
|-----|-----------|
| 4 | SSO/OIDC, roles (Risk Analyst, Risk Lead, Admin) |
| 5 | Motor interno de reglas de fraude, dashboard de alertas |
| 6 | Persistencia PostgreSQL para snapshots y auditoría (R2DBC) |
| 7 | Scheduler programable para snapshots automáticos |
