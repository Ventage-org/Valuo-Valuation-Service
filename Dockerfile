FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -q dependency:go-offline
COPY src src
RUN ./mvnw -q package -DskipTests

FROM eclipse-temurin:21-jre
RUN useradd --system --create-home valuo
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
USER valuo
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
