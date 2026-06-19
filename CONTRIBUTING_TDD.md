# Lineamientos de Desarrollo y TDD (TallerWebi)

Este documento define las reglas de juego para el desarrollo de funcionalidades y la escritura de tests. Todo agente de IA (Copilot/Cursor) o desarrollador debe seguir estos estándares para mantener la integridad del proyecto.

## 1. Estructura del Proyecto (Maven Standard)
El proyecto utiliza estrictamente la estructura de directorios de Maven. No mover ni centralizar archivos.
- **Código Productivo:** `src/main/java/com/tallerwebi/...`
- **Código de Tests:** `src/test/java/com/tallerwebi/...`

## 2. Estándares de Testing (TDD)
Seguir estrictamente el patrón de `ServicioLoginTest.java`.

### Regla de Oro: Autonomía en el Ciclo TDD (Auto-iteración)
Cualquier instrucción de implementación que reciba el agente debe seguir obligatoriamente este ciclo antes de considerarse "completa":
1. **Red:** Generar/Ejecutar el test y verificar que falle.
2. **Green:** Generar la implementación mínima para que el test pase.
3. **Refactor:** Limpiar código y asegurar cumplimiento de estilo (Checkstyle/PMD).
4. **Validate:** Ejecutar `mvn validate`.
Si el agente no puede ejecutar la terminal él mismo (plan gratuito), debe **mostrarte los prompts de prueba y los bloques de código necesarios** para que tú solo tengas que aceptar los cambios (Aceptar Diff) en orden.


### Nomenclatura de Métodos:
- Deben seguir el formato: `[accion]Deberia[resultadoEsperado]`.
- Ejemplos: `deberiaCrearUnUsuarioConCredencialesValidas`, `noDeberiaCrearUnUsuarioConEmailVacio`.

### Estructura y Estilo BDD (Comentarios de soporte):
- Se deben usar comentarios para estructurar los tests siguiendo el estilo BDD (Given-When-Then), traducido al español:
  1. `// dado` (Preparación): `when(...).thenReturn(...)`
  2. `// cuando` (Ejecución): Llamada al método a probar.
  3. `// entonces` (Validación): `assertThat(...)` o `verify(...)`.

*Nota: Aunque el ejemplo `ServicioLoginTest.java` usa "preparacion", "ejecucion", "validacion", los nuevos desarrollos deben migrar a "dado", "cuando", "entonces" para mayor claridad BDD.*


## 3. Calidad de Código
Cualquier código generado por un agente **debe** pasar automáticamente estas validaciones. Si el agente comete errores, debe autocorregirse:
- **Checkstyle:** Basado en `checkstyle-base.xml`. Longitud de línea máxima: 100 caracteres.
- **PMD:** Se deben respetar las reglas configuradas en `pmd-reglas-de-codigo.xml`.
- **Cobertura (JaCoCo):** Obligatorio mantener una cobertura mínima del 80% (métrica `BUNDLE` en `LINE`).
- **Formato:** Se utiliza `prettier-maven-plugin`. No es necesario formatear manualmente, el build lo hará, pero el código generado debe ser limpio y legible.
