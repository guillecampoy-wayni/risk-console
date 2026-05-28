# ADR 001 - Stack inicial

## Decisión

Usar Java 21 + Spring Boot 3 para backend y React + TypeScript + Vite para frontend.

El backend usa perfiles Spring para separar ejecución productiva y desarrollo:

- `prod`: perfil por defecto, usa WebClient contra Pomelo.
- `dev`: usa un gateway en memoria con datos representativos para avanzar el frontend sin credenciales externas.

## Motivos

- Backend orientado a integración, normalización y reglas de negocio.
- WebClient permite llamadas concurrentes y controladas contra Pomelo.
- Java/Spring facilita observabilidad, testing, validaciones, retry/circuit breaker y seguridad enterprise.
- React + TypeScript permite una UI operativa simple, mantenible y escalable.
- Perfiles Spring mantienen el mismo contrato HTTP hacia el frontend mientras cambian la fuente de datos según el contexto de ejecución.

## Alternativas consideradas

- Node/NestJS: válido, pero menos alineado con equipos backend Java y gobernanza fuerte de tipos.
- Python/FastAPI: muy bueno para prototipos, menos ideal si el equipo quiere una base enterprise Java.
- Fullstack Next.js: rápido para MVP, pero menos claro para separar responsabilidades operativas y backend de integración.
