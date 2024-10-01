FROM gradle:8.10.2 AS build
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project
RUN gradle :solve-lpaas:shadowJar

FROM openjdk:11.0.16-jre-slim
ENV APP_HOME=/app
COPY --from=build /home/gradle/project/solve-lpaas/build/libs/2p-solve-lpaas*redist.jar $APP_HOME/
WORKDIR $APP_HOME
EXPOSE 8080
ENTRYPOINT exec java -jar 2p-solve-lpaas*redist.jar