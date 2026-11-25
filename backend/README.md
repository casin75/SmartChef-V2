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

## Estado

- [x] Modelo de datos y repositorios JPA
- [ ] Servicios, DTOs y mappers
- [ ] API REST documentada con Swagger
- [ ] Conexión de la app con el backend
