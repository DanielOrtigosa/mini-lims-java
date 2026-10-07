# Mini LIMS

Sistema de gestión de muestras de laboratorio (LIMS), inspirado en la experiencia real adquirida durante las prácticas en Kymos (Departamento LIMS, software Labware).

## Estado del proyecto: EN DESARROLLO

*Funcionalidades ya implementadas:*
- Modelo de datos: Muestra, Tecnico, Resultado, Estado (JPA/Hibernate), con sus relaciones (@OneToMany, @ManyToOne).
- Repositorios JPA para las 4 entidades.
- Capa de servicios con reglas de negocio: validación de estados de muestra, asignación de técnicos y gestión de casos límite (p. ej. muestras sin técnico asignado, técnicos con varias muestras).

*Pendiente:*
- Endpoints REST (controladores).
- Tests (JUnit).
- Despliegue con Docker.

## Stack técnico

- Java + Spring Boot
- Spring Data JPA / Hibernate
- Base de datos: Oracle
- Control de versiones: Git / GitHub

## Por qué Oracle

Se ha elegido Oracle como motor de base de datos por ser el mismo ecosistema usado en el entorno LIMS/Labware de las prácticas, lo que permite practicar con el mismo tipo de particularidades (tipos de dato, secuencias, etc.) que se encuentran en un entorno profesional real.

## Modelo de datos

| Tabla | Descripción |
|---|---|
| Muestra | Muestra de laboratorio: código, fecha de entrada, estado, técnico asignado. |
| Tecnico | Persona que analiza las muestras: nombre, email, rol (técnico/admin). |
| Resultado | Resultado del análisis de una muestra: valor, fecha. |
| Estado | Fase de una muestra: pendiente, en análisis, completado. |

## Cómo ejecutarlo

1. Clonar el repositorio.
2. Copiar application.properties.example como application.properties y configurar tu conexión a Oracle.
3. Ejecutar con:

   ./mvnw spring-boot:run

## Autenticación JWT (API de estados)

La API de estados (`/api/statuses/**`) requiere un token JWT. Inicia sesión con un usuario activo mediante:

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{"email":"admin@example.com","password":"tu-contraseña"}
```

La respuesta contiene `accessToken`. Envíalo en las siguientes peticiones como `Authorization: Bearer <accessToken>`. Las lecturas requieren autenticación; crear, actualizar y borrar estados requiere el rol `ADMIN`.

Las contraseñas nuevas guardadas a través de `LimsUserServices.addUser` se cifran con BCrypt. Define `JWT_SECRET` como una clave Base64 de al menos 32 bytes antes de desplegar la aplicación; el valor predeterminado de `application.properties` sirve solo para desarrollo local.


## Contexto

Proyecto personal desarrollado para reforzar Java, SQL y Spring Boot tras finalizar el Ciclo Formativo de Grado Superior en Desarrollo de Aplicaciones Multiplataforma (DAM) y las prácticas en Kymos.
