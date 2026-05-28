# Pomelo Risk Console Starter

Aplicación inicial para consolidar usuarios y cuentas digitales de Pomelo, orientada a analistas de riesgo/fraude.

## Objetivo

Construir un reporte operativo que combine:

- `GET /users/v1/`: usuarios y estado del usuario (`ACTIVE`, `BLOCKED`).
- `GET /core/accounts/v1`: cuentas digitales, estado de cuenta, motivo y comentario de actualización.

La primera versión prioriza usuarios bloqueados, pero deja filtros para evolucionar a otros estados.

## Stack propuesto

- Backend: Java 21 + Spring Boot 3 + WebClient + PostgreSQL.
- Frontend: React + TypeScript + Vite.
- Infra local: Docker Compose con PostgreSQL.
- Observabilidad inicial: logs estructurados, correlation id y healthcheck.
- Seguridad inicial: API key interna para el frontend/backend o integración posterior con SSO/OIDC.

## Arquitectura lógica

```text
[Risk/Fraud Analyst]
        |
        v
[React Risk Console]
        |
        v
[Spring Boot Backend]
        |
        +--> Pomelo Users API
        +--> Pomelo Accounts API
        |
        +--> PostgreSQL snapshot/cache/audit
```

## Decisión funcional inicial

El backend expone un endpoint propio:

```http
GET /api/risk/customers?userStatus=BLOCKED&accountStatus=ACTIVE,FROZEN,DISABLED,DELETED&country=ARG&page=1&pageSize=50
```

El backend:

1. En modo productivo consulta usuarios en Pomelo filtrando por estado.
2. Para cada usuario, en modo productivo consulta cuentas por `filter[user_id]` y `filter[country]`.
3. Normaliza la información.
4. Devuelve un reporte apto para UI.
5. Opcionalmente persiste snapshot para auditoría, exportación y trazabilidad.

Para avanzar el prototipo sin credenciales reales, el modo desarrollo entrega datos representativos desde memoria y respeta los mismos filtros de `userStatus`, `accountStatus` y `country`.

## Modelo de reporte

```json
{
  "userId": "usr-...",
  "externalId": "client-internal-id",
  "fullName": "Juan Perez",
  "email": "juan@example.com",
  "identification": "DNI 12345678",
  "taxIdentification": "CUIL 20123456789",
  "userStatus": "BLOCKED",
  "accounts": [
    {
      "accountId": "acc-...",
      "country": "ARG",
      "currency": "ARS",
      "balance": "982345.12",
      "accountStatus": "FROZEN",
      "statusUpdateMotive": "OTHER",
      "statusUpdateComment": "Comentario sobre el motivo",
      "statusUpdatedBy": "CLIENT",
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

## Variables de entorno

Backend:

```bash
POMELO_BASE_URL=https://api.pomelo.la
POMELO_API_KEY=replace-me
DATABASE_URL=jdbc:postgresql://localhost:5432/risk_console
DATABASE_USER=risk
DATABASE_PASSWORD=risk
APP_INTERNAL_API_KEY=dev-local-key
```

Frontend:

```bash
VITE_API_BASE_URL=http://localhost:8080
VITE_INTERNAL_API_KEY=dev-local-key
```

## Ejecución local

### Backend en modo desarrollo

Usar este modo para trabajar el frontend sin depender de Pomelo ni de credenciales externas. Activa el perfil Spring `dev`, que reemplaza el gateway HTTP real por datos representativos en memoria.

```bash
docker compose up -d postgres
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Consulta de ejemplo:

```bash
curl -H 'X-Internal-Api-Key: dev-local-key' \
  'http://localhost:8080/api/risk/customers?userStatus=BLOCKED&accountStatus=FROZEN,DISABLED&country=ARG&page=1&pageSize=50'
```

### Backend en modo productivo

El perfil por defecto es `prod`. Este modo usa `PomeloGateway` y requiere credenciales válidas para llamar a Pomelo.

```bash
docker compose up -d postgres
cd backend
mvn spring-boot:run
```

También puede declararse explícitamente:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Lineamientos de validación

El repositorio versiona lineamientos agenticos para evolucionar el prototipo con TDD, programacion orientada a objetos mandatoria, cobertura minima del 80% mediante JaCoCo, commit interactivo y validacion local antes del push, sin GitHub Actions en esta etapa.

Ver [docs/agentic-engineering-guidelines.md](docs/agentic-engineering-guidelines.md).

### Restricciones de testing

No se debe usar Mockito ni agregar dependencias `mockito-*`.

Los tests deben aislar colaboraciones con fakes, stubs o dobles de prueba propios implementados como clases. Esta restriccion evita fallas de portabilidad por attach dinamico de ByteBuddy/Mockito inline en distintas JVMs y mantiene la suite ejecutable en validaciones locales antes del push.

### Cobertura backend

Validacion bloqueante con umbral por defecto del 80%:

```bash
scripts/validate-coverage.sh
```

Validacion con parametros explicitos:

```bash
scripts/validate-coverage.sh --threshold 80 --mode block
```

Modo advertencia, util para exploracion local sin bloquear por cobertura:

```bash
scripts/validate-coverage.sh --threshold 80 --mode warn
```

El script ejecuta `mvn clean verify`, genera el reporte JaCoCo y compara la cobertura de lineas contra el umbral indicado. En modo `block`, una cobertura menor al umbral corta la ejecucion con error.

Reportes:

```text
backend/target/site/jacoco/index.html
backend/target/site/jacoco/jacoco.xml
backend/target/site/jacoco/jacoco.csv
```

### Push interactivo a origin

Flujo recomendado para validar, revisar, commitear y publicar una rama:

```bash
scripts/push-origin-interactive.sh --threshold 80 --coverage-mode block
```

El script:

1. Muestra el estado local.
2. Ejecuta validacion backend con JaCoCo.
3. Ejecuta build frontend cuando corresponde o si se fuerza con `--frontend always`.
4. Permite staging interactivo con `git add -p`.
5. Muestra el diff staged y abre `git commit -v`.
6. Pide confirmacion antes de ejecutar `git push origin <rama>`.

Opciones utiles:

```bash
scripts/push-origin-interactive.sh --coverage-mode warn
scripts/push-origin-interactive.sh --threshold 85 --coverage-mode block
scripts/push-origin-interactive.sh --frontend always
scripts/push-origin-interactive.sh --branch feature/risk-report
```

## Roadmap recomendado

### MVP 1

- Reporte de usuarios bloqueados.
- Filtro por estado de cuenta.
- Exportación CSV.
- Vista detalle por usuario.
- Logs con correlation id.

### MVP 2

- Persistencia de snapshots.
- Historial de cambios de estado.
- Scheduler configurable para refresh.
- Alertas por variación anómala de bloqueos/frozen accounts.

### MVP 3

- SSO/OIDC.
- Roles: Risk Analyst, Risk Lead, Admin.
- Auditoría de consultas y exportaciones.
- Integración con motor interno de casos/fraude.
