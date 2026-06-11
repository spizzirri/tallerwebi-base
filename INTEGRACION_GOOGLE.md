# Integración con Google OAuth2

Para poder utilizar el inicio de sesión con Google en esta aplicación, es necesario obtener las credenciales de un proyecto en Google Cloud:

## Paso 1: Crear un proyecto en Google Cloud

1. Ir a la [Consola de Google Cloud](https://console.cloud.google.com/).
2. Si es tu primera vez, crea una cuenta o inicia sesión.
3. Hacer clic en el selector de proyectos en la parte superior y seleccionar **"Proyecto nuevo"**.
4. Dale un nombre a tu proyecto y hacer clic en **"Crear"**.

## Paso 2: Configurar la pantalla de consentimiento de OAuth

1. En el menú lateral izquierdo, ir a **"APIs y servicios"** > **"Pantalla de consentimiento de OAuth"**.
2. Seleccionar el tipo de usuario **"Externo"** y hacer clic en **"Crear"**.
3. En **"Información de la aplicación"**, completar los campos obligatorios (Nombre de la aplicación, correo electrónico de soporte).
4. En **"Información de contacto del desarrollador"**, ingresa tu correo electrónico.
5. Hacer clic en **"Guardar y continuar"** hasta finalizar la configuración. No es necesario agregar ámbitos (scopes) adicionales en esta etapa básica, ya que usaremos los predeterminados (`openid`, `profile`, `email`).

## Paso 3: Crear credenciales (ID de cliente y Secreto)

1. Ir a **"APIs y servicios"** > **"Credenciales"**.
2. Hacer clic en **"Crear credenciales"** y seleccionar **"ID de cliente de OAuth"**.
3. En **"Tipo de aplicación"**, seleccionar **"Aplicación web"**.
4. Asignar un nombre (p. ej., "Taller Web I Login").
5. En **"URIs de redireccionamiento autorizadas"**, hacer clic en **"Añadir URI"** y pegar la siguiente (debe coincidir exactamente):
   `http://localhost:8080/spring/login/oauth2/code/google`
6. Hacer clic en **"Crear"**.
7. Se mostrarán tu **ID de cliente** y **Secreto del cliente**. Copia estos valores.

## Paso 4: Configurar la aplicación

1. Abrir o crear un archivo de configuración (`application.properties` o similar). Puede estar en otra ruta.
2. Agregar las siguientes variables con los valores obtenidos:

```properties
GOOGLE_CLIENT_ID=CLIENT_ID
GOOGLE_CLIENT_SECRET=CLIENT_SECRET
```

## Paso 5: Ejecutar la aplicación

Para levantar la aplicación con la configuración de propiedades necesaria, utilizar el siguiente comando (ajustar la ruta del archivo si es necesario):

```bash
mvn clean jetty:run -Djetty.systemPropertiesFile=/ruta/a/application.properties
```

## Solución de problemas comunes

*   **Error `redirect_uri_mismatch`**:
    *   Verificar que la URI en la consola de Google coincida al 100% (incluyendo el `/spring` del `contextPath` definido en el `pom.xml`).
*   **El inicio de sesión no pide elegir cuenta**:
    *   Google está usando una sesión activa en tu navegador. Limpiar las cookies del navegador o usar una ventana de incógnito.
*   **El usuario no se registra en la base de datos**:
    *   Revisar la terminal de Maven (`mvn jetty:run`) para ver si hay mensajes de error en los logs.
    *   Revisar que las credenciales (`GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`) sean correctas y no tengan espacios en blanco.
*   **Error de `503 Service Unavailable` al iniciar**:
    *   Generalmente significa que hay otra instancia de Jetty corriendo. Detener cualquier proceso anterior (`fuser -k 8080/tcp`).
