package com.valhalla.domain.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * [NGRP] Reglas de lectura para agrupar notificaciones repetibles.
 *
 * <p>Se agrupan solo los tipos repetibles ({@link NotificationType#isRepeatable()}): seguidores,
 * participantes y planes publicados. Las invitaciones, solicitudes y avisos de acceso conservan
 * una fila por evento porque cada una pide una decision distinta (NGRP-005). Las filas leidas
 * siguen dentro del grupo: marcarlas no lo desarma, solo las muestra atenuadas. El conteo del
 * badge sigue siendo por fila no leida.
 */
public final class NotificationGrouping {

  private NotificationGrouping() {}

  /**
   * Arma los grupos en el orden recibido. El listado del repositorio ya viene de mas nuevo a mas
   * viejo, y el grupo se queda en la posicion de su primera fila para no alterar ese orden.
   */
  public static List<NotificationGroup> group(List<Notification> notifications) {
    List<NotificationGroup> groups = new ArrayList<>();
    Map<GroupKey, Integer> openGroups = new HashMap<>();
    for (Notification notification : notifications) {
      GroupKey key = groupKeyOf(notification);
      if (key == null) {
        groups.add(NotificationGroup.of(List.of(notification)));
        continue;
      }
      Integer index = openGroups.get(key);
      if (index == null || !groups.get(index).accepts(notification)) {
        groups.add(NotificationGroup.of(List.of(notification)));
        openGroups.put(key, groups.size() - 1);
      } else {
        groups.set(index, groups.get(index).plus(notification));
      }
    }
    return groups;
  }

  private static GroupKey groupKeyOf(Notification notification) {
    if (!notification.getType().isRepeatable()) {
      return null;
    }
    return new GroupKey(notification.getType(), notification.getRelatedPlanId());
  }

  /** Clave de NGRP-001: mismo tipo y mismo plan; el plan nulo es una clave valida. */
  private record GroupKey(NotificationType type, UUID planId) {}
}
