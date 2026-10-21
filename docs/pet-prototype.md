# PET: primera versión funcional

Esta entrega en `task-20-PET` implementa el pedido de acceso a planes privados.
No incorpora la rama `task-19-BAN`.

## Cómo probar el flujo manualmente

1. Crear un plan privado y entrar en su detalle con la cuenta del dueño.
2. Abrir **Configuración del plan** y cambiar **Compartir link** a **Dueño y editores**
   o **Todos los participantes**. Guardar.
3. Volver al detalle y usar **Copiar link**.
4. Abrir el link con otra cuenta registrada. Sin sesión, el sitio lleva al login y
   conserva el destino para volver después de ingresar.
5. La landing muestra nombre, descripción, fecha, cantidad de lugares y un cover
   genérico. No muestra dueño, participantes, emails, short code ni itinerario.
6. Pulsar **Solicitar participación**. La misma pantalla informa que el pedido está
   pendiente; volver a enviarlo no crea otra solicitud.
7. Desde el detalle, el dueño ve **Solicitudes**, con enlace al perfil y acciones
   **Aceptar** y **Rechazar**. Puede habilitar esta gestión para editores en la
   configuración. Los participantes comunes no pueden gestionar solicitudes.
8. Al aceptar, la persona pasa a participante. Al abrir nuevamente el link, accede
   directamente al detalle. Rechazar elimina el pedido y permite solicitar de nuevo.

Ingresar el código corto de un plan privado también genera una solicitud: nunca
incorpora directamente al visitante. La pantalla confirma que debe esperar la
aprobación del dueño o de un editor autorizado. Este ingreso por código funciona
aunque compartir el link esté deshabilitado; en ese caso no se muestra el resumen.
Los planes públicos conservan el ingreso directo por código. El dueño y quienes ya
son participantes van directamente al detalle, sin crear una solicitud.

Ambos permisos nacen en **Solo dueño**. En planes privados, este valor para
**Compartir link** deshabilita resumen y formulario para visitantes. Los miembros
siguen accediendo al detalle. Los planes públicos conservan su vista pública.
Solo el dueño puede modificar la configuración; los endpoints también verifican
los permisos y los formularios conservan la protección CSRF existente.

## Persistencia

Hibernate agrega las columnas `share_link_permission` y `handle_requests_permission`
a `plans` y crea `plan_join_requests` al iniciar la aplicación con la configuración
actual `hibernate.hbm2ddl.auto=update`. No hace falta borrar la base. Para los planes
existentes, los valores nulos se interpretan como **Solo dueño**.

Cada pedido es único por plan y solicitante. Se elimina al responderlo, al aceptar
una invitación al mismo plan o al ingresar por código a un plan que pasó a público. Deshabilitar el link no
elimina los pedidos pendientes, que pueden seguir respondiéndose desde el detalle.

## Pendiente para las próximas entregas

- Banear desde solicitudes, impedir accesos de usuarios baneados y quitar bans.
- Selectores de invitación interna y por email, con autorización por canal.
- Email real por SMTP y aceptación por token.
- Paso de privacidad y permisos en el wizard de creación de `/explore`.
- Tests unitarios, integración, seguridad y E2E, y gate completo.

En esta etapa no se agregan ni se ejecutan tests, por pedido del usuario. La
validación se limita a compilación, formato y controles estáticos. La tarjeta PET
de Trello queda pendiente de sincronización: no hay herramientas Trello disponibles
en esta sesión.
