# PET: primera versión funcional

Esta entrega en `task-20-PET` implementa el pedido de acceso a planes privados.
No incorpora la rama `task-19-BAN`. Spec: `docs/specs/21-PET.md`.

> Actualización card #49: se eliminó la configuración de "Compartir link"; la
> visibilidad del plan gobierna el acceso. Tampoco hay un botón para copiar el link
> desde la configuración del plan. Ver `docs/specs/21-PET.md`.

## Cómo probar el flujo manualmente

1. Crear un plan privado y entrar en su detalle con la cuenta del dueño.
2. Abrir manualmente `/plans/{id}/public`, usando el id que aparece en la URL del
   detalle. La visibilidad del plan define el acceso.
3. Abrir esa URL con otra cuenta registrada. Sin sesión, el sitio lleva al login y
   conserva el destino para volver después de ingresar.
4. La landing muestra nombre, descripción, fecha, cantidad de lugares y un cover
   genérico. No muestra dueño, participantes, emails, short code ni itinerario.
5. Pulsar **Solicitar participación**. La misma pantalla informa que el pedido está
   pendiente; volver a enviarlo no crea otra solicitud.
6. Desde el detalle, el dueño ve el bloque **Solicitudes** entre el resumen del
   plan y el formulario de invitación, con enlace al perfil y acciones **Aceptar** y
   **Rechazar**. Puede habilitar esta gestión para editores en la configuración.
   Los participantes comunes no pueden gestionar solicitudes.
7. Al aceptar, la persona pasa a participante. Al abrir nuevamente el link, accede
   directamente al detalle. Rechazar elimina el pedido y permite solicitar de nuevo.

Ingresar el código corto de un plan privado también genera una solicitud: nunca
incorpora directamente al visitante. La pantalla confirma que debe esperar la
aprobación del dueño o de un editor autorizado. Los planes públicos conservan el
ingreso directo por código. El dueño y quienes ya son participantes van
directamente al detalle, sin crear una solicitud.

Los permisos de invitación y de gestión de solicitudes tienen sus defaults
(**Solo dueño** para gestionar solicitudes e invitar por email; **Dueño y editores**
para invitar dentro de la app). Un plan privado siempre muestra el resumen y el
formulario de solicitud en la landing; un plan público conserva su vista pública.
Solo el dueño puede cambiar los permisos de roles; dueño y editores administran
datos, solicitudes y bans. La configuración `#configuracion` conserva datos, roles y
baneados; las solicitudes viven junto a participantes.
Los endpoints también verifican los permisos y los formularios conservan la
protección CSRF existente.

## Persistencia

Hibernate agrega la columna `handle_requests_permission` a `plans` y crea
`plan_join_requests` al iniciar la aplicación con la configuración actual
`hibernate.hbm2ddl.auto=update`. La columna `share_link_permission` puede quedar en
bases existentes, pero ya no se mapea ni se usa. No hace falta borrar la base. Para
los planes existentes, los valores nulos se interpretan como **Solo dueño**.

Cada pedido es único por plan y solicitante. Se elimina al responderlo, al aceptar
una invitación al mismo plan o al ingresar por código a un plan que pasó a público.
Cambiar los permisos no elimina los pedidos pendientes, que pueden seguir
respondiéndose desde el detalle.

## Permisos e invitaciones por canal

La sección **Roles y permisos** del detalle y el paso 3 de `/explore` incluyen tres permisos:
invitación interna (Dueño y editores), invitación por email (Solo dueño) y
gestionar solicitudes (Solo dueño). Los dos primeros admiten Todos los
participantes; gestionar solicitudes solo admite dueño o editores. El permiso de
invitación no permite a un participante proponer el rol Editor. Solo el dueño
puede cambiar estos permisos, incluso si un editor puede modificar los datos y el
itinerario. La edición ordinaria no resetea los permisos guardados.

En el detalle, el formulario muestra los canales autorizados para la cuenta.
Ambos requieren una cuenta destinataria registrada y dejan una invitación en
Mis planes. El canal email agrega un token aleatorio de un solo uso. El GET
`/plans/{id}/invitations/{token}/accept` es público y redirige al login sin
sesión; luego muestra una confirmación. La aceptación efectiva usa POST con
CSRF y exige la cuenta destinataria. Abrir un enlace no incorpora al usuario.
Aceptar o rechazar la invitación desde Mis planes también invalida el token.
Las invitaciones anteriores sin token siguen respondiéndose desde Mis planes.

Hibernate agrega `invite_internal_permission`, `invite_email_permission` y
`plan_invitations.token` con la configuración actual `hbm2ddl.auto=update`.
No se requiere borrar la base de datos. Los permisos nulos usan sus defaults.

## Email

`.env.example` documenta APP_BASE_URL y SMTP_HOST/PORT/USERNAME/PASSWORD/FROM,
SMTP_AUTH, SMTP_STARTTLS y SMTP_SSL; Compose los pasa al proceso de la app.
En ejecución local con Maven deben definirse como variables del proceso.
La configuración se resuelve mediante `Environment` de Spring, conservando las
variables y los defaults anteriores. Las pruebas SMTP sustituyen el transporte
para comprobar remitente, destinatario, URL, opciones y errores sin enviar correo.
APP_BASE_URL debe ser la URL externa desde la que se abre PlanIt.
Sin SMTP_HOST se usa LoggingEmailNotifier: la invitación queda en Mis planes,
no se envía correo y no se imprimen tokens ni destinatarios en logs.
Con SMTP configurado, un fallo de envío revierte la invitación y muestra un
mensaje para reintentar o utilizar el canal interno.

## Fuera de alcance

Bans, bannedUsers, la decisión ban y quitar-ban se integrarán desde `task-19-BAN`
(AC-05, AC-10, AC-14 y AC-15 de la spec). Esta rama no los implementa ni los
declara cubiertos.

## Verificación de esta ampliación

La ampliación de pruebas incluye:

- `PlanTest`: defaults, valores nulos heredados, validación, capacidades por rol
  y conservación de permisos durante una edición ordinaria.
- `PlanServiceImplTest`: solicitudes idempotentes sin acceso implícito, rechazo
  de solicitudes en planes públicos, miembros existentes,
  aceptación, rechazo, limpieza de invitaciones, delegación y settings del dueño.
- `PlanControllerTest`: landing, identidad de sesión, mapeo de los tres permisos
  al crear y mensaje de error ante fallos SMTP.
- `PlanControllerIntegrationTest`: persistencia y renderizado de permisos,
  actualización de settings, aceptación por token y protección CSRF.
- `PlanJoinRequestSecurityTest`: seis escenarios de seguridad sobre login,
  privacidad, landing de solicitud, CSRF, identidad de sesión y delegación. Agrega
  integración de duplicados, aceptación, rechazo con reintento y resolución de
  solicitudes existentes después de cambiar los permisos.
- `EmailConfigurationTest`: fallback sin filtrar destinatarios ni tokens,
  transporte SMTP simulado, URL de aceptación, defaults y propagación de errores.
- `PublicPlanJoinRequestE2E`: enlace compartido por el dueño, solicitud desde otra
  sesión, aceptación y acceso al itinerario privado desde el navegador.

El gate descubre las nuevas clases automáticamente por paquete y sufijo:
`*Test` para unitarias/integración y `*E2E` para navegador. No se modifican sus
umbrales de cobertura ni se omiten capas. Comando de verificación completa:
`powershell -File scripts/gate.ps1 all -Port 8081`.

La preparación de E2E elimina `plan_join_requests` antes que planes y usuarios,
y reinicia su secuencia para evitar fallos de claves foráneas entre escenarios.
El escenario de clonación desde el detalle crea un plan público para ingresar
directamente por código; la entrada privada se prueba mediante solicitud y aceptación.
El escenario de detalles de visita también avanza al nuevo paso de privacidad
antes de crear el plan, conservando la hora de visita del paso anterior.

Verificación final del 22/10/2026, sobre `main` ya integrado (bookmarks de
[MARK] y seguidos de [FOLLOW] incluidos): `BUILD SUCCESS` con el comando completo
anterior, 823 pruebas unitarias/integración y 60 E2E, sin fallos, errores ni
pruebas omitidas. Se agregaron 30 pruebas unitarias/integración y un flujo E2E
sobre la base de `main`. Pasan Prettier, Checkstyle, PMD, CPD y todos los umbrales
JaCoCo: dominio al 100 % de líneas y ramas, presentación al 100 % de líneas y
correo al 100 % de líneas. Cobertura global: 99,4 % de líneas y 93,1 % de ramas.
Registro local: `target/pet-full-gate.log`.

Los escenarios de ban y quitar-ban siguen pendientes de integrar su funcionalidad;
no se marcan como cubiertos. Tampoco se verifica la entrega con un SMTP real.
