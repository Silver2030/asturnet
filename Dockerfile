# --- Stage 1: Build the application ---
# Usamos una imagen que incluye el JDK (Java Development Kit) para compilar el código.
# eclipse-temurin es una buena opción, ligera y mantenida.
FROM eclipse-temurin:17-jdk-jammy AS build

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos los archivos de Maven Wrapper y el pom.xml primero.
# Esto es una optimización clave para el cache de Docker:
# Si solo cambia el código fuente y no las dependencias en pom.xml,
# esta capa no se reconstruirá, acelerando las builds.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Descargamos las dependencias de Maven.
# El comando 'dependency:go-offline' descarga todas las dependencias
# para que el paso de 'mvn clean install' sea más rápido y no dependa de la red.
RUN ./mvnw dependency:go-offline

# Copiamos el resto del código fuente
COPY src src

# Construimos la aplicación. -DskipTests para saltar los tests durante la build de Docker,
# ya que normalmente se ejecutan en CI/CD antes de la imagen Docker.
RUN ./mvnw clean install -DskipTests

# --- Stage 2: Create the final production image ---
# Usamos una imagen más ligera que solo incluye el JRE (Java Runtime Environment)
# para la ejecución, ya que no necesitamos las herramientas de compilación en producción.
FROM eclipse-temurin:17-jre-jammy

# Establecemos el directorio de trabajo final
WORKDIR /app

# Copiamos el archivo JAR ejecutable desde la etapa 'build' al nuevo contenedor.
# El nombre de tu JAR será algo como 'asturnet-0.0.1-SNAPSHOT.jar',
# el wildcard '*' lo selecciona automáticamente.
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto en el que la aplicación Spring Boot escuchará.
# Por defecto, Spring Boot usa el puerto 8080. Render lo detectará automáticamente.
EXPOSE 8080

# Define el comando para ejecutar la aplicación cuando el contenedor se inicie.
# '-jar app.jar' le dice a Java que ejecute el JAR.
ENTRYPOINT ["java", "-jar", "app.jar"]