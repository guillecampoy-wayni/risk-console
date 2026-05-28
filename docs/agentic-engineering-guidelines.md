# Lineamientos agenticos de ingenieria

## Proposito

Este documento define como debe trabajar el agente sobre este repositorio para que cada cambio sea trazable, testeable y validable antes de subirlo a una rama remota.

Aplica a pedidos de implementacion, refactor, fixes, documentacion tecnica y evolucion del prototipo inicial de Risk Console.

## Principios de trabajo

- Entender primero el estado del repositorio: leer README, ADRs, build files y codigo tocado por el pedido.
- Mantener cambios pequenos, versionables y alineados con la arquitectura existente.
- No introducir GitHub Actions ni workflows bajo `.github/workflows` para esta etapa del prototipo.
- Usar validacion local obligatoria antes de publicar una rama remota.
- Tratar los tests como intenciones de diseno: cada comportamiento esperado se expresa primero como prueba o caso verificable antes de cerrar la implementacion.
- Usar programacion orientada a objetos de forma mandatoria en el backend: modelar responsabilidades en clases, servicios, gateways, configuraciones y objetos de dominio, evitando implementaciones procedurales o scripts embebidos para logica de negocio.
- No usar Mockito ni agregar dependencias `mockito-*`. Los tests deben usar fakes, stubs o dobles de prueba implementados como clases propias para evitar problemas de portabilidad con agentes dinamicos de ByteBuddy/Mockito inline en distintas JVMs.
- No revertir cambios ajenos sin instruccion explicita.

## Flujo TDD esperado

Para cada pedido funcional o tecnico, el agente debe trabajar con este ciclo:

1. Declarar la intencion de comportamiento: que caso de negocio, regla o contrato se esta protegiendo.
2. Escribir o ajustar tests que fallen por la ausencia del comportamiento.
3. Implementar el minimo cambio productivo que haga pasar los tests.
4. Refactorizar manteniendo la suite verde.
5. Ejecutar cobertura y revisar que el umbral minimo siga cumplido.
6. Documentar decisiones relevantes si cambian arquitectura, contratos o flujo operativo.

Los tests deben cubrir intenciones observables, no detalles accidentales. Ejemplos de intenciones validas:

- "Cuando Pomelo devuelve usuarios bloqueados, el reporte conserva usuario, estado y cuentas asociadas".
- "Cuando una cuenta no coincide con los filtros solicitados, no aparece en el reporte".
- "Cuando falta una credencial requerida, la aplicacion falla de forma explicita y diagnosticable".

### Dobles de prueba

No se permite Mockito en este repositorio. Para aislar colaboraciones se deben crear dobles de prueba orientados a objetos:

- Fakes en memoria para gateways, repositorios o clientes externos.
- Stubs pequenos para respuestas deterministicas.
- Clases de soporte dentro del test cuando el doble solo aplica a un caso.

Motivo: Mockito inline depende de attach dinamico de ByteBuddy y puede fallar segun JVM, sistema operativo o restricciones del entorno. Esa variabilidad rompe la validacion local y el flujo de push.

## Cobertura minima

El backend usa JaCoCo como puerta de calidad local. La cobertura minima aceptada es:

- 80% de lineas cubiertas por bundle Maven.

La regla queda configurada en `backend/pom.xml` con `jacoco-maven-plugin`. Cualquier cambio que reduzca la cobertura por debajo del umbral debe agregar pruebas o justificar una exclusion tecnica explicita en el mismo cambio.

Comando de validacion backend:

```bash
cd backend
mvn clean verify
```

Script parametrizable:

```bash
scripts/validate-coverage.sh --threshold 80 --mode block
```

Modos disponibles:

- `block`: falla la ejecucion si la cobertura queda por debajo del umbral.
- `warn`: ejecuta tests y JaCoCo, reporta advertencia si no cumple, pero no bloquea el proceso.

Reportes generados:

```text
backend/target/site/jacoco/index.html
backend/target/site/jacoco/jacoco.xml
```

## Validacion local antes del push

Antes de empujar una rama remota, el agente debe ejecutar la validacion local aplicable:

```bash
scripts/validate-coverage.sh --threshold 80 --mode block
```

```bash
cd frontend
npm install
npm run build
```

Si una validacion no puede ejecutarse por falta de dependencias, red, credenciales o entorno, el agente debe dejarlo documentado en la respuesta final con el motivo exacto y el riesgo residual.

## Commit interactivo

Los cambios deben prepararse de forma revisable, evitando commits masivos sin inspeccion. Flujo recomendado:

```bash
git status --short
git diff
git add -p
git diff --cached
git commit -v
git push origin <rama>
```

Script interactivo recomendado:

```bash
scripts/push-origin-interactive.sh --threshold 80 --coverage-mode block
```

El script valida JaCoCo, permite staging interactivo con `git add -p`, abre commit interactivo con `git commit -v` y pide confirmacion antes de ejecutar `git push origin <rama>`.

El commit debe agrupar una unidad logica de trabajo. El mensaje debe describir la intencion, no solo los archivos modificados.

Ejemplos:

```text
docs: define agentic validation guidelines
test: capture blocked customer report intent
feat: expose filtered customer risk report
```

## Validacion al subir una rama remota

Como no se usan GitHub Actions en esta etapa, la validacion del push depende de la disciplina local y de la evidencia versionada:

1. La rama debe contener codigo, tests y documentacion del cambio.
2. `mvn clean verify` debe pasar localmente para backend y hacer cumplir JaCoCo.
3. `npm run build` debe pasar localmente para frontend cuando el cambio lo toque.
4. La respuesta final del agente debe indicar comandos ejecutados y resultado.
5. El push se realiza solo despues de revisar el diff staged con `git diff --cached`.

Comando de publicacion:

```bash
git push origin <rama>
```

## Pedidos futuros al agente

Cada pedido al agente deberia incluir, cuando aplique:

- Objetivo funcional o tecnico.
- Comportamiento esperado expresado como intencion testeable.
- Restricciones de arquitectura, seguridad o integracion.
- Criterio de aceptacion local.
- Documentacion que debe actualizarse.

Formato sugerido:

```text
Implementar <capacidad>.
Intencion testeable: <comportamiento observable>.
Criterio de aceptacion: mvn clean verify y/o npm run build pasan localmente.
Actualizar documentacion si cambia contrato, arquitectura o flujo operativo.
Usar programacion orientada a objetos para la logica de negocio.
No usar Mockito; crear fakes o stubs propios para tests.
No agregar GitHub Actions.
```

## Estado actual del prototipo

El prototipo actual tiene implementado:

- Backend Java 21 + Spring Boot 3 orientado a integracion con Pomelo.
- Frontend React + TypeScript + Vite.
- Infra local con Docker Compose y PostgreSQL (no usado en MVP 1-3; se usa almacenamiento en memoria).
- MVP 1: reporte de clientes filtrado, exportacion CSV, vista detalle, correlation ID.
- MVP 2: snapshots on-demand con `SnapshotService` e `InMemorySnapshotRepository`.
- MVP 3: auditoria automatica de consultas y exportaciones via `AuditWebFilter`, endpoint `GET /api/risk/audit`.
- 47 tests backend con cobertura >=80% via JaCoCo.
- ADR inicial de stack en `docs/adr-001-stack.md`.
- Umbral de cobertura backend versionado con JaCoCo en `backend/pom.xml`.

La persistencia con PostgreSQL (R2DBC) esta planificada para MVP 6. Hasta entonces, snapshots y auditoria usan `ConcurrentHashMap` en memoria, reemplazables por implementaciones con base de datos sin cambiar las interfaces de dominio.

Este estado es suficiente como base inicial siempre que las siguientes evoluciones mantengan tests de intencion, cobertura minima y validacion local antes de publicar ramas remotas.
