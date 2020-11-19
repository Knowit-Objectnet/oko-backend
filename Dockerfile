FROM gradle:6.5.0-jdk8 as builder 

WORKDIR /tmp/backend

COPY build.gradle.kts .
COPY gradle.properties .
COPY settings.gradle.kts .
COPY src src

RUN gradle build -x test


FROM openjdk:8-jre-alpine

WORKDIR /app

ENV APPLICATION_USER ktor

RUN adduser -D -g '' $APPLICATION_USER

COPY --from=builder /tmp/backend/build/libs/backend-all.jar /app/backend-all.jar

RUN chown -R $APPLICATION_USER .

USER $APPLICATION_USER

EXPOSE 8080

ENTRYPOINT ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "backend-all.jar"]
