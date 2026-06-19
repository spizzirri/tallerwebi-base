# Spec: Listado de Comentarios

## 1. Contexto de la Funcionalidad
- **Feature**: Mostrar listado de comentarios.
- **Objetivo**: Permitir a los usuarios visualizar los comentarios existentes en una tabla, mostrando el contenido.

## 2. Escenarios (BDD - Given-When-Then)

### Escenario: Visualización exitosa de comentarios existentes
- **Given**: Existen comentarios guardados en el sistema.
- **When**: El usuario accede a la página de listado de comentarios.
- **Then**: Se muestra una tabla con todos los comentarios registrados, mostrando la columna de contenido.

### Escenario: Listado vacío
- **Given**: No existen comentarios registrados.
- **When**: El usuario accede a la página de listado de comentarios.
- **Then**: Se muestra un mensaje informativo: "No hay comentarios disponibles", en lugar de una tabla vacía.

## 3. Casos Borde y Flujos Alternativos
- **Error**: Error al intentar recuperar los comentarios del repositorio (debe mostrarse un mensaje de error amigable al usuario).
- **Borde**: Comentario de hasta 2000 caracteres.

## 4. Plan de Pruebas (Technical Mapping)
*Mapeo de pruebas según la arquitectura y tipo de test (Unitario vs Integración):*

### Capa Presentación (Controlador)

#### Pruebas Unitarias
*Pruebas aisladas del controlador instanciando la clase con `new` y usando mocks para el servicio. Validan la lógica del `ModelAndView`.*
- **deberiaRetornarVistaComentariosCuandoSeAccedeAlListado**: Verifica que el nombre de la vista sea el esperado y el modelo contenga la lista.
- **deberiaRetornarMensajeDeErrorCuandoElServicioFalla**: Verifica que ante una excepción del servicio, el controlador redirija o devuelva el mensaje de error adecuado en el modelo.

#### Pruebas de Integración (Web Layer)
*Requieren `@WebMvcTest` y `MockMvc` para validar el contrato HTTP y el renderizado.*
- **deberiaEstarDisponibleEnLaUrlListadoComentarios**: Verifica que el endpoint `/listado-comentarios` sea accesible vía GET y retorne status 200.
- **deberiaRenderizarTablaConComentariosEnLaVista**: Verifica que el HTML renderizado por Thymeleaf contiene las filas (`<tr>`) con el contenido dinámico correcto.
- **deberiaMostrarMensajeInformativoCuandoNoHayDatos**: Verifica que ante una lista vacía, el HTML renderizado contenga el texto "No hay comentarios disponibles".

### Capa Dominio (Pruebas Unitarias)
*Aisladas (sin contexto de Spring). Mockeo de dependencias externas.*
- **deberiaRetornarListaDeComentariosCuandoElRepositorioDevuelveDatos**: Verifica la lógica de delegación al repositorio.
- **deberiaRetornarListaVaciaCuandoElRepositorioNoTieneComentarios**: Verifica el comportamiento ante lista vacía sin levantar contexto de Spring.

### Capa Infraestructura (Pruebas de Integración - Data Layer)
*Requieren @DataJpaTest para validar persistencia real.*
- **deberiaGuardarYRecuperarComentarioDeHasta2000Caracteres**: Verifica la persistencia en base de datos real (o h2).
- **deberiaRetornarListaVaciaCuandoNoHayComentariosEnLaBaseDeDatos**: Verifica el acceso a datos en una tabla vacía.
