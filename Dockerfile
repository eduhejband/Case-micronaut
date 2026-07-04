FROM eclipse-temurin:25-jre

WORKDIR /app

COPY build/libs/*-all.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]