Spring MVC hello world example (Maven and Thymeleaf)
===============================

This is the source code for the article - [Spring MVC hello world example (Maven and Thymeleaf)](https://mkyong.com/spring-mvc/spring-mvc-hello-world-example/).

_P.S This tutorial is NOT a Spring Boot application, just pure Spring Web MVC!_

## 1. Technologies and tools used:
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

## 2. How to run this project?
```shell
$ git clone https://github.com/mkyong/spring-mvc/

$ cd spring-mvc-hello-world

$ mvn clean jetty:run

# visit http://localhost:8080/spring

# visit http://localhost:8080/spring/hello/mkyong
```