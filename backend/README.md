# SmartChef · Backend

API de SmartChef hecha con Spring Boot. Es la parte de **Acceso a Datos** del proyecto integrado de 2º DAM.

**Tecnologías:** Java 25 · Spring Boot 4 · Spring Data JPA / Hibernate · PostgreSQL · Lombok · Maven · JUnit 5

## Qué incluye (Entrega 1: capa de acceso a datos)

- Base de datos PostgreSQL de 15 tablas (esquema `smartchef`) mapeada con 12 entidades JPA: recetas, ingredientes, pasos, usuarios, despensa, colecciones, notificaciones…
- Claves compuestas con `@EmbeddedId` y `@MapsId` para las tablas intermedias con datos propios (receta-ingrediente, despensa).
- Todas las relaciones con carga `LAZY`, y consultas `JOIN FETCH` para traer lo necesario sin caer en el problema N+1.
- 10 repositorios con consultas derivadas, JPQL, consultas nativas y operaciones `@Modifying` con `@Transactional`.
- Tests de repositorio con `@DataJpaTest` y un `CommandLineRunner` que comprueba la persistencia al arrancar.
- El esquema se valida al iniciar (`ddl-auto=validate`) y las credenciales van en variables de entorno, no en el código.

## Cómo ejecutarlo

Necesitas Java 25 y PostgreSQL. Primero crea el esquema y las 15 tablas con el script [`src/main/resources/db/Script_SmartChef.sql`](src/main/resources/db/Script_SmartChef.sql). Al final del script hay datos de prueba comentados; descoméntalos si quieres empezar con recetas, ingredientes y usuarios de ejemplo.

Después configura la conexión con variables de entorno:

```bash
cd backend
export DB_URL=jdbc:postgresql://localhost:5432/tu_base_de_datos
export DB_USER=tu_usuario
export DB_PASSWORD=tu_contraseña
export SCHEMA=smartchef

./mvnw spring-boot:run      # en Windows: mvnw.cmd spring-boot:run
./mvnw test                 # ejecutar los tests
```

## Entrega 2: servicios y API REST de recetas

Encima de la capa de datos he montado la API del recurso **recetas**, por capas:

- **DTOs** (`dto/request` y `dto/response`) como `record`, con validación (`@NotBlank`, `@Positive`, `@Size`...). El cliente nunca ve las entidades.
- **Mapper** con MapStruct (`RecetaMapper`) para pasar de entidad a DTO y al revés.
- **Servicio** con interfaz e implementación (`RecetaService` y `RecetaServiceImpl`), `@Transactional(readOnly = true)` en la clase y `@Transactional` en los métodos que escriben. Aquí están las reglas de negocio, comprobadas con `if`.
- **Controlador** `RecetaController` en `/api/v1/recetas`, documentado con Swagger.
- **Errores** con excepciones propias y un `@RestControllerAdvice` que devuelve siempre el mismo formato (`ProblemDetail`).

### Endpoints

| Método | Ruta | Qué hace | Respuesta |
|--------|------|----------|-----------|
| GET | `/api/v1/recetas?nombre=&page=&size=&sort=` | Lista paginada, filtrando por nombre | 200 |
| GET | `/api/v1/recetas/{id}` | Ficha completa con pasos, ingredientes y fotos | 200 / 404 |
| POST | `/api/v1/recetas` | Crea una receta | 201 + `Location` |
| PUT | `/api/v1/recetas/{id}` | Edita una receta | 200 / 404 |
| DELETE | `/api/v1/recetas/{id}` | Borra una receta | 204 / 404 / 409 |

### Reglas de negocio

| Regla | Error |
|-------|-------|
| No puede haber dos recetas con el mismo nombre | 409 |
| No se puede borrar una receta que ya está en el historial de alguien | 409 |
| Una receta no puede llevar el mismo ingrediente dos veces | 422 |
| El ingrediente tiene que existir | 404 |
| Campos obligatorios y rangos (tiempo > 0, raciones de 1 a 20, dificultad de 1 a 3) | 400 |

### Probarla

Con la app arrancada:

- **Postman:** importa la colección [`docs/SmartChef.postman_collection.json`](docs/SmartChef.postman_collection.json). Tiene las peticiones correctas y los casos de error. Los datos de prueba se crean con estas mismas peticiones.
- **Swagger:** la documentación de la API está en http://localhost:8080/swagger-ui.html

## Estado

- [x] Modelo de datos y repositorios JPA
- [x] Servicios, DTOs y mappers
- [x] API REST de recetas documentada con Swagger
- [ ] Resto de recursos (ver [`docs/plan-requisitos.md`](docs/plan-requisitos.md))
- [ ] Conexión de la app con el backend
