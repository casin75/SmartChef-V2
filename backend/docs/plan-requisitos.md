# Plan del backend según los requisitos (Caso 2 · SmartChef)

Comparo lo que pide el documento `Proyecto_Backend_2DAM.pdf` con lo que ya tengo en `backend/`.
Leyenda: ✅ hecho · 🟡 hecho a medias · ❌ falta

## 1. Los 16 endpoints de negocio

| # | Método | Ruta | Rol | Estado | Qué falta |
|---|--------|------|-----|--------|-----------|
| 1 | GET | `/api/v1/recetas` | Público | 🟡 | Filtros vegetariano, sinGluten, rápido, económico, dificultad y tiempoMax con Specification. Que no salgan las retiradas. Preferencias del perfil por defecto (RN-03) |
| 2 | GET | `/api/v1/recetas/{id}` | Público | 🟡 | Añadir coste y kcal por ración y las preferencias calculadas (RN-02) |
| 3 | POST | `/api/v1/recetas` | CHEF | 🟡 | Guardar el autor (el chef logueado). RN-01: 1 a 30 ingredientes, 1 a 12 raciones, al menos un paso |
| 4 | PUT | `/api/v1/recetas/{id}` | CHEF autor, ADMIN | 🟡 | Solo el autor o un admin, si no 403 (RN-10) |
| 5 | DELETE | `/api/v1/recetas/{id}` | CHEF autor, ADMIN | 🟡 | Cambiar el borrado por baja lógica (`activa = false`) (RN-09) |
| 6 | GET | `/api/v1/ingredientes?texto=` | Público | ❌ | Controlador y servicio de ingredientes, paginado (la consulta ya existe) |
| 7 | POST | `/api/v1/recetas/buscar-por-ingredientes` | USUARIO | ❌ | Más del 50 %, ordenado por porcentaje, con los que faltan, sin caducados (RN-04) |
| 8 | GET | `/api/v1/despensa` | USUARIO | ❌ | Mi despensa ordenada por caducidad |
| 9 | PUT | `/api/v1/despensa/{ingredienteId}` | USUARIO | ❌ | Añadir o actualizar; cantidad 0 lo borra |
| 10 | POST | `/api/v1/listas-compra/desde-receta` | USUARIO | ❌ | Escalar a raciones, restar despensa, sumar a la lista abierta (RN-05) |
| 11 | PATCH | `/api/v1/listas-compra/{id}/cerrar` | USUARIO (propia) | ❌ | Lo comprado pasa a la despensa y la lista queda CERRADA (RN-06) |
| 12 | GET | `/api/v1/colecciones` | USUARIO | ❌ | Mis colecciones con sus recetas |
| 13 | PUT | `/api/v1/colecciones/{nombre}/recetas/{recetaId}` | USUARIO | ❌ | La crea si no existe, sin recetas repetidas (RN-08) |
| 14 | POST | `/api/v1/registros` | USUARIO | ❌ | Uno por día y momento (409), sin fechas futuras, descontar despensa (RN-07) |
| 15 | GET | `/api/v1/registros/semana?fecha=` | USUARIO | ❌ | La semana por días con las kcal |
| 16 | GET | `/api/v1/informes/recetas-populares?desde=&hasta=` | ADMIN, CHEF | ❌ | Más cocinadas y más guardadas |

Endpoints comunes y avanzados (Fases 4 y 5):

| Método | Ruta | Rol | Estado |
|--------|------|-----|--------|
| POST | `/api/v1/auth/registro` | Público | ❌ (al registrarse se crea la colección «Favoritas») |
| POST | `/api/v1/auth/login` | Público | ❌ (devuelve JWT de 60 min) |
| POST | `/api/v1/recetas/{id}/fotos` | CHEF autor | ❌ (multipart, `portada=true`, máx. 1 portada + 4) |
| GET | `/api/v1/fotos/{id}` | Público | ❌ |
| GET | `/api/v1/notificaciones?leida=` | Autenticado | ❌ |
| PATCH | `/api/v1/notificaciones/{id}/leida` | Autenticado | ❌ |

## 2. Las 16 consultas (cada una usada desde un servicio)

| Tipo | Nº | Consulta | Estado |
|------|----|----------|--------|
| Q1 derivada | 5 | `findByNombreContainingIgnoreCaseAndActivaTrue` | 🟡 existe sin `Activa` |
| | | `findByUsuarioIdAndFechaBetweenOrderByFechaAsc` (registros) | 🟡 existe en UsuarioReceta |
| | | `existsByUsuarioIdAndFechaAndMomento` | ❌ |
| | | `findByUsuarioIdAndNombreIgnoreCase` (colecciones) | ❌ |
| | | `findByUsuarioIdAndEstado` (lista abierta) | ❌ |
| Q2 JPQL | 3 | Vegetarianas / sin gluten con `NOT EXISTS` | ❌ |
| | | Coste por ración con `SUM` + `JOIN` | 🟡 `calcularCoste` (falta dividir por raciones) |
| | | Más cocinadas con `COUNT` + `GROUP BY` | ✅ `recetasMasRegistradas` |
| Q3 nativa | 2 | Coincidencia con la despensa en % (`GROUP BY` + `HAVING`) | 🟡 `buscarPorCoincidenciaConDespensa` (falta el `HAVING > 50 %` y quitar caducados) |
| | | Kcal por día de la semana con `DATE_TRUNC('week', …)` | ❌ (hay un informe mensual) |
| Q4 @Modifying | 1 | Borrar caducados hace más de 7 días | ✅ casi, falta usarla desde la tarea programada |
| Q5 proyección | 2 | Interfaz `RecetaTarjetaView` para el catálogo | ❌ |
| | | `SELECT new ...ResumenDiaDto(...)` para la semana | ❌ |
| Q6 Pageable | 1 | Endpoints 1 y 6 | ✅ |
| Q7 Specification | 1 | Filtros del endpoint 1 (`JpaSpecificationExecutor`) | ❌ |
| Q8 N+1 | 1 | `@EntityGraph` / `JOIN FETCH` para el detalle | ✅ (dos consultas para evitar `MultipleBagFetchException`) |

## 3. Cambios en el modelo

- Enums con `@Enumerated(EnumType.STRING)`: `Dificultad` (FACIL, MEDIA, DIFICIL), `Unidad` (GRAMOS, MILILITROS, UNIDADES), `Rol` (ADMIN, CHEF, USUARIO), `Momento` (DESAYUNO, COMIDA, CENA), `EstadoLista` (ABIERTA, CERRADA), `TipoNotificacion`. Ahora son `Integer`.
- `Receta`: `autor` (Usuario chef), `activa` (baja lógica), `creadoEn` y `actualizadoEn` con `@PrePersist` / `@PreUpdate`.
- `Ingrediente`: `vegetariano` y `contieneGluten` (boolean).
- `Usuario`: `email` único, `rol` enum, y las preferencias del perfil (vegetariano, sinGluten, rapido, economico). La tabla `Preferencia` actual sobra porque las preferencias se calculan (RN-02).
- Nuevas: `ListaCompra` y `LineaListaCompra` (cambian el `@ManyToMany listaCompra` de Usuario).
- `UsuarioReceta` pasa a `RegistroComida`: `fecha` (LocalDate), `momento`, `receta` opcional y `descripcion` para comida libre.
- `Foto` pasa a `FotoReceta`: `portada`, nombre del fichero (UUID), tipo y tamaño.
- `Notificacion`: `usuario`, `leida`, `creadaEn`.
- Una excepción propia más (4 mínimo), por ejemplo `FicheroNoValidoException`.

## 4. Fases que vienen después

- **Fase 4 · Seguridad:** Spring Security + JWT, BCrypt, 3 roles, `@PreAuthorize` en 3 métodos como mínimo, reglas de propiedad (lista propia, receta del autor), 401 y 403.
- **Fase 5 · Avanzado:** fotos (jpeg/png/webp, 5 MB, 413/415), notificaciones y tareas `@Scheduled` (cada día a las 09:00 avisos de caducidad y limpieza; los lunes resumen semanal) con el cron en `application.yml`.
- **Fase 6 · Cierre:** `compose.yaml` con PostgreSQL 16, README con la tabla de consultas y decisiones, colección de Postman en `docs/`, Swagger con Bearer.

## 5. Dudas para el profesor

1. El documento pide al menos un validador propio, pero en clase me dijo que lo haga en el servicio con `if`. ¿Cuál sigo?
2. Pide `schema.sql` y `data.sql`, pero también me dijo que los datos los meta por la API con Postman. ¿Las dos cosas?
3. Uso Spring Boot 4 / Java 25 y el paquete `com.example.smartchefjava`; el documento dice Boot 3 / Java 21 y `es.safareyes.smartchef`. ¿Hace falta cambiarlo?

## 6. Orden de trabajo

1. Ajustar el modelo (enums, campos nuevos, entidades nuevas).
2. Terminar recetas (endpoints 1 a 5) con Specification y baja lógica.
3. Ingredientes y despensa (6, 8, 9).
4. Búsqueda por ingredientes (7).
5. Listas de la compra (10, 11).
6. Colecciones (12, 13).
7. Registros e informes (14, 15, 16).
8. Seguridad, ficheros, notificaciones y tareas.
9. README, Postman y cierre.
