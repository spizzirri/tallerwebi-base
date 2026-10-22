# Integración Cloudflare R2

## 1. Configuración en Cloudflare R2

1.  **Crear un Bucket**:
    *   Iniciar sesión en el [Dashboard de Cloudflare](https://dash.cloudflare.com/).
    *   Seleccionar **Storage & databases > R2 Object Storage** en el menú lateral izquierdo.
    *   Hacer clic en **Crear bucket** (*Create bucket*).
    *   Asignar un nombre al bucket (será el `r2.bucketName` en la configuración). Solo admite minúsculas (a-z), números (0-9) y guiones (-), entre 3 y 63 caracteres, sin guión al inicio ni al final.
    *   Elegir la ubicación (*Location*) y, como clase de almacenamiento por defecto (*Default storage class*), **Standard**: la capa gratuita no aplica a *Infrequent Access*.
    *   Hacer clic en **Crear bucket**.

2.  **Crear API Token**:
    *   En la página principal de R2, dentro de **Account Details**, hacer clic en **Manage API Tokens**.
    *   Hacer clic en **Create Account API token** (asociado a la cuenta) o **Create User API token** (asociado al usuario).
    *   Asignar un nombre descriptivo al token.
    *   En **Permisos**, seleccionar **Object Read & Write**.
    *   En el alcance de buckets, seleccionar **Apply to specific buckets only** y elegir el bucket creado anteriormente.
    *   Hacer clic en **Create API Token**.

3.  **Obtener Credenciales**:
    *   Copiar los valores de **Access Key ID** (`r2.accessKey`) y **Secret Access Key** (`r2.secretKey`). *Nota: la Secret Access Key no volverá a mostrarse.*
    *   Copiar el **S3 API Endpoint** (`r2.endpoint`), visible en la confirmación del token o en la página principal de R2, con el formato `https://<ACCOUNT_ID>.r2.cloudflarestorage.com`. Los buckets creados con jurisdicción (por ejemplo, UE) usan un endpoint propio: `https://<ACCOUNT_ID>.eu.r2.cloudflarestorage.com`.

## 2. Configuración en la Aplicación

Configurar en un archivo de propiedades externo al proyecto (por ejemplo, `application.properties`) con los valores obtenidos:

```properties
r2.accessKey=<TU_ACCESS_KEY_ID>
r2.secretKey=<TU_SECRET_ACCESS_KEY>
r2.endpoint=https://<ACCOUNT_ID>.r2.cloudflarestorage.com
r2.bucketName=<NOMBRE_DEL_BUCKET>
```

Para iniciar el servidor con esta configuración, ejecutar:
`mvn jetty:run -Djetty.systemPropertiesFile=/ruta/a/tu/application.properties`

El gestor de archivos queda disponible en `http://localhost:8080/spring/gestor-archivos`.

> **Importante**: por ahora la carga de estas propiedades solo está soportada con `mvn jetty:run`. Los contenedores de Docker (Jetty y Tomcat) no reciben las propiedades `r2.*`, y sin ellas la aplicación no inicia.

## 3. Consideraciones Adicionales

### Seguridad
- **Protección de Credenciales**: NO versionar el archivo `application.properties` o cualquier archivo que contenga la `secretKey` a repositorios de código. Es mas seguro guarar el archivo fuera del directorio del proyecto.
- **Principio de Menor Privilegio**: El token creado solo debe tener acceso al bucket específico del proyecto. Si es necesario cambiar permisos, hacerlo desde el dashboard de Cloudflare.

### Límites de Archivo
- **Tamaño de subida**: La aplicación tiene configurado un límite de subida de **50MB** por archivo y por solicitud (constante `TAMANIO_MAXIMO_SUBIDA` en `MyServletInitializer.java`). Si es necesario subir archivos más grandes, ajustar ese valor.

### Cliente S3
- `R2Config.java` seguir la configuración que indica Cloudflare para el AWS SDK for Java v2: región `auto`, acceso *path-style* y *chunked encoding* desactivado.
- **Actualización del SDK**: desde la versión 2.30.0, el AWS SDK calcula checksums CRC32 por defecto, y R2 no los soporta. Si se actualiza la dependencia `software.amazon.awssdk:s3`, configurar `requestChecksumCalculation` y `responseChecksumValidation` en `WHEN_REQUIRED`.

### Monitoreo
- Se puede monitorear el uso de almacenamiento, ancho de banda y el número de operaciones directamente en el panel de **R2** dentro del Dashboard de Cloudflare para evitar sorpresas en el uso de la capa gratuita.
  - **Operaciones Clase A**: Son operaciones que modifican el estado o listan (ej: escribir, listar o crear objetos).
  - **Operaciones Clase B**: Son operaciones de lectura que no modifican el estado (ej: leer objetos).

### Solución de Problemas Comunes
- **Error 403 (Forbidden)**: Verificar que el token tenga permisos `Object Read & Write` y acceso al bucket que estás utilizando. Si el error es de firma (*SignatureDoesNotMatch*), verificar que el *chunked encoding* siga desactivado en `R2Config.java`.
- **Error 404 (NoSuchKeyException)**: Si al intentar descargar un archivo da este error, revisar que el nombre del archivo (key) en R2 coincida exactamente con el que se intenta acceder, teniendo en cuenta la codificación de caracteres especiales.

## 4. Detalles del Plan Gratuito (Free Tier)

Cloudflare R2 ofrece una capa gratuita generosa ideal para proyectos en desarrollo o de pequeña escala. Aplica solo a la clase de almacenamiento **Standard**. Los límites actuales son:

*   **Almacenamiento**: 10 GB-mes por mes.
*   **Operaciones Clase A**: 1 millón por mes (escrituras, listados, creación de buckets).
*   **Operaciones Clase B**: 10 millones por mes (lecturas).
*   **Egreso** (transferencia de salida): gratuito.

Si se superan estos límites, Cloudflare aplicará cargos según su tabla de precios vigente. Se recomienda monitorear el uso desde el dashboard de R2 para evitar costos no esperados.
