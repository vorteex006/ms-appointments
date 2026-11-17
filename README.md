# VitalPet Connect - ms-appointments

Microservicio de **citas** de VitalPet Connect (agendamiento de citas veterinarias), pensado para correr **por cuenta propia** (Spring Boot 3.2 · Java 17 · MySQL 8), con la misma estructura que `ms-pets`.

## Reglas de negocio

- Toda cita nueva queda en estado **Pendiente** (lo pone el servidor).
- La **fecha no puede ser anterior a hoy** y la **hora debe estar entre 09:00 y 19:00** (`400` si no se cumple).
- Cambios de estado permitidos (si no, `409`):
  - `Pendiente` → `Confirmada` o `Cancelada`
  - `Confirmada` → `Realizada`
  - `Realizada` y `Cancelada` son finales.
- Opcional: con `PETS_VALIDATION_ENABLED=true` se valida en `ms-pets` que la mascota exista, esté activa y pertenezca al dueño.

## Endpoints

Base: `http://<host>:8084/api/appointments`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/` | Todas las citas (ordenadas por fecha y hora) |
| GET | `/{id}` | Una cita (`404` si no existe) |
| POST | `/` | Crear cita → `201`, queda `Pendiente` |
| PUT | `/{id}/status` | Cambiar estado (`Confirmada`, `Cancelada` o `Realizada`) |
| DELETE | `/{id}` | Eliminar cita → `204` |
| GET | `/{id}/owner` | Citas de un dueño (`id` = ownerId) |
| GET | `/{id}/exists` | `true`/`false` |

**Crear** (`AppointmentRequestDTO`):

```json
{
  "petId": 5,
  "petName": "Rocky",
  "ownerId": 3,
  "ownerName": "Camila Rojas",
  "ownerEmail": "camila.rojas@gmail.com",
  "date": "2026-11-30",
  "time": "10:00",
  "reason": "Vacuna antirrábica",
  "vet": "Dra. Paula Núñez"
}
```

**Cambiar estado:** `{ "status": "Confirmada" }`

**Respuesta** (`AppointmentResponseDTO`): los mismos campos más `id`, `status` y `createdAt`. La hora sale como `HH:mm` y los estados son exactamente `Pendiente`, `Confirmada`, `Realizada`, `Cancelada` (los que usa el front).

**Errores** (misma forma que ms-pets): `{ "timestamp", "status", "error", "message", "details": [] }` — `400` validación/regla de negocio, `404` no encontrado, `409` cambio de estado no permitido, `503` ms-pets no responde (solo si la validación está activa).

Swagger UI: `http://<host>:8084/swagger-ui.html`

## Configuración (variables de entorno)

| Variable | Por defecto | Descripción |
|---|---|---|
| `SERVER_PORT` | `8084` | Puerto HTTP |
| `DB_URL` | `jdbc:mysql://localhost:3306/vitalpet_appointments?...` | JDBC de MySQL |
| `DB_USER` / `DB_PASSWORD` | `root` / `root` | Credenciales de MySQL |
| `DDL_AUTO` | `update` | `update`, `validate`, `create` (⚠ `create` borra los datos) |
| `PETS_VALIDATION_ENABLED` | `false` | `true` = valida la mascota contra ms-pets |
| `PETS_SERVICE_URL` | `http://localhost:8083` | URL base de ms-pets |
| `CORS_ALLOWED_ORIGINS` | `*` | Orígenes permitidos, separados por coma |
| `JAVA_OPTS` (solo Docker) | `-Xmx256m -Xms128m -Duser.timezone=America/Santiago` | Memoria y zona horaria de la JVM |

## Ejecución con Docker

```bash
cp .env.example .env     # y edita las claves
docker compose up -d --build
curl -i http://localhost:8084/api/appointments
```

En EC2 recuerda abrir el puerto **8084** en el grupo de seguridad. La base de datos no se publica fuera de Docker.

## Tests

```bash
mvn test
```

Los tests usan Mockito y datafaker. El reporte de cobertura (JaCoCo) queda en `docs/coverage`.
