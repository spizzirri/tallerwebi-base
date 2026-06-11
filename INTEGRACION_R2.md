# Integración Cloudflare R2

## 1. Configuración en Cloudflare R2

1.  **Crear un Bucket**:
    *   Iniciar sesión en el [Dashboard de Cloudflare](https://dash.cloudflare.com/).
    *   seleccionarr **R2** en el menú lateral izquierdo.
    *   Hacer clic en **Crear bucket** (*Create bucket*).
    *   Asignar un nombre al bucket (será el `r2.bucketName` en la configuración).
    *   Hacer clic en **Crear bucket**.

2.  **Crear API Token**:
    *   Dentro de la sección **R2**, hacer clic en **Gestionar tokens R2 API** (*Manage R2 API Tokens*).
    *   Hacer clic en **Crear token API** (*Create API token*).
    *   Asignar un nombre descriptivo al token.
    *   En **Permisos**, seleccionar **Object Read & Write**.
    *   En **Acceso a buckets**, seleccionar **Specific bucket access** y elegir el bucket creado anteriormente.
    *   Hacer clic en **Crear token API**.

3.  **Obtener Credenciales**:
    *   copiar los valores de **Access Key ID** (`r2.accessKey`) y **Secret Access Key** (`r2.secretKey`). *Nota: Estos valores no volverán a mostrarse.*
    *   En la pantalla principal de R2, copiar el **S3 API Endpoint** (`r2.endpoint`), que tiene el formato `https://<ACCOUNT_ID>.r2.cloudflarestorage.com`.

## 2. Configuración en la Aplicación

Configurar en un archivo de propiedades (puede ser externo, como: `application.properties`) con los valores obtenidos:

```properties
r2.accessKey=<TU_ACCESS_KEY_ID>
r2.secretKey=<TU_SECRET_ACCESS_KEY>
r2.endpoint=https://<ACCOUNT_ID>.r2.cloudflarestorage.com
r2.bucketName=<NOMBRE_DEL_BUCKET>
```

Para iniciar el servidor con esta configuración, ejecutar:
`mvn jetty:run -Djetty.systemPropertiesFile=/ruta/a/tu/application.properties`

## 3. Consideraciones Adicionales

### Seguridad
- **Protección de Credenciales**: Nunca subas el archivo `application.properties` o cualquier archivo que contenga la `secretKey` a repositorios de código. Asegúrate de que estos archivos estén correctamente incluidos en tu `.gitignore`.
- **Principio de Menor Privilegio**: El token creado solo debe tener acceso al bucket específico del proyecto. Si necesitas cambiar permisos, hazlo exclusivamente desde el dashboard de Cloudflare.

### Límites de Archivo
- **Tamaño de subida**: La aplicación tiene configurado un límite de subida de **50MB** por archivo (definido en `SpringWebConfig.java`). Si requieres subir archivos más grandes, debes ajustar la propiedad `multipartResolver.setMaxUploadSize`.

### Monitoreo
- Se puede monitorear el uso de almacenamiento, ancho de banda y el número de operaciones directamente en el panel de **R2** dentro del Dashboard de Cloudflare para evitar sorpresas en el uso de la capa gratuita.
  - **Operaciones Clase A**: Son operaciones que modifican el estado (ej: escribir, listar o crear objetos).
  - **Operaciones Clase B**: Son operaciones de lectura que no modifican el estado (ej: leer objetos).

### Solución de Problemas Comunes
- **Error 403 (Forbidden)**: Verifica que el token tenga permisos `Object Read & Write` y acceso restringido específicamente al bucket que estás utilizando.
- **Error 404 (NoSuchKeyException)**: Si al intentar descargar un archivo obtienes este error, asegúrate de que el nombre del archivo (key) en R2 coincida exactamente con el que intentas acceder, teniendo en cuenta la codificación de caracteres especiales.

## 4. Detalles del Plan Gratuito (Free Tier)

Cloudflare R2 ofrece una capa gratuita generosa ideal para proyectos en desarrollo o de pequeña escala. Los límites actuales son:

*   **Almacenamiento**: 10 GB por mes.
*   **Operaciones Clase A**: 1 millón por mes (escrituras, listados, creación de buckets).
*   **Operaciones Clase B**: 10 millones por mes (lecturas).

Si se superan estos límites, Cloudflare aplicará cargos según su tabla de precios vigente. Se recomienda monitorear el uso desde el dashboard de R2 para evitar costos no esperados.

