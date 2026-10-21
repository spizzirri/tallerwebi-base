package com.valhalla.presentation.shared;

/**
 * [PROF] Reglas de formato del avatar URL (11-PROF AC-25). Vive en presentation/compartido por
 * que el valor llega desde DOS forms (registro self-service y edicion de perfil) y la regla es
 * una sola.
 *
 * <p>Solo se acepta la GALERIA del sitio ({@code /images/avatars/...}, los 3 SVG de PROF-012) o
 * una URL externa {@code https://...}. Todo lo demas — {@code http://}, {@code javascript:},
 * {@code data:}, paths de la app que no son de la galeria — rechaza. Un valor vacio es valido
 * obligatoriamente: el avatar es opcional, y vaciarlo es una eleccion legitima (el agregado
 * normaliza blanco a null, U-06).
 */
public final class AvatarUrlPolicy {

  /** Mensaje canonico del reject en ambos controllers: un contrato, un texto. */
  public static final String REJECT_MESSAGE =
    "El avatar debe ser una URL segura (https://) o una imagen de la galeria del sitio";

  private static final String HTTPS_PREFIX = "https://";
  private static final String BUNDLED_PREFIX = "/images/avatars/";

  private AvatarUrlPolicy() {}

  public static boolean isValid(String url) {
    if (url == null || url.trim().isEmpty()) {
      return true; // opcional: vaciar el avatar es una eleccion legitima
    }
    String value = url.trim();
    return hasMeaningfulRest(value, HTTPS_PREFIX) || hasMeaningfulRest(value, BUNDLED_PREFIX);
  }

  /**
   * El prefijo tiene que estar y algo tiene que seguirle: {@code https://} solo, o la galeria
   * sin nombre de archivo, no identifican una imagen.
   */
  private static boolean hasMeaningfulRest(String value, String prefix) {
    return value.startsWith(prefix) && value.length() > prefix.length();
  }
}
