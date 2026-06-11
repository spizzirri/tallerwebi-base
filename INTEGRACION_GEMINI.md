# Guía de Configuración de API Key para Google Gemini

## Generación de API Key en AI Studio
1.  Acceder a [Google AI Studio](https://aistudio.google.com/).
2.  Iniciar sesión con tu cuenta de Google.
3.  Hacer clic en "Get API key" en la barra lateral.
4.  Seleccionar "Create API key" (se puede crear en un proyecto de Google Cloud existente o crear uno nuevo).
5.  Copiar la clave generada y guardarla de forma segura.

## Configuración de la API Key
La clave de la API debe ser referenciada bajo el nombre `GEMINI_API_KEY` en el archivo de configuración.

### Formato del archivo
El archivo de propiedades (ej. `application.properties`) debe definir la clave de la siguiente forma:

```properties
GEMINI_API_KEY=api_key
```

Para ejecutar la aplicación utilizando dicha configuración:

```bash
mvn jetty:run -Djetty.systemPropertiesFile=/ruta/a/application.properties
```

## Seguridad y Buenas Prácticas
*   **Nunca compartir la API Key:** No incluyas el archivo que contiene la clave real en el repositorio. Asegúrate de que dicho archivo esté listado en el `.gitignore`.
*   **Uso de variables de entorno:** En entornos de producción o CI/CD, se recomienda inyectar la clave directamente como una variable de entorno (`GEMINI_API_KEY`) en lugar de usar archivos de configuración locales.

## Modelos Disponibles
Los modelos principales de la familia Gemini accesibles a través de la API incluyen:
*   **Gemini 2.5 Flash Lite**: Modelo optimizado para máxima velocidad, mayores límites de peticiones y alta eficiencia.
*   **Gemini 2.5 Flash**: Modelo equilibrado, estable y rápido.
*   **Gemini 1.5 Pro**: Diseñado para tareas complejas que requieren razonamiento profundo y una ventana de contexto amplia.

## Funcionalidades Principales
*   **Preguntar**: Envía un mensaje directo al modelo de Gemini para obtener una respuesta basada en el conocimiento general del modelo.
*   **Preguntar con regla**: Permite enviar un mensaje junto con una instrucción de formato o restricción específica ("regla"), guiando al modelo para que ajuste su respuesta a parámetros definidos.
*   **Limpiar**: Restablece el historial de la conversación actual en el servicio, permitiendo iniciar una interacción nueva sin el contexto acumulado de preguntas anteriores.

## Solución de Problemas Comunes (Troubleshooting)
*   **Error 401/403 (Unauthorized/Forbidden):** Verificar que la `GEMINI_API_KEY` sea válida y esté activa en Google AI Studio.
*   **Error 429 (Too Many Requests):** Se alcanzado el límite de cuota del modelo. Considerar cambiar a un modelo con límites más altos (ej. *Gemini 2.5 Flash Lite*).
*   **Error de Red / Timeout:** Si la petición falla, revisar la conexión a internet o los límites de tiempo configurados en el `RestTemplate`.
*   **Logs de Aplicación:** Revisar la consola o los logs del servidor para obtener detalles sobre la respuesta recibida por la API.
