# IMPORTANTE: Queda de ejemplo, deben generar uno nuevo para cada funcionalidad.

# Plan de Implementación: Funcionalidad de Agregar Comentarios

**Proyecto:** TallerWebi  
**Feature:** `feature/agregar-comentarios`  
**Fecha:** 2026-06-18  
**Arquitecto:** Senior Developer (TDD-First)  
**Ciclo:** Red → Green → Refactor → Validate

---

## 📋 Tabla de Contenidos

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Especificación Funcional](#especificación-funcional)
3. [Orden de Desarrollo (TDD)](#orden-de-desarrollo-tdd)
4. [Checklist de Implementación](#checklist-de-implementación)
5. [Validación Final](#validación-final)

---

## 🎯 Resumen Ejecutivo

### Requisito Funcional
Implementar funcionalidad para **guardar comentarios** con máximo **2000 caracteres** en una aplicación Java Spring MVC.

### Criterios de Aceptación
- ✅ Usuario puede guardar comentarios válidos (1-2000 caracteres)
- ✅ Usuario recibe feedback clara si intenta guardar comentarios vacíos
- ✅ Usuario recibe feedback si intenta guardar comentarios > 2000 caracteres
- ✅ Cobertura JaCoCo ≥ 80% en `BUNDLE/LINE`
- ✅ Código cumple Checkstyle (máx 100 caracteres/línea)
- ✅ Código cumple PMD
- ✅ Mensaje de éxito en UI (Thymeleaf)

### Stack Técnico
- **Backend:** Java 8+, Spring MVC, Hibernate
- **Testing:** JUnit 4, Mockito
- **Validación:** Checkstyle, PMD, JaCoCo
- **Frontend:** Thymeleaf, HTML5
- **BD:** SQL (DDL incluido en `data.sql`)

---

## 📐 Especificación Funcional

### Entidad: Comentario

```java
@Entity
@Table(name = "comentarios")
public class Comentario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(length = 2000, nullable = false)
  private String contenido;
}
```

### Script DDL (agregar a `data.sql`)
```sql
CREATE TABLE comentarios (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  contenido VARCHAR(2000) NOT NULL
);
```

### Validaciones Empresariales
- Contenido NO nulo
- Contenido NO vacío (trim)
- Contenido ≤ 2000 caracteres

---

## 🔄 Orden de Desarrollo (TDD)

El siguiente orden **es obligatorio** y debe ejecutarse ciclo por ciclo:

```
FASE 1: ENTIDAD
├─ Crear Comentario.java
└─ Verificar compilación

FASE 2: REPOSITORIO
├─ Test RepositorioComentarioTest.java (Red)
├─ Impl RepositorioComentarioImpl.java (Green)
└─ Refactor (Checkstyle/PMD)

FASE 3: SERVICIO
├─ Test ServicioComentarioTest.java (Red)
├─ Impl ServicioComentarioImpl.java (Green)
└─ Refactor (Checkstyle/PMD)

FASE 4: CONTROLADOR
├─ Test ControladorComentarioTest.java (Red)
├─ Impl ControladorComentario.java (Green)
└─ Refactor (Checkstyle/PMD)

FASE 5: VISTA
├─ vista-comentario.html (Thymeleaf)
├─ Validación mensaje éxito
└─ Refactor (Prettier)

FASE 6: VALIDACIÓN INTEGRAL
└─ mvn validate + mvn test + JaCoCo report
```

---

## ✅ Checklist de Implementación

### FASE 1: ENTIDAD `Comentario`

**Archivo:** `src/main/java/com/tallerwebi/dominio/Comentario.java`

- [ ] Crear clase `Comentario` con anotaciones JPA
- [ ] Atributos: `id`, `contenido` (VARCHAR 2000)
- [ ] Getters/Setters para todos los atributos
- [ ] Constructor sin parámetros (JPA)
- [ ] Constructor con parámetros (contenido)
- [ ] Validar que compile sin errores
- [ ] Ejecutar: `mvn compile`

**Checklist específico:**
```
[ ] Anotación @Entity en clase
[ ] Anotación @Table(name = "comentarios")
[ ] @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
[ ] @Column(length = 2000, nullable = false) en contenido
[ ] Getters con Javadoc
[ ] Setters con Javadoc
[ ] Constructor() sin parámetros
[ ] Constructor(String) con parámetro contenido
[ ] Cumple Checkstyle (línea máx 100 caracteres)
[ ] Cumple PMD (sin variables no usadas)
```

---

### FASE 2: REPOSITORIO

#### PASO 2.1: Interface RepositorioComentario

**Archivo:** `src/main/java/com/tallerwebi/dominio/RepositorioComentario.java`

**Interfaz (sin tests, solo contrato):**
```java
public interface RepositorioComentario {
  void guardar(Comentario comentario);
}
```

- [ ] Crear interface con solo el método guardar()

#### PASO 2.2: Test - RepositorioComentarioTest

**Archivo:** `src/test/java/com/tallerwebi/infraestructura/RepositorioComentarioTest.java`

**RED: Crear test unitario**

| # | Caso de Prueba | Descripción | Patrón |
|---|---|---|---|
| 1 | `deberiaGuardarUnComentarioValido` | Guardar comentario de 500 caracteres | CRUD |
| 2 | `deberiaGuardarUnComentarioMaximo` | Guardar comentario de 2000 caracteres (máximo) | Borde |

**Checklist:**
```
[ ] Test 1: deberiaGuardarUnComentarioValido
    [ ] // dado: crear Comentario con 500 caracteres
    [ ] // cuando: repositorio.guardar(comentario)
    [ ] // entonces: verify guardó (usar mock de BD)
    
[ ] Test 2: deberiaGuardarUnComentarioMaximo
    [ ] // dado: crear Comentario con 2000 caracteres
    [ ] // cuando: repositorio.guardar(comentario)
    [ ] // entonces: verify guardó (usar mock de BD)
    
[ ] Ejecutar: mvn test -Dtest=RepositorioComentarioTest (FALLA esperada - RED)
```

#### PASO 2.3: Implementación - RepositorioComentarioImpl

**Archivo:** `src/main/java/com/tallerwebi/infraestructura/RepositorioComentarioImpl.java`

**GREEN: Código mínimo para pasar tests**

- [ ] Inyectar `SessionFactory` (Hibernate)
- [ ] Implementar `guardar()`: `session.save(comentario)`
- [ ] Anotación: `@Repository`
- [ ] Ejecutar: `mvn test -Dtest=RepositorioComentarioTest` (PASA)

#### PASO 2.4: Refactor - RepositorioComentarioImpl

**REFACTOR: Checkstyle/PMD**

- [ ] Líneas < 100 caracteres
- [ ] Sin variables no usadas
- [ ] Javadoc en métodos públicos
- [ ] Sin duplicación de código
- [ ] Ejecutar: `mvn clean verify` (sin errores)

---

### FASE 3: SERVICIO

#### PASO 3.1: Interface ServicioComentario

**Archivo:** `src/main/java/com/tallerwebi/dominio/ServicioComentario.java`

```java
public interface ServicioComentario {
  void guardar(Comentario comentario) throws ComentarioInvalido;
}
```

- [ ] Crear interface

#### PASO 3.2: Exception - ComentarioInvalido

**Archivo:** `src/main/java/com/tallerwebi/dominio/ComentarioInvalido.java`

```java
public class ComentarioInvalido extends Exception {
  public ComentarioInvalido(String mensaje) {
    super(mensaje);
  }
}
```

- [ ] Crear exception personalizada

#### PASO 3.3: Test - ServicioComentarioTest

**Archivo:** `src/test/java/com/tallerwebi/dominio/ServicioComentarioTest.java`

**RED: Crear test unitario con Mockito**

| # | Caso de Prueba | Descripción | Patrón |
|---|---|---|---|
| 1 | `deberiaGuardarUnComentarioConValoresValidos` | 500 caracteres | Éxito |
| 2 | `deberiaGuardarUnComentarioConMaximoPermitido` | 2000 caracteres | Éxito |
| 3 | `noDeberiaGuardarUnComentarioVacio` | contenido = "" | Error |
| 4 | `noDeberiaGuardarUnComentarioNulo` | contenido = null | Error |
| 5 | `noDeberiaGuardarUnComentarioConSoloEspacios` | contenido = "   " | Error |
| 6 | `noDeberiaGuardarUnComentarioQueExcedaDosMil` | 5000 caracteres | Error |

**Checklist:**
```
[ ] @RunWith(MockitoJUnitRunner.class)
[ ] @Mock private RepositorioComentario repositorioMock
[ ] @InjectMocks private ServicioComentarioImpl servicio

[ ] Test 1: deberiaGuardarUnComentarioConValoresValidos
    [ ] // dado: Comentario con 500 caracteres válidos
    [ ] // cuando: servicio.guardar(comentario)
    [ ] // entonces: verify(repositorioMock, times(1)).guardar(comentario)

[ ] Test 2: deberiaGuardarUnComentarioConMaximoPermitido
    [ ] // dado: Comentario con "x" repetido 2000 veces
    [ ] // cuando: servicio.guardar(comentario)
    [ ] // entonces: verify(repositorioMock, times(1)).guardar(comentario)

[ ] Test 3: noDeberiaGuardarUnComentarioVacio
    [ ] // dado: Comentario comentario con contenido = ""
    [ ] // cuando/entonces: assertThrows(ComentarioInvalido.class, 
           () -> servicio.guardar(comentario))

[ ] Test 4: noDeberiaGuardarUnComentarioNulo
    [ ] // dado: Comentario comentario con contenido = null
    [ ] // cuando/entonces: assertThrows(ComentarioInvalido.class, 
           () -> servicio.guardar(comentario))

[ ] Test 5: noDeberiaGuardarUnComentarioConSoloEspacios
    [ ] // dado: Comentario comentario con contenido = "   "
    [ ] // cuando/entonces: assertThrows(ComentarioInvalido.class, 
           () -> servicio.guardar(comentario))

[ ] Test 6: noDeberiaGuardarUnComentarioQueExcedaDosMil
    [ ] // dado: Comentario comentario con "x".repeat(5000)
    [ ] // cuando/entonces: assertThrows(ComentarioInvalido.class, 
           () -> servicio.guardar(comentario))

[ ] Ejecutar: mvn test -Dtest=ServicioComentarioTest (FALLA - RED)
```

#### PASO 3.4: Implementación - ServicioComentarioImpl

**Archivo:** `src/main/java/com/tallerwebi/dominio/ServicioComentarioImpl.java`

**GREEN: Código mínimo**

```java
@Service
@Transactional
public class ServicioComentarioImpl implements ServicioComentario {
  
  @Autowired
  private RepositorioComentario repositorio;
  
  /**
   * Guarda un comentario validando que no exceda 2000 caracteres.
   *
   * @param comentario el comentario a guardar
   * @throws ComentarioInvalido si el contenido es vacío o excede 2000 caracteres
   */
  @Override
  public void guardar(Comentario comentario) 
      throws ComentarioInvalido {
    if (comentario == null 
        || comentario.getContenido() == null 
        || comentario.getContenido().trim().isEmpty()) {
      throw new ComentarioInvalido(
        "El comentario no puede estar vacío"
      );
    }
    
    String contenido = comentario.getContenido().trim();
    if (contenido.length() > 2000) {
      throw new ComentarioInvalido(
        "El comentario no puede exceder 2000 caracteres"
      );
    }
    
    comentario.setContenido(contenido);
    repositorio.guardar(comentario);
  }
}
```

- [ ] Implementar validaciones
- [ ] Inyectar `RepositorioComentario`
- [ ] Ejecutar: `mvn test -Dtest=ServicioComentarioTest` (PASA)

#### PASO 3.5: Refactor - ServicioComentarioImpl

**REFACTOR: Checkstyle/PMD**

- [ ] Líneas < 100 caracteres
- [ ] Javadoc en todos los métodos
- [ ] Sin complejidad ciclomática > 10
- [ ] Ejecutar: `mvn clean verify`

---

### FASE 4: CONTROLADOR

#### PASO 4.1: Test - ControladorComentarioTest

**Archivo:** `src/test/java/com/tallerwebi/presentacion/ControladorComentarioTest.java`

**RED: Test con Spring Test + Mockito**

| # | Caso de Prueba | Descripción | HTTP |
|---|---|---|---|
| 1 | `deberiaGuardarComentarioYRedirigir` | POST válido | 302 |
| 2 | `deberiaValidarComentarioVacio` | POST vacío | 200 (form) |
| 3 | `deberiaValidarComentarioExcedido` | POST > 2000 | 200 (form) |
| 4 | `deberiaAgregarMensajeExitoAlRedirigir` | POST válido → mensaje | attr |

**Checklist:**
```
[ ] @RunWith(SpringRunner.class)
[ ] @WebMvcTest(ControladorComentario.class)
[ ] @MockBean private ServicioComentario servicio
[ ] @Autowired private MockMvc mockMvc

[ ] Test 1: deberiaGuardarComentarioYRedirigir
    [ ] // dado: comentario con 500 caracteres válidos
    [ ] // cuando: POST /comentarios
    [ ] // entonces: status 302, redirige a lista

[ ] Test 2: deberiaValidarComentarioVacio
    [ ] // dado: comentario con contenido = ""
    [ ] // cuando: POST /comentarios
    [ ] // entonces: status 200, model error

[ ] Test 3: deberiaValidarComentarioExcedido
    [ ] // dado: comentario con 5000 caracteres
    [ ] // cuando: POST /comentarios
    [ ] // entonces: status 200, model error

[ ] Test 4: deberiaAgregarMensajeExitoAlRedirigir
    [ ] // dado: comentario válido, doNothing.when(servicio).guardar()
    [ ] // cuando: POST /comentarios
    [ ] // entonces: RedirectAttributes.addFlashAttribute("mensaje", "...")

[ ] Ejecutar: mvn test -Dtest=ControladorComentarioTest (FALLA - RED)
```

#### PASO 4.2: Implementación - ControladorComentario

**Archivo:** `src/main/java/com/tallerwebi/presentacion/ControladorComentario.java`

**GREEN: Controlador REST/Web**

```java
@Controller
@RequestMapping("/comentarios")
public class ControladorComentario {
  
  @Autowired
  private ServicioComentario servicioComentario;
  
  /**
   * Muestra el formulario de nuevo comentario.
   *
   * @return vista del formulario
   */
  @GetMapping
  public String obtenerFormulario(Model model) {
    model.addAttribute("comentario", new Comentario());
    return "comentarios/formulario";
  }
  
  /**
   * Guarda un nuevo comentario validado.
   *
   * @param comentario el comentario desde el formulario
   * @param bindingResult resultado de validación
   * @param redirectAttributes para mensajes flash
   * @return redirección a lista o formulario
   */
  @PostMapping
  public String guardarComentario(
      @ModelAttribute("comentario") Comentario comentario,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {
    
    try {
      servicioComentario.guardar(comentario);
      redirectAttributes.addFlashAttribute(
        "mensaje",
        "Comentario guardado exitosamente"
      );
      return "redirect:/comentarios";
    } catch (ComentarioInvalido e) {
      bindingResult.reject("comentario.invalido", e.getMessage());
      return "comentarios/formulario";
    }
  }
}
```

- [ ] Crear controlador con rutas GET/POST
- [ ] Implementar manejo de excepciones
- [ ] Usar `RedirectAttributes` para mensajes
- [ ] Ejecutar: `mvn test -Dtest=ControladorComentarioTest` (PASA)

#### PASO 4.3: Refactor - ControladorComentario

**REFACTOR: Checkstyle/PMD**

- [ ] Líneas < 100 caracteres
- [ ] Javadoc en todos los métodos
- [ ] Ejecutar: `mvn clean verify`

---

### FASE 5: VISTA (Thymeleaf)

#### PASO 5.1: Crear Vista - formulario.html

**Archivo:** `src/main/webapp/WEB-INF/views/comentarios/formulario.html`

**Checklist de implementación:**
```
[ ] Crear archivo HTML5 con Thymeleaf
[ ] Form con th:object="${comentario}"
[ ] Input textarea para contenido (maxlength=2000)
[ ] Mostrar contador de caracteres en tiempo real (JavaScript)
[ ] Validación HTML5 (required, maxlength)
[ ] Botón submit
[ ] Bloque th:if="${mensaje}" para mensaje de éxito
[ ] Bloque th:errors para mostrar errores
[ ] Estilos CSS responsivos (Bootstrap o similar)
```

**Estructura mínima:**
```html
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>Nuevo Comentario</title>
  <link rel="stylesheet" 
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body>
  <div class="container mt-5">
    <h1>Agregar Comentario</h1>
    
    <!-- Mensaje de éxito -->
    <div th:if="${mensaje}" class="alert alert-success" role="alert">
      <span th:text="${mensaje}">Comentario guardado</span>
    </div>
    
    <!-- Formulario -->
    <form th:action="@{/comentarios}" 
          th:object="${comentario}" 
          method="POST" 
          class="mt-4">
      
      <div class="mb-3">
        <label for="contenido" class="form-label">Comentario</label>
        <textarea class="form-control" 
                  id="contenido" 
                  th:field="*{contenido}" 
                  maxlength="2000" 
                  rows="5" 
                  required></textarea>
        <small class="form-text text-muted">
          <span id="contador">0</span>/2000 caracteres
        </small>
        <div th:if="${#fields.hasErrors('contenido')}" 
             class="alert alert-danger mt-2">
          <span th:errors="*{contenido}">Error</span>
        </div>
      </div>
      
      <button type="submit" class="btn btn-primary">Guardar</button>
      <a th:href="@{/}" class="btn btn-secondary">Cancelar</a>
    </form>
  </div>
  
  <script>
    document.getElementById('contenido').addEventListener('input', 
    function() {
      document.getElementById('contador').textContent = this.value.length;
    });
  </script>
</body>
</html>
```

- [ ] HTML válido y semántico
- [ ] Mensaje de éxito: `th:if="${mensaje}"`
- [ ] Contador de caracteres en tiempo real
- [ ] Validación de errores con `th:errors`
- [ ] Responsive design

#### PASO 5.2: Vista - lista.html (opcional pero recomendado)

**Archivo:** `src/main/webapp/WEB-INF/views/comentarios/lista.html`

- [ ] Listar comentarios guardados
- [ ] Mostrar fecha, contenido, usuario
- [ ] Link para agregar nuevo comentario

---

### FASE 6: VALIDACIÓN INTEGRAL

#### PASO 6.1: Scripts DDL en data.sql

**Archivo:** `src/main/resources/data.sql`

- [ ] Agregar script de creación de tabla `comentarios`
- [ ] Verificar que sea compatible con HSQLDB (tests)

#### PASO 6.2: Validación Checkstyle

```bash
[ ] Ejecutar: mvn checkstyle:check
[ ] Verificar línea máx 100 caracteres
[ ] Sin warning/errors
```

#### PASO 6.3: Validación PMD

```bash
[ ] Ejecutar: mvn pmd:check
[ ] Verificar: sin variables no usadas
[ ] Verificar: complejidad ciclomática < 10
```

#### PASO 6.4: Validación JaCoCo (Cobertura)

```bash
[ ] Ejecutar: mvn test
[ ] Ejecutar: mvn jacoco:report
[ ] Abrir: target/site/jacoco/index.html
[ ] Verificar: BUNDLE/LINE ≥ 80%
[ ] Clases cubiertas:
    [ ] Comentario: ≥ 80%
    [ ] RepositorioComentarioImpl: ≥ 80%
    [ ] ServicioComentarioImpl: ≥ 80%
    [ ] ControladorComentario: ≥ 80%
```

#### PASO 6.5: Validación Completa

```bash
[ ] Ejecutar: mvn clean verify
[ ] Resultado esperado: BUILD SUCCESS
[ ] Ejecutar: mvn test
[ ] Resultado esperado: 12+ tests PASSED
[ ] Verificar logs: sin errores PMD/Checkstyle
```

#### PASO 6.6: Testeo Manual en Navegador

```
[ ] Iniciar servidor: mvn spring-boot:run (o Tomcat/Jetty)
[ ] Acceder a: http://localhost:8080/comentarios
[ ] Ingresar comentario válido (500 caracteres)
[ ] Verificar: mensaje "Comentario guardado exitosamente"
[ ] Verificar: formulario se limpia
[ ] Ingresar comentario > 2000 caracteres
[ ] Verificar: error mostrado en rojo
[ ] Ingresar comentario vacío
[ ] Verificar: error "no puede estar vacío"
[ ] Verificar: contador de caracteres funciona en tiempo real
```

---

## 📊 Resumen de Tests Esperados

### Total de Tests: 10

| Componente | Casos de Éxito | Casos de Error | Total |
|---|---|---|---|
| RepositorioComentarioTest | 2 | 0 | 2 |
| ServicioComentarioTest | 2 | 4 | 6 |
| ControladorComentarioTest | 2 | 2 | 4 |
| **TOTAL** | **6** | **6** | **10 + Integrales** |

---

## 🎯 Validación Final

### Criterios de "DONE"

```
[ ] Todos los 14 tests PASEN
[ ] Checkstyle: 0 errores
[ ] PMD: 0 errores (no ignorar)
[ ] JaCoCo: BUNDLE/LINE ≥ 80%
[ ] Lineas de código: máx 100 caracteres
[ ] Javadoc: en todos los métodos públicos
[ ] Vista: mensaje de éxito visible y validaciones funcionales
[ ] mvn clean verify: BUILD SUCCESS
[ ] Git: commit con mensaje descriptivo
```

### Commit Message Sugerido

```
feat(comentarios): implementar guardar comentarios con máx 2000 caracteres

- Agregar entidad Comentario con validaciones
- Implementar RepositorioComentario (método guardar)
- Implementar ServicioComentario (lógica de negocio)
- Implementar ControladorComentario (HTTP endpoints)
- Vista Thymeleaf con validación HTML5
- 10 tests unitarios con cobertura ≥ 80%
- Cumple Checkstyle/PMD/JaCoCo

Fixes: #XYZ
```

---

## 📚 Referencias

- **CONTRIBUTING_TDD.md:** Estándares del proyecto
- **ServicioLoginTest.java:** Patrón de testing (BDD)
- **checkstyle-base.xml:** Reglas de estilo
- **pmd-reglas-de-codigo.xml:** Reglas de calidad

---

**Versión:** 1.0  
**Estado:** Listo para Implementación  
**Próximo Paso:** FASE 1 - Crear Entidad Comentario
