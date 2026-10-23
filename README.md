# Valhalla, Taller Web I

Spring MVC + Thymeleaf + Tailwind project for Taller Web I (UNLaM).

## Quick Start

```shell
git clone <repo-url>
cd valhalla
cp .env.example .env
docker compose up
```

The app runs at [http://localhost:8080](http://localhost:8080).

**Default credentials:** `test@unlam.edu.ar` / `password`

This starts PostgreSQL + the app in Docker with hot-reload. Source code is mounted as a volume, changes reflect immediately.

## Project Structure

```
src/main/java/com/valhalla/
├── config/                 # Spring configuration (JPA, MVC, security, validation, dev hot-reload)
├── domain/                 # Business logic (entities, services, repository interfaces, exceptions)
│   ├── exception/          # PlanNotFoundException, UserNotFoundException, UserAlreadyExists
│   ├── login/              # LoginService
│   ├── user/               # User, UserService, UserRepository interface
│   ├── place/              # Place, PlaceCategory, PlaceService, PlaceRepository interface
│   └── plan/               # Plan, PlanService, PlanRepository interface
├── infrastructure/         # Persistence (Spring Data JPA repositories) + seeders
│   ├── user/               # JpaUserRepository, UserRepositoryImpl
│   ├── place/              # JpaPlaceRepository, PlaceRepositoryImpl
│   ├── plan/               # JpaPlanRepository, PlanRepositoryImpl
│   ├── login/              # Spring Security adapters for the login flow
│   ├── security/           # CustomUserDetailsService, CustomAuthenticationSuccessHandler
│   ├── UserSeeder.java     # Seeds test admin on startup
│   └── PlaceDataSeeder.java # Seeds 10 Buenos Aires places
├── presentation/           # MVC controllers, request DTOs
│   ├── login/              # LoginController (/admin/login)
│   ├── auth/               # AuthController (/auth/*: login, register, password recovery)
│   ├── landing/            # LandingController (/)
│   ├── user/               # UserController + EditUserRequest
│   ├── place/              # PlaceController, PlaceRestController
│   ├── plan/               # PlanController + PlanRequest
│   └── shared/             # GlobalExceptionHandler + NewUser/Register/RecoverPassword requests
└── MyServletInitializer.java  # Bootstrap for external servlet containers

src/main/webapp/
├── WEB-INF/templates/
│   ├── layouts/base.html   # page chrome: head + layout decorator
│   ├── components/
│   │   ├── navbar.html     # top nav, renders the authenticated email
│   │   └── alerts/error-alert.html
│   └── pages/
│       ├── landing.html    # public landing page (/)
│       ├── home.html, error.html
│       ├── admin/          # users list + user form
│       ├── auth/           # /auth/login, /auth/new-user
│       │   └── user/       # login, register, forgot-password, recovered
│       ├── places/list.html # places map + sidebar (details render in the sidebar)
│       └── plans/          # list, new, detail (itinerary + map)
└── resources/
    ├── core/js/            # vendored, no build step: tailwind-browser.js, vue.global.prod.js,
    │                       # lucide.min.js, leaflet.js
    ├── css/                # vendored stylesheets: bootstrap.min.css, leaflet.css
    │                       # (+ css/images/ for leaflet's relative url() targets)
    └── images/             # place-placeholder.svg
```

El frontend no tiene paso de build: Tailwind corre en el browser (`tailwind-browser.js`, vendored),
Vue igual (`vue.global.prod.js`) y Leaflet tambien (`leaflet.js` + `leaflet.css`, version fija en
el repo), y su logica de mapa vive en modulos de `resources/core/js/pages/`, no inline en los
templates Thymeleaf.

## Documentation

| Document | Description |
| :--- | :--- |
| [Architecture](docs/architecture.md) | Layered architecture, domain model, dependency rules |
| [Adding a Feature](docs/adding-a-feature.md) | Step-by-step: domain, repository, service, controller, template |
| [Frontend Stack](docs/frontend-stack.md) | Why each tool exists and team conventions |
| [Vue + Tailwind Guide](docs/guide-vue.md) | How to create views with Vue |
| [CSS + JS Guide](docs/guide-css-js.md) | How to create views with plain CSS + JS |
| [Error Handling](docs/error-handling.md) | Validation, custom exceptions, GlobalExceptionHandler |
| [Testing](docs/testing.md) | Unit tests, integration tests, E2E tests |
| [Environment Setup](docs/setup.md) | Install Java, Maven, Docker, IDE config |
| [Commands Reference](docs/commands.md) | Maven, Docker, and testing commands |
| [Code Quality](docs/code-quality.md) | Checkstyle, PMD, CPD, JaCoCo, Prettier |
| [Spec Format](docs/spec-format.md) | Spec structure: criterios, escenarios de test, referencia |
| [Sprint Planner](docs/sprint-planner.md) | Sprint plan, dependencies, team assignments |

## Specs

Las specs definen cada feature del proyecto. Cada una tiene Criterios de Aceptacion, Escenarios de Test y Referencia de Implementacion.

| Spec | Feature | Trello |
| :--- | :--- | :--- |
| [01-LOG](docs/specs/01-LOG.md) | Login, Register, Security | [LOG](https://trello.com/c/AuMX9RgJ) |
| [02-PLC](docs/specs/02-PLC.md) | Explorar Lugares + Ficha | [PLC](https://trello.com/c/mPp6mZml) |
| [03-PLN](docs/specs/03-PLN.md) | Crear Plan | [PLN](https://trello.com/c/vij4lVFO) |
| [04-APL-BE](docs/specs/04-APL-BE.md) | Agregar Lugares al Plan (Backend) | [APL-BE](https://trello.com/c/zHj13Gy6) |
| [05-APL-FE](docs/specs/05-APL-FE.md) | Agregar Lugares al Plan (Frontend) | [APL-FE](https://trello.com/c/K7XqvGWZ) |
| [06-CMP](docs/specs/06-CMP.md) | Compartir Plan | [CMP](https://trello.com/c/vZuil82c) |
| [07-VPC](docs/specs/07-VPC.md) | Vista Publica de Plan Compartido | [VPC](https://trello.com/c/hr1n4EwA) |
| [08-AUT](docs/specs/08-AUT.md) | Auth de Usuarios (login, registro, recovery) | [AUT](https://trello.com/c/AhEeTyf7) |
| [09-BRD](docs/specs/09-BRD.md) | Branding e identidad visual | — (card pendiente) |
| [10-LAND](docs/specs/10-LAND.md) | Landing Page | — (card pendiente) |
| [11-PROF](docs/specs/11-PROF.md) | Perfil de usuario y cambio de contraseña | — (card pendiente) |
| [12-DATA](docs/specs/12-DATA.md) | ABM de lugares + importador desde OpenStreetMap | [DATA](https://trello.com/c/ExZAxLQ5) |

LO, PLC, PLN y AUT (01, 02, 03, 08) ya estan mergeados; APL-BE, APL-FE, CMP y VPC (04 a 07) son
backlog. Las 09 a 12 son las nuevas de user-facing. El costo en puntos y el sprint de cada card
estan en el [Sprint Planner](docs/sprint-planner.md).

## Authentication

Spring Security handles auth. `SecurityConfig` configures form login (`/admin/login`), logout (`/admin/logout`), CSRF (exempt for `/api/**`), and session management (1 session per user). `CustomUserDetailsService` bridges `UserRepository` to Spring Security. `CustomAuthenticationSuccessHandler` sets `loginTime` in the HTTP session after successful login. Public routes: `/`, `/share/**`. Protected: `/admin/**` requires `ROLE_ADMIN`. New registrations get `role = USER` and `active = true`.

User-facing auth (self-registration, `/auth/login`, password recovery) is specified in [08-AUT](docs/specs/08-AUT.md) and is already implemented — merged in PR #5.

## Technologies

- Docker
- Java 25 (LTS)
- Spring 6.2.19 + Spring Data JPA 3.5.13 + Spring Security 6.5.11
- Hibernate Validator 8.0.5.Final
- Thymeleaf 3.1.5.RELEASE
- Embedded Jetty Server EE10 12.0.37
- Tailwind CSS 4.3.3 (compiled in the browser)
- Vue.js 3.5.13 (client-side interactivity)
- Leaflet 1.9.4 + OpenStreetMap (interactive maps)
- Spring Test 6.2.19 / Hamcrest 2.2 / JUnit 6.1.2
- Mockito 5.23.0 / Playwright 1.61.0
- PMD 7.26.0 / Checkstyle 13.8.0 / Prettier 0.22 / JaCoCo 0.8.15
