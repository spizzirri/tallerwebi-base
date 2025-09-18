Proyecto base Taller Web I (Maven and Thymeleaf)
===============================

## Preparación del ambiente de desarrollo
Antes de comenzar a trabajar con el proyecto, debemos instalar y configurar algunas herramientas:

### Java
Java es el lenguaje de programación con el que trabajaremos. El proyecto esta configurado para funcionar con la version 11 de Java.
* Descargar el JDK para el sistema operativo en uso: [JDK 11 para Linux](https://download.java.net/openjdk/jdk11.0.0.2/ri/openjdk-11.0.0.2_linux-x64.tar.gz), [JDK 11 para Windows](https://download.java.net/openjdk/jdk11.0.0.2/ri/openjdk-11.0.0.2_windows-x64.zip).
* Descomprimir el archivo descargado en una carpeta. 
    * Ejemplo: `/home/java` (En Linux) o `C:\java` (en Windows).
* Configurar una variable de entorno con la clave `JAVA_HOME` indicando en el valor, la ruta donde se descomprimió el archivo descargado. 
    * Ejemplo: `C:\java\jdk-11.0.0.2` (en Windows) o `/home/java/jdk-11.0.0.2` (en Linux).
* Configurar la variable de entorno `PATH` incluyendo la variable `JAVA_HOME`: 
    * Agregamos: `%JAVA_HOME%\bin` al listado existente.
* Luego de guardar la configuración de las variables de entorno, ejecutamos en el CMD o Terminal `java -version` y luego `javac -version`, debiendo visualizar como salida la versión de Java y la versión del compilador instaladas, respectivamente.
* [Guía para instalar Java en Windows](https://www.java.com/es/download/help/windows_manual_download.html)
* [Guía para instalar Java en Linux](https://www.java.com/es/download/help/linux_x64_install.html)

### Maven
Maven es una herramienta que permite la gestión de proyectos (principalmente proyectos Java). Simplifica y estandariza el proceso de construcción del producto software. 
* Es requisito tener instalado Java (mínimo Java 8 para Maven, Java 11 para el proyecto) y configuradas las variables de entorno (`JAVA_HOME` y `PATH`).
* Descargar Maven desde el [sitio oficial](https://dlcdn.apache.org/maven/maven-3/3.8.9/binaries/apache-maven-3.8.9-bin.zip) y luego descomprimirlo en una carpeta (Puede estar junto a la instalación de Java o en otra carpeta).
* Configurar una variable de entorno con la clave `MAVEN_HOME` indicando en el valor, la ruta donde se descomprimió el archivo descargado. 
    * Ejemplo: `/home/maven/apache-maven-3.9.11` (en Linux) o `C:\maven\apache-maven-3.9.11` (en Windows).
* Configurar la variable de entorno `PATH` incluyendo la variable `MAVEN_HOME`: 
    * Agregamos: `%MAVEN_HOME%\bin` al listado existente.
* Luego de guardar la configuración de las variables de entorno, ejecutamos en el CMD o Terminal `mvn -version`, debiendo visualizar como salida la versión de Maven descargada. Si el Terminal o CMD estaba abierto durante la configuración, hay que cerrarlo y abrir nuevamente.
* Carpeta **.m2**: Es el repositorio local de Maven donde guarda los artefactos (como archivos JAR), descargados por dependencias o generados localmente. Por defecto esta carpeta se crea en las siguientes rutas:
    * Linux: `/home/<mi usuario>/.m2`.
    * Windows: `C:\Users\<mi usuario>\.m2`.
* [Guía para instalar Maven en Windows o Linux](https://maven.apache.org/install.html).
 
#### Configuración en IDEs
* IntelliJ: Maven viene instalado y el plugin está accesible en el panel de la derecha (aparece con una letra **M**). Permite ejecutar comandos, gestionar plugins y dependencias. 
* VS Code (Plugins recomendados):
    * Maven for Java: Es el plugin oficial. Desde el explorador, como nueva sección debajo del proyecto aparece Maven, facilitando la ejecución de comandos, gestión de dependencias y plugins.
    * XML: Mejora el autocompletado y la validación de sintáxis en archivos XML. Para Maven es crucial el archivo `pom.xml`,

### Docker
Docker es una plataforma de contenedores que permite empaquetar aplicaciones con todas sus dependencias en contenedores ligeros y portables. Los contenedores se ejecutan de forma aislada y consistente en cualquier entorno que tenga Docker instalado.
* Para instalar sobre Windows, el camino mas simple es instalar `Docker Desktop`. Para esto es necesario seguir esta [guía](https://docs.docker.com/desktop/setup/install/windows-install/).
* Para instalar sobre Linux, alcanza con instalar `Docker Engine` siguiendo esta [guía](https://docs.docker.com/engine/install/ubuntu/). También es posible instalar `Docker Desktop` (incluye Docker Engine).

## 1. ¿Cómo iniciar el proyecto?
> Necesitamos previamente una base de datos mysql en el puerto 3306.
```shell
# Levantamos un BBDD con docker
docker build -f DockerfileSQL -t mysql .
docker run --env-file .env --name mysql-container -d -p 3306:3306 mysql

# Iniciamos el proyecto
$ mvn clean jetty:run
# http://localhost:8080/spring
```
## 2. Thymeleaf
* [Documentación](https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html)

## 3. Hamcrest
* [Documentación](https://hamcrest.org/JavaHamcrest/javadoc/2.2/)

## 4. GitHub Actions
* [Documentación](https://docs.github.com/es/actions/quickstart)

## 5. Playwright
* [Documentación](https://playwright.dev/java/docs/intro)

## 6. Jetty
* [Documentación](https://eclipse.dev/jetty/documentation/jetty-9/index.html#maven-and-jetty)

## 7. ¿Cómo correr las pruebas de punta a punta?

### Iniciar el servidor
```shell
# Opción 1
$ mvn clean jetty:run

# Opción 2 -- ver seccion 10 docker-compose
$ docker-compose up --build
```
### Correr las pruebas en otra terminal
```shell
$ mvn test -Dtest="VistaLoginE2E"
$ mvn test -Dtest="VistaLoginE2E#deberiaNavegarAlHomeSiElUsuarioExiste"
```

## 8. ¿Cómo correr las pruebas unitarias de javascript?
```shell
$ cd src/main/webapp/resources/core/js
# Si es la primera vez debo descargar e instalar las dependencias
$ npm install
# Ejecuto las pruebas
$ npm run test
```

## 9. Docker:
Los archivos de docker de este proyecto estan preparados para desplegar un archivo WAR usando el servido Jetty o Tomcat.
El archivo de docker para Jetty y Tomcat esperan que el archivo WAR se debe llamar "tallerwebi-base-1.0-SNAPSHOT" para eso debemos modificar los atributos <artifactId> y <version> del archivo pom.xml. 

Para generar un archivo WAR debemos ejecutar maven.
```shell
mvn clean package
```

Una vez que tenemos el archivo WAR, debemos generar la imagen de docker.
```shell
docker build -f DockerfileJetty -t tallerwebi .
docker build -f DockerfileTomcat -t tallerwebi .
```

Una vez que tenemos la imagen generada, podemos instanciar un contenedor y ejecutarlo.
```shell
docker run -p 8080:8080 tallerwebi
```

### 9. Comandos básicos:
```shell
# Crear una imagen con el nombre "tallerwebi".
docker build -f DockerfileJetty -t tallerwebi .

# Instancia y ejecuta un contendor en base a la imagen "tallerwebi". 
docker run -p 8080:8080 tallerwebi 

# Ejecuta un contendor ya instanciado.
docker start <containerId> 

# Instancia un contendor en base a la imagen tallerwebi para ejecutar bash.
docker run -it --entrypoint /bin/bash tallerwebi

# Muestra los logs.
docker logs <containerId>

# Muestra todos los contenedores corriendo.
docker ps

# Muestra todos los contenedores creados (o existentes).
docker ps -a 

# Muestra todas las imágenes creadas.
docker images

# Elimina un contenedor.
docker rm <containerId>

# Elimina una imagen.
docker rmi <imageId>

# Crear una imagen con el nombre "mysql".
docker build -f DockerfileSQL -t mysql .

# Instancia un contendor en base a la imagen mysql.
docker run --env-file .env --name mysql-container -d -p 3306:3306 mysql # sudo apt install mysql-client
```

## 10. docker-compose
Docker Compose es una herramienta que permite definir y ejecutar aplicaciones multi-contenedor usando archivos YAML. Simplifica la gestión de múltiples servicios y sus dependencias, permitiendo orquestar todo el stack de la aplicación con un solo comando.

```shell
mvn clean package
# Invoco a docker-compose para que me genere contenedores de todos los servicios especificadas
docker-compose up --build

# Invoco a docker para que elimine los contenedores creados 
docker-compose down
```
## 11. Comandos de Maven
Para ejecutar comandos de Maven, ya sea en el terminal integrado al IDE, o en otro terminal como el de Linux o Windows (CMD), se debe utilizar el comando principal `mvn` seguido del comando o fase del ciclo de vida a ejecutar. Ejemplo: `mvn clean`.

> Maven ejecuta todas las fases anteriores a la fase del ciclo de vida indicada.

### Comandos:
```shell
# Es un comando. Limpia el directorio target (contiene JARs o WARs generados) de la compilación anterior 
mvn clean

# -> Fases en orden de ejecución <-

# Valida que el proyecto sea correcto y que toda la información necesaria este disponible
mvn validate

# Compila el código fuente del proyecto (tambien descarga dependencias)
mvn compile

# Ejecuta las pruebas unitarias
mvn test

# Empaqueta el código compilado en un archivo JAR o WAR (tambien descarga dependencias)
mvn package

# Verifica que el paquete es válido y cumple los criterios de calidad
mvn verify

# Instala el paquete en el repositorio local de Maven
mvn install

# Sube el artefacto a un repositorio remoto (que se debe definir en el archivo pom.xml) para que pueda distribuirse a otros equipos, desarrolladores, o bien, para desplegar (fin de la etapa de construcción)
mvn deploy

# El comando 'clean' es combinable con las fases 
mvn clean package

# Es el comando quizas mas ejecutado. Las dependencias se descargan al ejecutar un comando de construcción como 'compile' o 'package' ('install' las incluye, tambien las validaciones y la ejecución de las pruebas). 
mvn clean install

```

## Tecnologías:
* Docker
* Java 11
* Spring 5.2.22.RELEASE
* Thymeleaf 3.0.15.RELEASE
* Embedded Jetty Server 9.4.45.v20220203
* Servlet API 4.0.4
* Bootstrap 5.2.0 (webjars)
* IntelliJ IDEA | VS Code
* Maven 3.8.6
* Spring Test 5.2.22.RELEASE
* Hamcrest 2.2
* JUnit 5.9
* Hibernate 5.4.24.Final
* Mockito 5.3.1
* Playwright 1.36.0
* Node 18.16.1 o superior <- Instalación manual desde la [página de node](https://nodejs.org/en) 
* npm --> npm install -g npm

*_Proyecto modificado en base a: [Spring MVC hello world example (Maven and Thymeleaf)](https://mkyong.com/spring-mvc/spring-mvc-hello-world-example/) _*