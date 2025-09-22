FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . /workspace
RUN mvn -q -DskipTests -pl api-gateway -am package spring-boot:repackage

FROM eclipse-temurin:17-jre
ENV JAVA_OPTS=""
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
