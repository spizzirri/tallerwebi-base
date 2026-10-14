# [PLN] Crear Plan

> Trello: https://trello.com/c/vij4lVFO/5-pln-crear-plan
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.

## Objetivo

El usuario puede crear, editar y eliminar planes con nombre, descripcion, fecha y visibilidad (publico/privado). Cada plan tiene un shortCode unico para compartir.

## Invitaciones y participantes en el detalle

Desde `/plans/{id}`, el dueño y los editores pueden incorporar a una persona
registrada mediante su correo y elegir el rol Participante o Editor. La invitación
queda pendiente hasta que el destinatario acepte o rechace en Mis planes. No se envía un email.

En un plan público, cualquier usuario con sesión iniciada puede incorporar
personas registradas con el rol Participante. La asignación del rol Editor queda
reservada al dueño y a los editores. En un plan privado, solo ellos pueden invitar;
el detalle es accesible al dueño y a los participantes.

Los planes públicos se pueden consultar en `/plans/{id}` sin registrarse ni iniciar
sesión. Para visitantes anónimos, el detalle es de solo lectura: no muestra formularios
para invitar, editar, eliminar, cambiar roles o salir del plan. Los intentos de escritura
sin sesión requieren iniciar sesión aunque se envíe un token CSRF válido. Los planes
privados mantienen su restricción de acceso. Consultar información de los lugares
y navegar por el mapa no modifica el plan.

Si el detalle falla al evaluar `isPlanParticipant` con un valor nulo, comprobar que
el controlador y la plantilla correspondan a la misma versión. En desarrollo con
Docker, `docker compose restart app` recompila y carga las clases actualizadas.
La condición del formulario para salir del plan compara explícitamente los valores
booleanos para tolerar la ausencia de ese atributo durante una actualización.

El formulario envía `POST /plans/{id}/participants/invite`, con `participantEmail`
y `role` (`guest` para Participante, valor predeterminado; `editor` para Editor).
La operación requiere una sesión iniciada, valida los permisos en el servicio y
requiere un token CSRF. Si el correo no está registrado, la persona ya tiene una
invitación pendiente para ese plan o ya pertenece
al plan, muestra un mensaje en el detalle sin modificar los participantes ni sus roles.

La lista incluye al dueño una sola vez y muestra primero el nombre completo y luego
el correo de cada persona, junto con su rol. Mantiene el orden por correo sin
distinguir mayúsculas y minúsculas. Solo quienes pertenecen al plan pueden salir;
el dueño no puede hacerlo.

Desde la lista de participantes, el dueño y los editores pueden eliminar participantes
con el botón Eliminar del plan. Solo el dueño puede eliminar editores. El dueño no
puede ser eliminado; un editor utiliza Salir del plan para retirarse por su cuenta.
`POST /plans/{id}/participants/remove` recibe `participantEmail`, exige sesión y token
CSRF, y valida estos permisos en el servicio. La eliminación quita la participación,
el rol Editor y cualquier invitación pendiente de esa persona para el mismo plan.
No elimina su cuenta ni modifica otros planes. En planes privados, la persona eliminada
pierde el acceso al detalle; en planes públicos conserva el acceso de lectura público.

En `/plans`, un panel lateral derecho muestra las invitaciones pendientes del usuario
con sesión iniciada. En pantallas pequeñas aparece debajo de la lista de planes.
Cada invitación muestra el nombre del plan, nombre completo y correo de quien invitó,
el rol ofrecido y las acciones Aceptar y Rechazar. Una invitación a un plan privado
no permite consultar su detalle antes de aceptar.

`POST /plans/{id}/invitations/{invitationId}/respond` recibe `accept=true` o
`accept=false`, exige sesión y token CSRF, y valida que la invitación corresponda
al plan y al usuario que responde. Al aceptar se incorpora al destinatario con el
rol ofrecido; al rechazar no se incorpora. En ambos casos se elimina la invitación
pendiente y se vuelve a Mis planes con un mensaje. Una respuesta repetida no cambia
la participación. Si la persona ya se unió por código, la respuesta no modifica
su rol actual. Después de un rechazo se puede enviar una nueva invitación.

Las invitaciones se guardan en `plan_invitations`, con plan, destinatario, remitente,
rol y versión para controlar respuestas concurrentes. La combinación de plan y
destinatario es única. Eliminar un plan elimina también sus invitaciones pendientes.
Las participaciones existentes se mantienen; no se convierten en invitaciones.

`Plan` agrupa participantes, editores e invitaciones en el componente embebido
`PlanManager`. Las tablas `plan_participants`, `plan_editors` y `plan_invitations`
conservan sus nombres, columnas, restricciones y relaciones; esta refactorización
no necesita migrar datos. Los métodos públicos de `Plan` delegan en el componente.
Las consultas de participación e invitaciones recorren `plan.planManager`.

Los nombres de columnas y roles utilizados repetidamente se centralizan en constantes.
La creación de invitaciones separa la validación de permisos, rol, búsqueda del
destinatario y comprobación de pertenencia o invitación pendiente, conservando el
comportamiento y los mensajes. Las reglas de PMD permanecen activadas sin supresiones.

La cobertura en `PlanServiceImplTest` y `PlanControllerIntegrationTest` verifica
invitaciones del dueño y de editores, invitaciones de usuarios con sesión a planes públicos,
consulta anónima de solo lectura y rechazo de escrituras anónimas,
rechazo de permisos insuficientes, correos desconocidos y duplicados, persistencia
de roles, protección CSRF y visualización del nombre completo y correo.
También verifica persistencia de invitaciones pendientes, aceptación como Editor
o Participante, rechazo, aislamiento por destinatario, intentos de responder por otra
persona, duplicados, respuestas repetidas y eliminación de invitaciones con el plan.

## Pre-requisitos

- [LOG] completed (Spring Security configured)

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | El usuario puede ver la lista de sus planes en /plans |
| AC-02 | El usuario puede crear un plan nuevo con nombre, descripcion, fecha y visibilidad |
| AC-03 | El nombre del plan es obligatorio (minimo 1 caracter) |
| AC-04 | La visibilidad por defecto es PRIVATE |
| AC-05 | Al crear un plan se genera un shortCode unico automaticamente |
| AC-06 | Al crear un plan valido se redirige a /plans/{id} |
| AC-07 | El usuario puede ver el detalle de un plan (nombre, descripcion, fecha, visibilidad) |
| AC-08 | El usuario puede eliminar un plan desde la lista |
| AC-09 | Al eliminar un plan se redirige a /plans |
| AC-10 | GET /plans muestra solo los planes del usuario logueado |
| AC-11 | POST /plans crea un plan nuevo |
| AC-12 | PUT /plans/{id} actualiza un plan existente |
| AC-13 | DELETE /plans/{id} elimina un plan |

## Escenarios de Test

### Tests Unitarios (`presentation/plan/PlanControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | `listPlans()` retorna vista con los planes del usuario | AC-01, AC-10 |
| U-02 | `newPlan()` retorna vista con Plan vacio | AC-02 |
| U-03 | `createPlan()` con datos validos redirige a /plans/{id} | AC-06 |
| U-04 | `createPlan()` con nombre vacio retorna vista con error de validacion | AC-03 |
| U-05 | `planDetail()` con id valido retorna vista con el plan | AC-07 |
| U-06 | `planDetail()` con id invalido retorna vista sin plan | AC-07 |
| U-07 | `deletePlan()` elimina y redirige a /plans | AC-08, AC-09 |

### Tests Unitarios (`infrastructure/plan/PlanServiceImplTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-08 | `createPlan()` siempre genera un shortCode de 8 caracteres, incluso si el plan viene con uno | AC-05 |
| U-09 | `createPlan()` guarda el plan en el repositorio | AC-11 |
| U-10 | `getPlansByUserEmail()` retorna solo planes del usuario | AC-10 |
| U-11 | `getPlanById()` con id existente retorna el plan | AC-07 |
| U-12 | `getPlanById()` con id inexistente retorna empty | AC-07 |
| U-13 | `updatePlan()` actualiza el plan | AC-12 |
| U-14 | `deletePlan()` elimina el plan | AC-13 |

### Tests de Integracion (`integration/PlanControllerIntegrationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | `GET /plans` retorna 200 y lista de planes | AC-01 |
| I-02 | `GET /plans/new` retorna 200 y formulario | AC-02 |
| I-03 | `POST /plans` con datos validos redirige a /plans/{id} | AC-06 |
| I-04 | `POST /plans` con nombre vacio retorna 200 con error de validacion | AC-03 |
| I-05 | `GET /plans/{id}` con id valido retorna 200 | AC-07 |
| I-06 | `GET /plans/{id}` con id inexistente retorna 200 sin plan | AC-07 |
| I-07 | `DELETE /plans/{id}` redirige a /plans | AC-08, AC-09 |

### Tests de Integracion (`infrastructure/PlanRepositoryTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-08 | `save()` persiste el plan con shortCode generado | AC-05 |
| I-09 | `findByUserId()` retorna solo planes del usuario | AC-10 |
| I-10 | `findByShortCode()` retorna el plan correcto | AC-05 |
| I-11 | `deleteById()` elimina el plan | AC-13 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `PlansViewE2E` | Crear plan, ver en lista, ver detalle, eliminar | AC-02, AC-06, AC-07, AC-08 |

## Referencia de Implementacion

> Los pasos a continuación son guía de implementación, no reemplazan los acceptance criteria de arriba.

### 1. Create Plan entity

File: `src/main/java/com/valhalla/domain/plan/Plan.java`

```java
package com.valhalla.domain.plan;

import com.valhalla.domain.plan.PlanPlace;
import com.valhalla.domain.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Plan name is required")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "visit_date")
    private LocalDate visitDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility = Visibility.PRIVATE;

    @Column(name = "short_code", unique = true)
    private String shortCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanPlace> planPlaces = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Visibility {
        PUBLIC, PRIVATE
    }

    // Getters and setters
}
```

### 2. Create PlanRepository

File: `src/main/java/com/valhalla/domain/plan/PlanRepository.java`

```java
package com.valhalla.domain.plan;

import java.util.List;
import java.util.Optional;

public interface PlanRepository {
    Plan save(Plan plan);
    Optional<Plan> findById(Long id);
    Optional<Plan> findByShortCode(String shortCode);
    List<Plan> findByUserId(Long userId);
    void deleteById(Long id);
}
```

### 3. Create PlanService

File: `src/main/java/com/valhalla/domain/plan/PlanService.java`

```java
package com.valhalla.domain.plan;

import java.util.List;
import java.util.Optional;

public interface PlanService {
    Plan createPlan(Plan plan);
    Optional<Plan> getPlanById(Long id);
    Optional<Plan> getPlanByShortCode(String shortCode);
    List<Plan> getPlansByUserEmail(String email);
    Plan updatePlan(Plan plan);
    void deletePlan(Long id);
}
```

`generateShortCode()` **no** existe con ese nombre ni es parte de la interface: el método real es
`private String generateUniqueShortCode()` en `PlanServiceImpl`, con retry y `toUpperCase(Locale.ROOT)`.
La interface expone únicamente `createPlan(Plan, String ownerEmail)`.

File: `src/main/java/com/valhalla/infrastructure/plan/PlanServiceImpl.java`

```java
package com.valhalla.domain.plan;

import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    @Autowired
    public PlanServiceImpl(PlanRepository planRepository, UserRepository userRepository) {
        this.planRepository = planRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Plan createPlan(Plan plan, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
            .orElseThrow(UserNotFoundException::new);
        plan.setAdministrator(owner);
        // Always generated: the share code is the backend's, the form never asks for it.
        plan.setShortCode(generateUniqueShortCode());
        return planRepository.save(plan);
    }

    @Override
    public Optional<Plan> getPlanById(Long id) {
        return planRepository.findById(id);
    }

    @Override
    public Optional<Plan> getPlanByShortCode(String shortCode) {
        return planRepository.findByShortCode(shortCode);
    }

    @Override
    public List<Plan> getPlansByUserEmail(String email) {
        return userRepository.findByEmail(email)
            .map(user -> planRepository.findByUserId(user.getId()))
            .orElse(List.of());
    }

    @Override
    public Plan updatePlan(Plan plan) {
        return planRepository.save(plan);
    }

    @Override
    public void deletePlan(Long id) {
        planRepository.deleteById(id);
    }

    /** La unique constraint de shortCode es la guarda real; el loop solo acorta la ventana. */
    private String generateUniqueShortCode() {
      for (int attempt = 0; attempt < SHORT_CODE_MAX_ATTEMPTS; attempt++) {
        String candidate = UUID.randomUUID().toString()
          .substring(0, SHORT_CODE_LENGTH)
          .toUpperCase(Locale.ROOT);
        if (!planRepository.existsByShortCode(candidate)) {
          return candidate;
        }
      }
      throw new IllegalStateException("Could not generate a unique plan short code");
    }
}
```

### 4. Create JPA Repository

File: `src/main/java/com/valhalla/infrastructure/plan/JpaPlanRepository.java`

```java
package com.valhalla.infrastructure.plan;

import com.valhalla.domain.plan.Plan;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByUserId(Long userId);
    Plan findByShortCode(String shortCode);
}
```

### 5. Create PlanRepositoryImpl

File: `src/main/java/com/valhalla/infrastructure/plan/PlanRepositoryImpl.java`

```java
package com.valhalla.infrastructure.plan;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PlanRepositoryImpl implements PlanRepository {

    private final JpaPlanRepository jpaPlanRepository;

    @Autowired
    public PlanRepositoryImpl(JpaPlanRepository jpaPlanRepository) {
        this.jpaPlanRepository = jpaPlanRepository;
    }

    @Override
    public Plan save(Plan plan) {
        return jpaPlanRepository.save(plan);
    }

    @Override
    public Optional<Plan> findById(Long id) {
        return jpaPlanRepository.findById(id);
    }

    @Override
    public Optional<Plan> findByShortCode(String shortCode) {
        return Optional.ofNullable(jpaPlanRepository.findByShortCode(shortCode));
    }

    @Override
    public List<Plan> findByUserId(Long userId) {
        return jpaPlanRepository.findByUserId(userId);
    }

    @Override
    public void deleteById(Long id) {
        jpaPlanRepository.deleteById(id);
    }
}
```

### 6. Create PlanController

File: `src/main/java/com/valhalla/presentation/plan/PlanController.java`

```java
package com.valhalla.presentation.plan;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/plans")
public class PlanController {

    private final PlanService planService;
    private final UserRepository userRepository;

    @Autowired
    public PlanController(PlanService planService, UserRepository userRepository) {
        this.planService = planService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ModelAndView listPlans(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> model = new ModelMap();
        model.put("plans", planService.getPlansByUserEmail(userDetails.getUsername()));
        return new ModelAndView("pages/plans/list", model);
    }

    @GetMapping("/new")
    public ModelAndView newPlan() {
        Map<String, Object> model = new ModelMap();
        model.put("plan", new Plan());
        return new ModelAndView("pages/plans/new", model);
    }

    @PostMapping
    public ModelAndView createPlan(
        @Valid @ModelAttribute Plan plan,
        BindingResult bindingResult,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("pages/plans/new");
        }
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        plan.setUser(user);
        Plan created = planService.createPlan(plan);
        return new ModelAndView("redirect:/plans/" + created.getId());
    }

    @GetMapping("/{id}")
    public ModelAndView planDetail(@PathVariable Long id) {
        Map<String, Object> model = new ModelMap();
        planService.getPlanById(id).ifPresent(plan -> model.put("plan", plan));
        return new ModelAndView("pages/plans/detail", model);
    }

    @PutMapping("/{id}")
    public ModelAndView updatePlan(
        @PathVariable Long id,
        @ModelAttribute Plan plan
    ) {
        plan.setId(id);
        planService.updatePlan(plan);
        return new ModelAndView("redirect:/plans/" + id);
    }

    @DeleteMapping("/{id}")
    public ModelAndView deletePlan(@PathVariable Long id) {
        planService.deletePlan(id);
        return new ModelAndView("redirect:/plans");
    }
}
```

### 7. Create templates

#### 7.1 Plan list

File: `src/main/webapp/WEB-INF/templates/pages/plans/list.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>My Plans</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 min-h-screen">
    <div th:replace="~{fragments/header :: header}"></div>

    <main class="container mx-auto px-4 py-8">
        <div class="flex justify-between items-center mb-8">
            <h1 class="text-3xl font-bold">My Plans</h1>
            <a th:href="@{/plans/new}" class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded">
                Create Plan
            </a>
        </div>

        <div th:if="${plans}" class="grid gap-4">
            <div th:each="plan : ${plans}" class="bg-white p-6 rounded-lg shadow">
                <div class="flex justify-between items-start">
                    <div>
                        <h3 class="text-xl font-bold" th:text="${plan.name}"></h3>
                        <p class="text-gray-600 mt-1" th:text="${plan.description}"></p>
                        <span class="inline-block mt-2 px-2 py-0.5 text-xs text-white rounded"
                              th:classappend="${plan.visibility.name() == 'PUBLIC'} ? 'bg-green-500' : 'bg-gray-500'"
                              th:text="${plan.visibility}"></span>
                    </div>
                    <div class="flex gap-2">
                        <a th:href="@{/plans/{id}(id=${plan.id})}"
                           class="bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded text-sm">
                            View
                        </a>
                        <form th:action="@{/plans/{id}(id=${plan.id})}" method="post" class="inline">
                            <input type="hidden" name="_method" value="delete">
                            <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">
                            <button type="submit" class="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded text-sm">
                                Delete
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <div th:unless="${plans}" class="text-center py-16 text-gray-500">
            <p class="text-lg mb-4">You don't have any plans yet.</p>
            <a th:href="@{/plans/new}" class="bg-blue-500 hover:bg-blue-600 text-white px-6 py-3 rounded inline-block">
                Create your first plan
            </a>
        </div>
    </main>
</body>
</html>
```

#### 7.2 New plan form

File: `src/main/webapp/WEB-INF/templates/pages/plans/new.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Create Plan</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 min-h-screen">
    <div th:replace="~{fragments/header :: header}"></div>

    <main class="container mx-auto px-4 py-8 max-w-2xl">
        <h1 class="text-3xl font-bold mb-8">Create New Plan</h1>

        <form th:action="@{/plans}" th:object="${plan}" method="post" class="bg-white p-6 rounded-lg shadow">
            <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">

            <div class="mb-4">
                <label for="name" class="block text-gray-700 font-medium mb-2">Plan Name</label>
                <input type="text" id="name" th:field="*{name}"
                       class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
                       required placeholder="e.g., Palermo Tour">
                <p th:if="${#fields.hasErrors('name')}" th:errors="*{name}" class="text-red-500 text-sm mt-1"></p>
            </div>

            <div class="mb-4">
                <label for="description" class="block text-gray-700 font-medium mb-2">Description</label>
                <textarea id="description" th:field="*{description}" rows="3"
                          class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
                          placeholder="Describe your plan..."></textarea>
            </div>

            <div class="mb-4">
                <label for="visitDate" class="block text-gray-700 font-medium mb-2">Visit Date</label>
                <input type="date" id="visitDate" th:field="*{visitDate}"
                       class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500">
            </div>

            <div class="mb-6">
                <label for="visibility" class="block text-gray-700 font-medium mb-2">Visibility</label>
                <select id="visibility" th:field="*{visibility}"
                        class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500">
                    <option value="PRIVATE">Private (only me)</option>
                    <option value="PUBLIC">Public (shareable)</option>
                </select>
            </div>

            <div class="flex gap-4 justify-end">
                <a th:href="@{/plans}" class="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded">
                    Cancel
                </a>
                <button type="submit" class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded">
                    Create Plan
                </button>
            </div>
        </form>
    </main>
</body>
</html>
```

#### 7.3 Plan detail

File: `src/main/webapp/WEB-INF/templates/pages/plans/detail.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="${plan?.name}">Plan Detail</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 min-h-screen">
    <div th:replace="~{fragments/header :: header}"></div>

    <main class="container mx-auto px-4 py-8" th:if="${plan}">
        <div class="flex justify-between items-start mb-8">
            <div>
                <h1 class="text-3xl font-bold" th:text="${plan.name}"></h1>
                <span class="inline-block mt-2 px-2 py-0.5 text-xs text-white rounded"
                      th:classappend="${plan.visibility.name() == 'PUBLIC'} ? 'bg-green-500' : 'bg-gray-500'"
                      th:text="${plan.visibility}"></span>
            </div>
            <div class="flex gap-2">
                <a th:href="@{/places}" class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded">
                    Add Places
                </a>
                <button class="bg-purple-500 hover:bg-purple-600 text-white px-4 py-2 rounded">
                    Share
                </button>
            </div>
        </div>

        <p class="text-gray-600 mb-4" th:text="${plan.description}"></p>
        <p class="text-gray-700 mb-8" th:if="${plan.visitDate}">
            <strong>Date:</strong> <span th:text="${plan.visitDate}"></span>
        </p>

        <div class="bg-white p-6 rounded-lg shadow">
            <h2 class="text-xl font-bold mb-4">Itinerary</h2>
            <p class="text-gray-500">No places added yet.</p>
        </div>
    </main>
</body>
</html>
```

## Archivos a crear

| File | Action |
|------|--------|
| `domain/plan/Plan.java` | Create (with @NotBlank validation) |
| `domain/plan/PlanRepository.java` | Create |
| `domain/plan/PlanService.java` | Create |
| `infrastructure/plan/PlanServiceImpl.java` | Create |
| `infrastructure/plan/JpaPlanRepository.java` | Create |
| `infrastructure/plan/PlanRepositoryImpl.java` | Create |
| `presentation/plan/PlanController.java` | Create (with @Valid + BindingResult) |
| `templates/pages/plans/list.html` | Create (with CSRF token in delete form) |
| `templates/pages/plans/new.html` | Create (with CSRF token + validation errors) |
| `templates/pages/plans/detail.html` | Create |
