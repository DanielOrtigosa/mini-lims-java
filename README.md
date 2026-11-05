# Mini LIMS

Aplicación web de gestión de muestras de laboratorio desarrollada con Java y Spring Boot. El proyecto sigue un recorrido práctico desde la creación del modelo de datos y las operaciones CRUD hasta la trazabilidad de estados, la autenticación por roles, las pruebas y el despliegue con Docker.

Está inspirada en un flujo de trabajo LIMS y en la experiencia de prácticas en el área LIMS de Kymos. Es un proyecto personal de aprendizaje; no es un producto oficial de Kymos ni de LabWare.

## Qué permite hacer

- Gestionar muestras, resultados, estados y usuarios/técnicos mediante persistencia JPA.
- Crear, consultar, modificar y desactivar muestras y resultados.
- Asignar a una muestra un técnico activo.
- Avanzar una muestra por el flujo `PENDING → IN_ANALYSIS → COMPLETED` y consultar el historial de cambios.
- Iniciar sesión con email y contraseña para obtener un token JWT.
- Aplicar permisos por rol: `ADMIN` gestiona usuarios y estados; los roles `ADMIN`, `TECHNICIAN` y `TECNICO` pueden usar las operaciones de trabajo.
- Probar la API con la colección de Postman incluida.

## Tecnologías

- Java 21 y Maven
- Spring Boot 4, Spring Web, Spring Security y Spring Data JPA
- Hibernate
- H2 en memoria para desarrollo local y PostgreSQL para el despliegue configurado
- JWT con HS256 y contraseñas cifradas con BCrypt
- Docker y Render

## Capas de la aplicación

```mermaid
flowchart TB
    cliente[Cliente REST / Postman] --> web[Controladores<br/>Spring Web]
    web --> servicios[Servicios<br/>Lógica de negocio]
    servicios --> repos[Repositorios<br/>Spring Data JPA]
    repos --> orm[Hibernate]
    orm --> bd[(H2 local<br/>PostgreSQL en Render)]
    seguridad[Spring Security<br/>JWT y BCrypt] -. protege .-> web
    docker[Docker] --> render[Render]
    render --> web
```

## Requisitos

- JDK 21
- Git

## Ejecutar en local

Clona el repositorio y arranca la aplicación:

```bash
git clone https://github.com/DanielOrtigosa/mini-lims-java
cd mini-lims-java
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación queda disponible en `http://localhost:8080`. En local usa una base H2 en memoria, que se reinicia al detener la aplicación.

### Crear el primer administrador

Al arrancar, la aplicación crea un usuario administrador si no existe otro con el email indicado. Define las credenciales mediante variables de entorno:

```powershell
$env:BOOTSTRAP_ADMIN_EMAIL = "admin@example.com"
$env:BOOTSTRAP_ADMIN_PASSWORD = "<contraseña-segura>"
.\mvnw.cmd spring-boot:run
```

Para usar una clave JWT propia, configura también `JWT_SECRET` con una cadena Base64 que represente al menos 32 bytes:

```powershell
$env:JWT_SECRET = "<clave-base64-de-al-menos-32-bytes>"
```

La clave predeterminada de la configuración es solo para desarrollo. No guardes credenciales ni claves reales en el repositorio.

## API

La API REST principal incluye estos recursos:

| Recurso | Rutas principales | Acceso |
|---|---|---|
| Autenticación | `POST /api/auth/login` | Público |
| Muestras | `/api/samples` y `/api/samples/{id}` | JWT y rol operativo |
| Resultados | `/api/results` y `/api/results/{id}` | JWT y rol operativo |
| Estados | `/api/statuses` y `/api/statuses/{id}` | JWT; escritura solo para `ADMIN` |
| Salud | `GET /health` | Público |

Las muestras también disponen de estas operaciones:

```text
PATCH /api/samples/{sampleId}/technician/{technicianId}
POST  /api/samples/{id}/status/advance
GET   /api/samples/{id}/status/history
```

El endpoint de avance aplica el flujo de estados y guarda cada transición en el historial.

### Inicio de sesión

Envía las credenciales de un usuario activo:

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "admin@example.com",
  "password": "tu-contraseña"
}
```

La respuesta incluye `accessToken`. En las peticiones protegidas, envíalo en la cabecera:

```http
Authorization: Bearer <accessToken>
```

## Probar la API con Postman

Importa [`postman/LIMS-Samples.postman_collection.json`](postman/LIMS-Samples.postman_collection.json), configura las variables `adminEmail` y `adminPassword`, ejecuta **Login as configured administrator** y después **Create sample**. La colección guarda el token y el identificador de la muestra para las peticiones siguientes.

## Pruebas automatizadas

Ejecuta las pruebas con Maven:

```bash
./mvnw test
```

En Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Hay pruebas para los servicios de muestras, resultados, estados y usuarios, pruebas REST de creación, consulta, modificación y borrado, y pruebas de seguridad y del administrador inicial. Al desactivar un usuario/técnico se conserva su registro y sus asignaciones para mantener la trazabilidad; el usuario deja de poder iniciar sesión y no puede recibir nuevas asignaciones.

## Despliegue

El repositorio incluye un `Dockerfile` y [`render.yaml`](render.yaml) para desplegar en Render con PostgreSQL. Conecta el repositorio en Render y crea un Blueprint a partir de ese archivo. Configura un email y una contraseña nuevos para el administrador inicial.

Render genera `JWT_SECRET` y proporciona al servicio las credenciales de la base de datos. Tras el despliegue, comprueba `https://<tu-servicio>.onrender.com/health` y utiliza `POST /api/auth/login` para obtener un token.

El Blueprint usa planes gratuitos para facilitar una demo. El servicio puede tardar en reanudarse tras un periodo inactivo y la base de datos gratuita tiene una duración limitada; consulta las condiciones actuales de Render antes de usarlo para conservar datos.

## Recorrido del proyecto

El desarrollo se organizó en etapas: configuración de Spring Boot y conexión a la base de datos; entidades, repositorios y servicios; API REST y pruebas manuales; flujo de estados y trazabilidad; autenticación JWT y permisos por rol; validaciones y pruebas automatizadas; y empaquetado y despliegue. La guía también propone revisiones iterativas y un pulido general del repositorio como parte del proceso.

El proyecto continúa evolucionando y sirve como práctica de Java, SQL y Spring Boot.
