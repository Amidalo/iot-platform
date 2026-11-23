# ---- Стадия сборки ----
FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Стадия запуска ----
FROM amazoncorretto:21-alpine
WORKDIR /app
ENV GATEWAY_BIND_ADDRESS=0.0.0.0
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
