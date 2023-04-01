FROM openjdk:11-jre-slim
ENV APP_HOME=/app
COPY /build/libs/2p-solve-lpaas*redist.jar $APP_HOME/
WORKDIR $APP_HOME
EXPOSE 8080
ENTRYPOINT exec java -jar 2p-solve-lpaas*redist.jar