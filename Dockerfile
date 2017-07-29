FROM openjdk:8

COPY . /opt/airbot
WORKDIR /opt/airbot
EXPOSE 8080
CMD ["./gradlew", "run", "--no-daemon"]