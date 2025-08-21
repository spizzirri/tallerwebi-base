Proyecto base Taller Web I (Maven and Thymeleaf)
===============================

## 1. Como iniciar el proyecto
> Necesitamos previamente una base de datos mysql en el puerto 3306 
```shell
# Levantamos un BBDD con docker
docker build -f DockerfileSQL -t mysql .
docker run --env-file .env --name mysql-container -d -p 3306:3306 mysql

# Iniciamos el proyecto
$ mvn clean jetty:run
# http://localhost:8080/spring
```
## 2. Thymeleaf
* [Documentacion](https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html)

## 3. Hamcrest
* [Documentacion](https://hamcrest.org/JavaHamcrest/javadoc/2.2/)

## 4. GitHub Actions
* [Documentacion](https://docs.github.com/es/actions/quickstart)

## 5. Playwright
* [Documentacion](https://playwright.dev/java/docs/intro)

## 6. Jetty
* [Documentacion](https://eclipse.dev/jetty/documentation/jetty-9/index.html#maven-and-jetty)

## 7. Como correr las pruebas de punta a punta

### Iniciar el servidor
```shell
# Opcion 1
$ mvn clean jetty:run

# Opcion 2 -- ver seccion 10 docker-compose
$ docker-compose up --build
```
### Correr las pruebas en otra terminal
```shell
$ mvn test -Dtest="VistaLoginE2E"
$ mvn test -Dtest="VistaLoginE2E#deberiaNavegarAlHomeSiElUsuarioExiste"
```

## 8. Como correr las pruebas unitarias de javascript
```shell
$ cd src/main/webapp/resources/core/js
# Si es la primera vez debo descargar las dependencias
$ npm install
# Ejecuto las pruebas
$ npm run test
```

## 9. Docker:
Docker es una plataforma de contenedores que permite empaquetar aplicaciones con todas sus dependencias en contenedores ligeros y portables. Los contenedores se ejecutan de forma aislada y consistente en cualquier entorno que tenga Docker instalado.

Los archivos de docker de este proyecto estan preparados para desplegar un archivo WAR usando el servido Jetty o Tomcat
El archivo de docker para Jetty y Tomcat esperan que el archivo WAR se debe llamar "tallerwebi-base-1.0-SNAPSHOT" para eso debemos modificar los atributos <artifactId> y <version> del archivo pom.xml 

Para generar un archivo WAR debemos ejecutar maven
```shell
mvn clean package
```

Una vez que tenemos el archivo WAR, debemos generar la imagen de docker
```shell
docker build -f DockerfileJetty -t tallerwebi .
docker build -f DockerfileTomcat -t tallerwebi .
```

Una vez que tenemos la imagen generada, podemos instanciar un contenedor y ejecutarlo.
```shell
docker run -p 8080:8080 tallerwebi
```

### 9. Comandos básicod:
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

# Muestra todos los contenedores creados.
docker ps -a 

# Muestra todas las imagenes creadas.
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

## Tecnologías:
* Docker
* Java 11
* Spring 5.2.22.RELEASE
* Thymeleaf 3.0.15.RELEASE
* Embedded Jetty Server 9.4.45.v20220203
* Servlet API 4.0.4
* Bootstrap 5.2.0 (webjars)
* IntelliJ IDEA
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