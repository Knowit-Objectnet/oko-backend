FROM gradle:6.5.0-jdk8 as builder 

WORKDIR /tmp/calendar

COPY build.gradle.kts .
COPY gradle.properties .
COPY settings.gradle.kts .
COPY resources resources
COPY src src

RUN gradle build 


FROM openjdk:8-jre-alpine

WORKDIR /app

ENV APPLICATION_USER ktor
ENV CALENDAR_DB_MIGRATIONS_LOCATION filesystem:/app/resources/db/migrations

RUN adduser -D -g '' $APPLICATION_USER

COPY --from=builder /tmp/calendar/build/libs/calendar-all.jar /app/calendar-all.jar
COPY resources resources

RUN chown -R $APPLICATION_USER .

USER $APPLICATION_USER

EXPOSE 8080

ENTRYPOINT ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "calendar-all.jar"]
