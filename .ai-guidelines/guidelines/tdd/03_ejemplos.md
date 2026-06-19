# Ejemplos de Pruebas y Verificación

Para mantener la consistencia, todo test debe seguir la estructura clara de **preparación, ejecución y validación** (BDD Style), tal como se observa en `ServicioLoginTest.java`.

```java
@Test
public void registrarUsuarioSiExisteDeberiaLanzarExcepcion() {
  // preparacion (Given)
  Usuario usuario = new Usuario();
  usuario.setEmail("existe@test.com");
  when(this.repositorioUsuarioMock.buscarUsuario(usuario.getEmail(), usuario.getPassword()))
    .thenReturn(new Usuario());

  // ejecucion (When)
  // Uso de assertThrows para excepciones y this para llamar a los métodos de servicio
  assertThrows(UsuarioExistente.class, () -> this.servicioLogin.registrar(usuario));

  // validacion (Then)
  // Verificación de que NO se llamó al método de persistencia mediante Mockito
  verify(this.repositorioUsuarioMock, times(0)).guardar(usuario);
}
```

## Reglas de Verificación:
- **Resultados**: Usar `assertThat(actual, equalTo(esperado))` (Hamcrest).
- **Interacciones**: Usar `verify(mock, times(n)).metodo(...)` para verificar llamadas a dependencias.
- **Excepciones**: Usar `assertThrows(Excepcion.class, () -> ...)` para validar flujos negativos.
