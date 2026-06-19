# Reglas y Estándares para TDD

Este documento define las reglas de implementación obligatorias al realizar TDD en `tallerwebi-base`.

## 1. Ciclo de Trabajo
- **Red**: Escribir una prueba que falle.
- **Green**: Escribir el código mínimo necesario para que la prueba pase.
- **Refactor**: Mejorar el código sin cambiar su comportamiento.

## 2. Nomenclatura (BDD Style)
- Las clases de test deben terminar en `Test` (ej: `TareaServiceTest.java`).
- Los métodos deben seguir una estructura BDD: `deberia[Accion]Cuando[Condicion]`.
    - Ejemplo: `deberiaCrearUnUsuarioConCredencialesValidas`.

## 3. Estructura de Tests (BDD)
- Se deben usar comentarios para estructurar los tests siguiendo el estilo BDD (Given-When-Then), traducido al español:
  1. `// dado` (Preparación): `when(...).thenReturn(...)`
  2. `// cuando` (Ejecución): Llamada al método a probar.
  3. `// entonces` (Validación): `assertThat(...)` o `verify(...)`.

*Nota: Aunque legacy code use "preparacion", "ejecucion", "validacion", los nuevos desarrollos deben migrar a "dado", "cuando", "entonces".*

## 4. Reglas de Implementación
- **Aislamiento**: Las pruebas de dominio NO deben levantar el contexto de Spring.
- **Contexto**: Las pruebas de integración deben usar las anotaciones de Spring (`@SpringBootTest`, `@DataJpaTest`, `@WebMvcTest`) según corresponda.
- **Código**: Usar siempre `this` para referenciar métodos y atributos de clase en las pruebas para claridad.
- **Organización**: Los tests deben ubicarse en la carpeta correspondiente a su tipo (ej: `integracion/` para tests con contexto de Spring, o la carpeta de la capa para tests unitarios).

