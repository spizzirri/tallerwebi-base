# [FOLLOW] Seguir usuarios y feed de planes de los seguidos

> Trello: https://trello.com/c/XQhci96Z/41-follow-ability-to-follow-a-user-and-having-a-feed-section-where-the-public-plan-created-by-those-you-follow-are-shown
> **Puntos:** 5
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.

## Objetivo

Un usuario con sesion puede **seguir y dejar de seguir** a otro usuario desde su perfil publico, y
**ver los planes publicos de la gente que sigue** en un scope propio del feed `/for-you`. El perfil
suma los contadores reales de seguidores/seguidos y una seccion `#follow` con las dos listas; la
entrada "Siguiendo" del modal del navbar, hoy inerte, pasa a navegar a esa seccion.

Esta card **no define** el criterio de popularidad ni los filtros del feed: los consume de [TOP]
(`17-TOP.md`) y [SEARCH] (`18-SEARCH.md`). El scope "Siguiendo" es el mismo conjunto publico,
restringido a los autores seguidos, con los mismos filtros y el mismo orden.

## Pre-requisitos

- [PROF] completed: vista publica `/profile/{id}`, contador placeholder `followerCount = 0`, anchors
  `#bookmarks` / `#follow` y la entrada "Siguiendo" deshabilitada en el modal del navbar.
- [PPV] completed: `GET /for-you`, `PlanFeedCards`, exclusion de los planes propios.
- [SEARCH] completed: `PlanDiscoveryQuery`, `PlanDiscovery.discover`, formulario GET y estados vacios.
- [CAL] completed: patron de entidad con `@UniqueConstraint` y soft gate anonimo a
  `/auth/login?redirect=`.

## Decisiones / aclaraciones

- **Entidad propia, sin coleccion inversa.** `Follow` (`follows`, unique `follower_id`/`followed_id`)
  sigue el patron de `PlanRating`: `User` no gana `@ManyToMany` ni `Set<User>` que se pueda recorrer.
  Los conteos y las listas son consultas agregadas, no un `size()` sobre una coleccion LAZY.
- **El feed no es una pagina nueva.** `CONTEXT.md` define *Feed* como `/for-you`; el scope viaja en
  `scope=following` y reusa cards, filtros, orden y estados vacios existentes. Una ruta `/following`
  duplicaria `public-list.html` y partiria el glosario.
- **El "Siguiendo" del navbar apunta a `/profile#follow`.** `/profile` ya redirige al perfil propio
  canonico (`/profile/{id}`) y el fragmento sobrevive al 302. No hace falta resolver el id del
  usuario en el navbar ni tocar `CurrentUserControllerAdvice`.
- **La seccion `#follow` vive en dos templates.** El perfil propio renderiza `edit.html` (con
  `#follow` como placeholder "A futuro" hoy) y el ajeno `view.html` (sin anchor). Las dos necesitan
  las listas: es la misma seccion con distinto encabezado. No hay tercer template.
- **Seguir es un POST con CSRF.** La identidad del seguidor sale de `Authentication`, nunca del
  path: `/profile/{id}/follow` solo nombra al **seguido**. No existe endpoint con "como este
  usuario" — esa es la defensa de AC-13.
- **Idempotencia.** Seguir dos veces no duplica (el unique es la ultima defensa, el servicio la
  primera); dejar de seguir a quien no se sigue es un no-op. Ninguna de las dos rompe la vista.
- **Self-follow no es un error visible.** El perfil propio no muestra boton y un POST directo al
  propio id redirige sin crear nada: no hay motivo para un 400 por una accion que el UI no ofrece.
- **El anonimo hace soft gate, no 403.** Mismo patron de [CAL]: el boton es un link a
  `/auth/login?redirect=/profile/{id}` y `scope=following` sin sesion redirige al login con vuelta a
  `/for-you?scope=following`. El follow anonimo por POST lo corta la cadena de seguridad con 302.
- **Los filtros aplican al scope Siguiendo.** `q`, `category`, `when` y `sort` significan lo mismo en
  los dos scopes: el scope elige el conjunto, [SEARCH] lo filtra y lo ordena. El formulario conserva
  el scope con un hidden input; sin el, buscar perderia la pestana.
- **Estados vacios distintos.** "Todavia no seguis a nadie" (con salida al feed completo) no es "los
  usuarios que seguis no tienen planes publicos" ni el estado historico de [PPV].
- **Los planes privados siguen invisibles.** Un autor seguido no gana visibilidad: solo entran sus
  planes publicos. El filtro `isPublic = true` no se relaja por seguir.
- **Los planes propios no aparecen.** No se puede seguir a uno mismo, pero el filtro de planes
  propios de [PPV] se mantiene igual en los dos scopes: la regla no depende del follow.
- **Sin paginacion en las listas.** Las listas de seguidores/seguidos se renderizan completas y
  ordenadas por nombre. Es una limitacion aceptada para el TP; paginar es card aparte.
- **Sin notificaciones.** Avisar de un plan nuevo de un seguido es [NOTIF]; esta card solo cambia lo
  que se ve cuando el usuario entra al feed.

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | Un usuario autenticado puede seguir a otro que exista desde `/profile/{id}`; se crea una unica relacion por par `(follower, followed)` y el boton pasa a "Dejar de seguir" |
| AC-02 | Dejar de seguir elimina la relacion; repetir seguir o dejar de seguir no duplica ni rompe (idempotencia) |
| AC-03 | No se puede seguir a uno mismo: el perfil propio no muestra el boton y un POST directo al propio id no crea relacion ni responde 500 |
| AC-04 | La vista publica del perfil muestra el contador real de seguidores y de seguidos, en lugar del placeholder `followerCount = 0` |
| AC-05 | El perfil tiene una seccion `#follow` con las listas de Seguidores y Seguidos, cada entrada con nombre/avatar y link a `/profile/{id}`; una lista vacia muestra su propio mensaje y la seccion existe tanto en `view.html` como en `edit.html` |
| AC-06 | En la variante autenticada del modal del navbar, la entrada "Siguiendo" es navegable y lleva a `/profile#follow`; deja de ser un span con "Pronto". La variante anonima no cambia |
| AC-07 | `GET /for-you` expone dos scopes: "Para vos" (default, comportamiento actual) y "Siguiendo"; el scope viaja como `scope=following` y la URL resultante es compartible |
| AC-08 | El scope Siguiendo muestra solo planes publicos cuyo administrador es un usuario seguido, aplicando los mismos `q`/`category`/`when`/`sort` de [SEARCH] y el mismo orden de [TOP], y nunca incluye planes propios |
| AC-09 | Un usuario que no sigue a nadie ve en el scope Siguiendo un estado vacio propio ("Todavia no seguis a nadie" con salida al feed completo), distinto de los estados vacios de [PPV]/[SEARCH] |
| AC-10 | Sin sesion, pedir `scope=following` redirige a `/auth/login` con vuelta a `/for-you?scope=following`, y el boton Seguir del perfil es un link al login con vuelta al perfil: no existe follow anonimo |
| AC-11 | Ningun plan privado de un autor seguido aparece en el scope Siguiendo, ni en el HTML ni en la API de planes publicos |
| AC-12 | El perfil publico y la seccion `#follow` no exponen email ni rol: solo nombre, avatar y link al perfil |
| AC-13 | La relacion solo se crea o elimina desde un POST autenticado con CSRF y la identidad sale de la sesion: no hay parametro que permita operar en nombre de otro usuario |
| AC-14 | Los datos demo incluyen relaciones de follow idempotentes para que el scope Siguiendo tenga contenido en demo y en E2E |
| AC-15 | El scope Siguiendo no agrega una query por plan: los autores se resuelven en una sola consulta y los conteos del perfil son agregados |

## Escenarios de Test

### Tests Unitarios (`infrastructure/follow/FollowServiceImplTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | Seguir crea la relacion y deja `isFollowing` en verdadero | AC-01 |
| U-02 | Seguir dos veces no duplica (el segundo intento es no-op) | AC-02 |
| U-03 | Dejar de seguir elimina la relacion y es idempotente cuando no existia | AC-02 |
| U-04 | Seguirse a si mismo lanza y no persiste nada | AC-03 |
| U-05 | Seguir a un usuario inexistente lanza `UserNotFoundException` | AC-01 |
| U-06 | `isFollowing` es falso sin relacion y verdadero con relacion | AC-01 |
| U-07 | Los conteos de seguidores y seguidos cuentan solo las relaciones del usuario | AC-04 |
| U-08 | Las listas de seguidores y seguidos llegan ordenadas por nombre y sin duplicados | AC-05 |

### Tests Unitarios (`presentation/profile/ProfileControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-09 | La vista publica expone conteos reales, `isFollowing` y las dos listas | AC-04, AC-05 |
| U-10 | El perfil propio no expone el boton de seguir y muestra la seccion `#follow` con las listas | AC-03, AC-05 |

### Tests Unitarios (`presentation/plan/ForYouControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-11 | Con `scope=following`, el controller pide los planes de los autores seguidos y mantiene filtros y orden | AC-07, AC-08 |
| U-12 | Con `scope=following` y sin seguidos, el modelo marca el estado vacio propio | AC-09 |

### Tests de Integracion (`integration/ProfileFollowIntegrationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | `POST /profile/{id}/follow` con sesion crea la relacion y redirige al perfil | AC-01 |
| I-02 | `POST /profile/{id}/follow` sin token CSRF responde 403 | AC-13 |
| I-03 | `POST /profile/{id}/unfollow` revierte la relacion y baja el contador | AC-02, AC-04 |
| I-04 | `POST /profile/{id}/follow` sin sesion redirige a `/auth/login` | AC-10 |
| I-05 | `POST` al propio id no crea relacion y no responde 500 | AC-03 |
| I-06 | Tras seguir, el HTML del perfil muestra el nuevo seguidor en `#follow` y el contador actualizado | AC-04, AC-05 |

### Tests de Integracion (`integration/ForYouFollowingIntegrationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-07 | `GET /for-you?scope=following` lista solo planes publicos de autores seguidos | AC-07, AC-08 |
| I-08 | `scope=following` respeta `q`, `category`, `when` y `sort` sobre el conjunto de seguidos | AC-08 |
| I-09 | Un usuario sin seguidos ve el estado vacio "Todavia no seguis a nadie" | AC-09 |
| I-10 | El scope Siguiendo no incluye planes privados de los seguidos ni planes propios | AC-08, AC-11 |

### Tests de Seguridad (`integration/FollowSecurityTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| S-01 | Un POST anonimo a `/profile/{id}/follow` termina en el login, sin crear relacion | AC-10, AC-13 |
| S-02 | No existe parametro de usuario en el POST: la relacion se crea solo para el usuario de la sesion | AC-13 |
| S-03 | El HTML de `scope=following` no filtra planes privados ni emails de los autores | AC-11, AC-12 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `FollowE2E` | Seguir a otro usuario desde su perfil, ver su plan publico en el scope Siguiendo, dejar de seguirlo y ver que desaparece | AC-01, AC-02, AC-07, AC-08 |
| E-02 | `ProfileViewE2E` | Entrada "Siguiendo" activa y navegable al anchor, contadores reales y seccion `#follow` con las dos listas | AC-04, AC-05, AC-06 |

## Referencia de Implementacion

> Los pasos a continuacion son guia de implementacion, no reemplazan los criterios de aceptacion.

### 1. Entidad

File: `src/main/java/com/valhalla/domain/follow/Follow.java` (crear)

```java
package com.valhalla.domain.follow;

import com.valhalla.domain.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** [FOLLOW] Arista dirigida follower -> followed. Sin coleccion inversa en User (patron PlanRating). */
@Entity
@Table(
  name = "follows",
  uniqueConstraints = @UniqueConstraint(columnNames = { "follower_id", "followed_id" })
)
public class Follow {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "follower_id", nullable = false)
  private User follower;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "followed_id", nullable = false)
  private User followed;

  public Follow() {}

  public Follow(User follower, User followed) {
    this.follower = follower;
    this.followed = followed;
  }

  public Long getId() {
    return id;
  }

  public User getFollower() {
    return follower;
  }

  public User getFollowed() {
    return followed;
  }
}
```

`hbm2ddl.auto=update` crea la tabla y el unique: el proyecto no tiene migraciones (ver `docs/setup.md`).

### 2. Repositorios

File: `src/main/java/com/valhalla/domain/follow/FollowRepository.java` (crear)

```java
public interface FollowRepository {
  void save(Follow follow);
  boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);
  void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);
  long countByFollowedId(Long followedId);
  long countByFollowerId(Long followerId);
  List<User> findFollowers(Long followedId);
  List<User> findFollowing(Long followerId);
  List<Long> findFollowedIds(Long followerId);
}
```

File: `src/main/java/com/valhalla/infrastructure/follow/JpaFollowRepository.java` (crear)

```java
public interface JpaFollowRepository extends JpaRepository<Follow, Long> {
  boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);
  long countByFollowedId(Long followedId);
  long countByFollowerId(Long followerId);
  void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);

  @Query(
    "select f.follower from Follow f where f.followed.id = :userId " +
    "order by lower(f.follower.firstName), lower(f.follower.lastName), f.follower.id"
  )
  List<User> findFollowers(Long userId);

  @Query(
    "select f.followed from Follow f where f.follower.id = :userId " +
    "order by lower(f.followed.firstName), lower(f.followed.lastName), f.followed.id"
  )
  List<User> findFollowing(Long userId);

  /** Una sola consulta alimenta el scope Siguiendo: sin N+1 (AC-15). */
  @Query("select f.followed.id from Follow f where f.follower.id = :userId")
  List<Long> findFollowedIds(Long userId);
}
```

`FollowRepositoryImpl` en el mismo paquete delega en el JPA, como `PlanRepositoryImpl`.

### 3. Servicio

File: `src/main/java/com/valhalla/domain/follow/FollowService.java` (crear)

```java
public interface FollowService {
  void follow(Long followerId, Long followedId);
  void unfollow(Long followerId, Long followedId);
  boolean isFollowing(Long followerId, Long followedId);
  long countFollowers(Long userId);
  long countFollowing(Long userId);
  List<User> followers(Long userId);
  List<User> following(Long userId);
  List<Long> followedIds(Long followerId);
}
```

File: `src/main/java/com/valhalla/infrastructure/follow/FollowServiceImpl.java` (crear)

```java
@Service
public class FollowServiceImpl implements FollowService {

  private final FollowRepository followRepository;
  private final UserService userService;

  @Override
  @Transactional
  public void follow(Long followerId, Long followedId) {
    if (followerId.equals(followedId)) {
      throw new IllegalArgumentException("No podes seguirte a vos mismo");
    }
    User follower = userService.findById(followerId);
    User followed = userService.findById(followedId); // UserNotFoundException -> 404
    if (followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
      return; // idempotente
    }
    followRepository.save(new Follow(follower, followed));
  }

  // unfollow: si la relacion no existe, no hace nada (mismo espiritu)
}
```

`userService.findById` ya existe y lanza `UserNotFoundException`, que `ProfileController` traduce a
404 (AC-20 de [PROF]).

### 4. Plans de un conjunto de autores

File: `src/main/java/com/valhalla/infrastructure/plan/JpaPlanRepository.java` (actualizar)

```java
/** [FOLLOW] Planes publicos de un conjunto de autores, en una sola consulta (AC-08, AC-15). */
List<Plan> findByIsPublicTrueAndAdministratorIdIn(Collection<Long> administratorIds);
```

Se expone por `PlanRepository` (puerto) y `PlanServiceImpl` como
`getPublicPlansByAdministrators(Collection<Long>)` con `@Transactional(readOnly = true)`. El orden y
los filtros siguen siendo de `PlanDiscovery`: la query no ordena.

### 5. Controller de perfil

File: `src/main/java/com/valhalla/presentation/profile/ProfileController.java` (actualizar)

```java
@PostMapping("/{id:[\\d]+}/follow")
public ModelAndView follow(@PathVariable Long id, Authentication authentication) {
  User me = sessionUser(authentication);
  if (!me.getId().equals(id)) {
    followService.follow(me.getId(), id);
  }
  return new ModelAndView("redirect:/profile/" + id);
}

@PostMapping("/{id:[\\d]+}/unfollow")
public ModelAndView unfollow(@PathVariable Long id, Authentication authentication) {
  followService.unfollow(sessionUser(authentication).getId(), id);
  return new ModelAndView("redirect:/profile/" + id);
}
```

`publicModel(owner, authentication)` suma:

```java
model.put("followerCount", followService.countFollowers(owner.getId()));
model.put("followingCount", followService.countFollowing(owner.getId()));
model.put("followers", followService.followers(owner.getId()));
model.put("following", followService.following(owner.getId()));
model.put("isFollowing", authenticated
  ? followService.isFollowing(sessionUser(authentication).getId(), owner.getId())
  : false);
model.put("isSelf", false); // el self ni siquiera entra por aca: renderiza edit.html
```

`editModel` recibe los mismos cuatro valores para su propia seccion `#follow`. Las rutas POST caen
en la cadena default de `SecurityConfig` (`anyRequest().authenticated()`): no hace falta tocar la
configuracion.

### 6. Feed con scope

File: `src/main/java/com/valhalla/presentation/plan/ForYouController.java` (actualizar)

```java
@RequestParam(required = false) String scope
```

```java
boolean followingScope = "following".equalsIgnoreCase(scope == null ? "" : scope.trim());
boolean authenticated = authentication != null && authentication.getPrincipal() instanceof UserDetails;
if (followingScope && !authenticated) {
  return new ModelAndView("redirect:/auth/login?redirect=/for-you%3Fscope%3Dfollowing");
}
List<Plan> source = followingScope
  ? planService.getPublicPlansByAdministrators(followService.followedIds(currentUser.getId()))
  : planService.getPublicPlans();
```

El resto del handler no cambia: filtro de planes propios, summaries, participantes,
`PlanDiscovery.discover(...)` y `PlanFeedCards.planCard(...)`. Al modelo se agregan
`followingScope` y `followsNobody` (seguidos vacio) para los estados vacios.

### 7. Templates

File: `templates/pages/profile/view.html` (actualizar): boton de seguir + seccion `#follow`.

```html
<div sec:authorize="isAuthenticated()">
  <form method="post"
    th:action="${isFollowing} ? @{/profile/{id}/unfollow(id=${owner.id})} : @{/profile/{id}/follow(id=${owner.id})}">
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">
    <button type="submit" class="rounded-md bg-primary px-4 py-2 font-medium text-white"
      th:text="${isFollowing} ? 'Dejar de seguir' : 'Seguir'">Seguir</button>
  </form>
</div>
<div sec:authorize="!isAuthenticated()">
  <a th:href="@{/auth/login(redirect=${'/profile/' + owner.id})}"
    class="rounded-md bg-primary px-4 py-2 font-medium text-white" id="btn-follow-anon">Seguir</a>
</div>

<section id="follow" class="space-y-4">
  <h2 class="text-lg font-semibold text-gray-900">Seguidores y seguidos</h2>
  <p class="text-sm text-gray-500">
    <span th:text="${followerCount}">0</span> seguidores ·
    <span th:text="${followingCount}">0</span> seguidos
  </p>
  <!-- dos listas: followers / following, cada item link a /profile/{id} con nombre o inicial.
       Empty state propio por lista. Sin email ni rol (AC-12). -->
</section>
```

File: `templates/pages/profile/edit.html` (actualizar): reemplazar el placeholder "A futuro" de
`#follow` por la misma seccion con las listas del usuario en sesion.

File: `templates/components/navbar-menu.html` (actualizar): la entrada "Siguiendo" pasa de `span`
`data-soon` a link en la variante autenticada.

```html
<a role="menuitem" th:href="@{/profile#follow}" id="menu-following"
  class="flex items-center gap-2 px-4 py-2 text-gray-700 hover:bg-gray-100">
  <!-- mismo icono de usuarios; sin badge "Pronto" -->
  Siguiendo
</a>
```

File: `templates/pages/plans/public-list.html` (actualizar): selector de scope sobre el formulario.

```html
<nav class="mb-4 flex gap-2" aria-label="Alcance del feed">
  <a th:href="@{/for-you}" th:classappend="${followingScope} ? 'bg-white text-gray-700' : 'bg-primary text-white'"
    class="rounded-full px-4 py-2 text-sm font-medium">Para vos</a>
  <a th:href="@{/for-you(scope='following')}" th:classappend="${followingScope} ? 'bg-primary text-white' : 'bg-white text-gray-700'"
    class="rounded-full px-4 py-2 text-sm font-medium">Siguiendo</a>
</nav>

<!-- dentro del form GET existente: el scope no se pierde al buscar -->
<input type="hidden" name="scope" value="following" th:if="${followingScope}" />
```

Estados vacios del scope, en este orden de `th:if`:

- `${publicPlans.isEmpty() and followingScope and followsNobody}` -> "Todavia no seguis a nadie" +
  link a `/for-you` y a `/explore`.
- `${publicPlans.isEmpty() and followingScope and !followsNobody}` -> "Los usuarios que seguis no
  tienen planes publicos".
- El estado de filtros de [SEARCH] sigue aplicando despues, sin cambios.

### 8. Demo

File: `src/main/java/com/valhalla/config/PublicPlanDataSeeder.java` (actualizar): relaciones de
follow idempotentes entre los usuarios demo (chequear `existsByFollowerIdAndFollowedId` antes de
insertar), de modo que el scope Siguiendo tenga al menos dos planes publicos en E2E y demo.

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `domain/follow/Follow.java` | Crear (entidad + unique) |
| `domain/follow/FollowRepository.java` | Crear (puerto) |
| `domain/follow/FollowService.java` | Crear (interfaz) |
| `infrastructure/follow/FollowRepositoryImpl.java` | Crear (delegacion al JPA) |
| `infrastructure/follow/JpaFollowRepository.java` | Crear (exists, delete, counts, listas, ids) |
| `infrastructure/follow/FollowServiceImpl.java` | Crear (reglas de AC-01..AC-03) |
| `domain/plan/PlanRepository.java` | Actualizar (planes por conjunto de autores) |
| `domain/plan/PlanService.java` | Actualizar (`getPublicPlansByAdministrators`) |
| `infrastructure/plan/PlanRepositoryImpl.java` | Actualizar (delegacion) |
| `infrastructure/plan/PlanServiceImpl.java` | Actualizar (readOnly) |
| `infrastructure/plan/JpaPlanRepository.java` | Actualizar (`findByIsPublicTrueAndAdministratorIdIn`) |
| `presentation/profile/ProfileController.java` | Actualizar (POST follow/unfollow, conteos, listas) |
| `presentation/plan/ForYouController.java` | Actualizar (scope + estados vacios) |
| `templates/pages/profile/view.html` | Actualizar (boton + `#follow`) |
| `templates/pages/profile/edit.html` | Actualizar (`#follow` real, sale el placeholder) |
| `templates/components/navbar-menu.html` | Actualizar (entrada "Siguiendo" navegable) |
| `templates/pages/plans/public-list.html` | Actualizar (selector de scope + empty states) |
| `config/PublicPlanDataSeeder.java` | Actualizar (relaciones demo idempotentes) |
| `infrastructure/follow/FollowServiceImplTest.java` | Crear (U-01..U-08) |
| `presentation/profile/ProfileControllerTest.java` | Actualizar (U-09, U-10) |
| `presentation/plan/ForYouControllerTest.java` | Actualizar (U-11, U-12) |
| `integration/ProfileFollowIntegrationTest.java` | Crear (I-01..I-06) |
| `integration/ForYouFollowingIntegrationTest.java` | Crear (I-07..I-10) |
| `integration/FollowSecurityTest.java` | Crear (S-01..S-03) |
| `e2e/FollowE2E.java` | Crear (E-01) |
| `e2e/ProfileViewE2E.java` | Actualizar (E-02) |
| `CONTEXT.md` | Actualizar (glosario Seguidor / Feed) |
| `docs/specs/11-PROF.md` | Actualizar (el placeholder de `#follow` deja de serlo) |

## Desvios de implementacion

Registrados durante la implementacion, sin cambio de alcance ni de criterios de aceptacion.

1. **E2E se arma sus propios datos.** \FollowE2E\ (E-01) registra al seguidor por UI y sigue a un
   autor con planes publicos creado por el propio test: los usuarios fantasma del seeder solo
   existen en el perfil demo/docker-dev, no bajo el gate.
2. **\ResetDatabase\ borra \ollows\ antes de \users\.** Sin ese DELETE el reset de E2E fallaba por
   la FK de \ollows\ en cuanto una corrida dejaba una relacion.
3. **Sin DTO \PublicProfileView\.** El modelo del perfil lo arma \ProfileController\
   (\publicModel\ / \editModel\ / \putFollowData\) sobre un \Map\, tal como ya estaba en 11-PROF. La
   referencia stale a ese DTO se corrigio en 11-PROF v2.5.
4. **Los conteos y las listas se resuelven en el controller con cuatro consultas agregadas**, no en
   la entidad: \Follow\ no expone colecciones inversas ni \User\ las gana (AC-15).
