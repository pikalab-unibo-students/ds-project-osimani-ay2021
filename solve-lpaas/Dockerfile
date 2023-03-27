# Alpine Linux , last versiom
FROM alpine:latest
# Install OpenJDK 11
RUN apk update; apk add openjdk11
# Copies everything from ’my - app/’ (on the host ) to ’/my - app /’
# on the container file system
COPY .. /lpaas/
# Moves to the /my - app / directory on the container file system
WORKDIR /lpaas/
EXPOSE 8080
# Compiles the to -be - deployed app
RUN ./gradlew build
# Sets some env . var. on the container
# Run the application by default
CMD ./gradlew run --console=plain