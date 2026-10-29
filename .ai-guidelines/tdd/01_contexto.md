# Contexto: capas y tipos de test

## 1. Capas
| Capa | Paquete | Contiene |
| :--- | :--- | :--- |
| Dominio | `com.tallerwebi.dominio` | Entidades, interfaces de servicio y repositorio, implementaciones de servicio, `excepcion/` |
| Infraestructura | `com.tallerwebi.infraestructura` | Implementaciones de repositorio con Hibernate (`SessionFactory`) |
| Presentación | `com.tallerwebi.presentacion` | Controladores `ControladorX` que devuelven `ModelAndView` |
| Vista | `src/main/webapp/WEB-INF/views/thymeleaf` | Templates Thymeleaf |

Las tablas las crea Hibernate a partir de las entidades (`hbm2ddl.auto=create`); no se escribe DDL a mano.

## 2. Tipos de test y ubicación
| Tipo | Ubicación | Qué valida | Cómo |
| :--- | :--- | :--- | :--- |
| Unitario de dominio | `src/test/java/com/tallerwebi/dominio/` | Reglas de negocio del servicio | `new` + mocks de Mockito, sin Spring |
| Unitario de presentación | `src/test/java/com/tallerwebi/presentacion/` | Vista y modelo del `ModelAndView` | `new` + mock del servicio, sin Spring |
| Infraestructura | `src/test/java/com/tallerwebi/infraestructura/` | Persistencia real en HSQLDB en memoria | `SpringExtension` + `HibernateInfraestructuraTestConfig`, `@Transactional @Rollback` |
| Integración web | `src/test/java/com/tallerwebi/integracion/` | Contrato HTTP y HTML renderizado | `SpringExtension` + `@WebAppConfiguration` + `SpringWebTestConfig` + `HibernateTestConfig` + `MockMvc` |
| E2E | `src/test/java/com/tallerwebi/punta_a_punta/` | Recorrido completo en el navegador | Playwright + page objects en `vistas/`, app levantada con MySQL |
