# --- Stage 1: Build the application ---
FROM eclipse-temurin:17-jdk-jammy AS build

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos los archivos de Maven Wrapper y el pom.xml primero.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# ¡¡¡AÑADIR ESTA LÍNEA!!! Da permisos de ejecución al script mvnw
RUN chmod +x mvnw

# Descargamos las dependencias de Maven.
RUN ./mvnw dependency:go-offline

# Copiamos el resto del código fuente
COPY src src

# Construimos la aplicación.
RUN ./mvnw clean install -DskipTests

# --- Stage 2: Create the final production image ---
FROM eclipse-temurin:17-jre-jammy

# Establecemos el directorio de trabajo final
WORKDIR /app

# Copiamos el archivo JAR ejecutable desde la etapa 'build' al nuevo contenedor.
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto en el que la aplicación Spring Boot escuchará.
EXPOSE 8080

# Define el comando para ejecutar la aplicación cuando el contenedor se inicie.
ENTRYPOINT ["java", "-jar", "app.jar"]