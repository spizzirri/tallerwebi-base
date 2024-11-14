# Usa la imagen base de OpenJDK 11
FROM openjdk:11

# Define una variable para la versión de Jetty
ENV JETTY_VERSION=9.4.56.v20240826
ENV JETTY_HOME=/opt/jetty

# Descarga y extrae Jetty
RUN apt-get update && apt-get install -y wget && \
    mkdir -p $JETTY_HOME && \
    wget https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/${JETTY_VERSION}/jetty-distribution-${JETTY_VERSION}.tar.gz && \
    tar -xzvf jetty-distribution-${JETTY_VERSION}.tar.gz -C $JETTY_HOME --strip-components=1 && \
    rm jetty-distribution-${JETTY_VERSION}.tar.gz

# Copia el archivo WAR al directorio webapps de Jetty
COPY target/tallerwebi-base-1.0-SNAPSHOT.war $JETTY_HOME/webapps/

# Expone el puerto en el que Jetty corre (por defecto 8080)
EXPOSE 8080

# Me paro en el directorio de Jetty
WORKDIR $JETTY_HOME

# Comando para iniciar Jetty y desplegar el archivo WAR
CMD ["java", "-jar", "./start.jar"]
