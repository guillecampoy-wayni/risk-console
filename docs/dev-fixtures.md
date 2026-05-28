# Dev Fixtures

## Proposito

Los archivos JSON en `backend/src/main/resources/devdata/` contienen los datos de prueba que el
`DevPomeloGateway` (activo con perfil `dev`) utiliza para simular las respuestas de la API de Pomelo
durante el desarrollo local.

Estos archivos son la unica fuente de verdad para los datos de desarrollo. Cualquier cambio en los
datos de prueba debe hacerse sobre estos JSON, no en codigo Java.

## Ubicacion

| Archivo | Contenido |
|---------|-----------|
| `backend/src/main/resources/devdata/pomelo-users.json` | Lista de usuarios simulados que DevPomeloGateway retorna en `searchUsers()` |
| `backend/src/main/resources/devdata/pomelo-accounts.json` | Mapa de `userId` a lista de cuentas, usado por DevPomeloGateway en `listAccounts()` |

### Formato: usuarios

```json
[
  {
    "id": "usr-dev-blocked-1",
    "name": "Lucia",
    "surname": "Fernandez",
    "email": "lucia.fernandez@example.com",
    "identification_type": "DNI",
    "identification_value": "30111222",
    "tax_identification_type": "CUIL",
    "tax_identification_value": "27301112229",
    "external_id": "client-dev-001",
    "status": "BLOCKED"
  }
]
```

### Formato: cuentas

```json
{
  "usr-dev-blocked-1": [
    {
      "id": "acc-dev-ars-frozen",
      "country": "ARG",
      "balance": "982345.12",
      "status": "FROZEN",
      "currency": "ARS",
      "status_update_motive": "OTHER",
      "status_update_comment": "Revision manual por alerta de riesgo",
      "status_updated_by": "CLIENT",
      "updated_at": "2026-05-20T14:30:00Z"
    }
  ]
}
```

## Ciclo de vida

1. Los JSON se cargan al iniciar `DevPomeloGateway` mediante `ObjectMapper` en el constructor.
2. Cualquier cambio en estos archivos se refleja automaticamente al reiniciar la aplicacion en dev.
3. Los archivos se versionan en el repositorio como parte del codigo fuente.

## Uso desde el frontend

El frontend puede consumir estos mismos JSON para:

- Mockear respuestas del backend durante el desarrollo frontend.
- Validar contratos de interfaz contra datos realistas.
- Generar fixtures para tests de componentes.

Los archivos estan disponibles en `backend/src/main/resources/devdata/`. Para usarlos desde el
frontend se pueden copiar o referenciar como parte del build.

## Fixtures de test

Los tests de integracion (outside-in) usan sus propias copias de los datos en:

| Ruta | Proposito |
|------|-----------|
| `src/test/resources/fixtures/input/pomelo-users.json` | Datos de entrada cargados por `RiskReportE2ETest` |
| `src/test/resources/fixtures/input/pomelo-accounts.json` | Datos de entrada de cuentas para el test |
| `src/test/resources/fixtures/output/risk-report-blocked-users.json` | Reporte esperado tras transformar los datos de entrada |

### Relacion entre devdata y test fixtures

- Los fixtures de test son una copia exacta de los datos en `devdata/` para que el test E2E
  valide el pipeline completo.
- Si se actualizan los datos en `devdata/`, los fixtures de test deben actualizarse para reflejar
  los nuevos valores esperados en el reporte de salida.
- El test `RiskReportE2ETest` valida que la transformacion (controller -> service -> gateway)
  produce exactamente el JSON de salida esperado.
