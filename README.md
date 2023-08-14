Proyecto base Taller Web I (Maven and Thymeleaf)
===============================

## 1. Como iniciar el proyecto
```shell
$ mvn clean jetty:run
# http://localhost:8080/spring
```
## 2. Thymeleaf
* [Documentacion](https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html)

## 3. GitHub Actions
* [Documentacion](https://docs.github.com/es/actions/quickstart)

## 4. Playwright
* [Documentacion](https://playwright.dev/java/docs/intro)

## 5. Como correr las pruebas de punta a punta

### Iniciar el servidor
```shell
$ mvn clean jetty:run
```
### Correr las pruebas en otra terminal
```shell
$ mvn test -Dtest="VistaLoginE2E"
```

## 6. Tecnologías:
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

*_Proyecto modificado en base a: [Spring MVC hello world example (Maven and Thymeleaf)](https://mkyong.com/spring-mvc/spring-mvc-hello-world-example/) _*