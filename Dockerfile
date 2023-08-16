FROM eclipse-temurin:17-jre
COPY . .
RUN chmod +x gradlew
RUN ./gradlew build -DexcludeContainerTests=true
EXPOSE 8000
CMD ["./gradlew", "app:run"]
# TODO - research how to decrease full image size, as right now it's huge 500 MB (base image size only 90 MB)
# probably because of the gradle wrapper and build ecosystem, but I'm not familiar enough with it yet to quickle solve