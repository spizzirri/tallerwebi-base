Proyecto base Taller Web I (Maven and Thymeleaf)
===============================

## 1. Como iniciar el proyecto
```shell
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
$ mvn clean jetty:run
```
### Correr las pruebas en otra terminal
```shell
$ mvn test -Dtest="VistaLoginE2E"
```

## 8. Como correr las pruebas unitarias de javascript
```shell
$ cd src/main/webapp/resources/core/js
// Si es la primera vez debo descargar las dependencias
$ npm install
// Ejecuto las pruebas
$ npm test
```

## 9. Tecnologías:
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

*_Proyecto modificado en base a: [Spring MVC hello world example (Maven and Thymeleaf)](https://mkyong.com/spring-mvc/spring-mvc-hello-world-example/) _*