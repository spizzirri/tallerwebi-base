# Confirmaciones con toasts

Las confirmaciones aparecen abajo a la derecha durante seis segundos, con cierre manual
accesible. El contador se pausa con el mouse o el foco dentro del aviso. El componente usa
texto seguro, admite varios avisos y respeta el tema oscuro.

## Criterio

Se reserva el toast para acciones destructivas, cambios de seguridad o permisos y envíos
cuyo resultado no es evidente en la pantalla. Las acciones frecuentes que ya actualizan
visiblemente la interfaz conservan su comportamiento original.

## Acciones con toast

| Área | Confirmaciones |
| --- | --- |
| Planes | Unirse, eliminar y salir |
| Participación | Solicitar acceso, expulsar, bloquear y desbloquear |
| Invitaciones | Enviar invitación y aceptar o rechazar desde Mis planes |
| Configuración | Guardar permisos del plan |
| Perfil | Cambiar contraseña |
| Administración | Eliminar usuarios y lugares |

## Acciones restauradas

Crear, editar y clonar planes; responder solicitudes;
cambiar roles; modificar el itinerario; calificar; guardar, quitar y clonar desde Guardados;
editar el perfil; cerrar sesión; crear o editar lugares; editar, activar y desactivar usuarios.
Cada una recupera la navegación, los cambios visibles y los avisos que tenía antes de esta tarea.

Enviar una solicitud de acceso, tanto desde el enlace como desde el código de un plan privado,
redirige a Mis planes con confirmación. Visitar nuevamente el enlace privado conserva el resumen
y el estado pendiente, necesarios para entender por qué todavía no se puede acceder.

Se conservan las pantallas de contraseña provisoria y los avisos administrativos que contienen
una clave para copiar, las confirmaciones previas a acciones destructivas, los errores de
validación y los estados persistentes de acceso, bloqueos o lugares ya importados.

Los formularios usan mensajes flash de Spring para las nuevas confirmaciones. Los avisos
de cambio de contraseña y configuración consumen y eliminan sus parámetros de URL.
Las operaciones asincrónicas del itinerario y Guardados recuperan su comportamiento original.

## Seguimiento

Trello no estaba disponible entre las herramientas de la sesión. Este documento registra
el alcance para trasladarlo a la tarjeta correspondiente cuando se restablezca la conexión.
