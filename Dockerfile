# Usa una imagen base de Java
FROM openjdk:17-slim

#Manteiner info
LABEL maintainer="Enrique <ebgvdeveloper@gmail.com>"

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /storage

# Copia el archivo JAR de tu proyecto al directorio /app en el contenedor
COPY target/storage-server-0.0.1-SNAPSHOT.jar /storage/storage-server.jar

# Expone el puerto
EXPOSE 8095

#execute the application
ENTRYPOINT ["java","-jar","storage-server.jar"]