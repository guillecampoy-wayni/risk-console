# ADR 001 - Stack inicial

## Decisión

Usar Java 21 + Spring Boot 3 para backend y React + TypeScript + Vite para frontend.

## Motivos

- Backend orientado a integración, normalización y reglas de negocio.
- WebClient permite llamadas concurrentes y controladas contra Pomelo.
- Java/Spring facilita observabilidad, testing, validaciones, retry/circuit breaker y seguridad enterprise.
- React + TypeScript permite una UI operativa simple, mantenible y escalable.

## Alternativas consideradas

- Node/NestJS: válido, pero menos alineado con equipos backend Java y gobernanza fuerte de tipos.
- Python/FastAPI: muy bueno para prototipos, menos ideal si el equipo quiere una base enterprise Java.
- Fullstack Next.js: rápido para MVP, pero menos claro para separar responsabilidades operativas y backend de integración.
