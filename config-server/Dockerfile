# Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . /workspace
RUN mvn -q -DskipTests -pl config-server -am package spring-boot:repackage

# Run
FROM eclipse-temurin:17-jre
ENV JAVA_OPTS=""
WORKDIR /app
COPY --from=build /workspace/config-server/target/*.jar /app/app.jar
# use native backend (reads from mounted /config-repo)
ENV SPRING_PROFILES_ACTIVE=native
ENV SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS=file:/config-repo
EXPOSE 8888
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
