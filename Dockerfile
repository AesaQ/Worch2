FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY wait-for-kk.sh /wait-for-kk.sh
RUN chmod +x /wait-for-kk.sh
RUN apt-get update && apt-get install -y curl

COPY target/worch-0.0.1.jar app.jar

ENTRYPOINT [ "/wait-for-kk.sh", "http://keycloak:8080/realms/Auth", "--", "java", "-jar", "app.jar" ]