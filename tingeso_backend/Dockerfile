FROM eclipse-temurin:17-jre-jammy
ARG JAR_FILE=target/tingeso_backend.jar
COPY ${JAR_FILE} tingeso_backend.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/tingeso_backend.jar"]