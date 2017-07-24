FROM openjdk:8 AS BUILD_IMAGE

COPY . /airbot

WORKDIR /airbot
RUN ./gradlew shadowJar

FROM vertx/vertx3

ENV VERTICLE_HOME /usr/local/airbot
ENV VERTICLE_NAME com.github.ithildir.airbot.AirBotVerticle

COPY --from=BUILD_IMAGE /airbot/libs/airbot-all.jar $VERTICLE_HOME

WORKDIR $VERTICLE_HOME

EXPOSE 8080

CMD ["vertx run $VERTICLE_NAME -cp $VERTICLE_HOME/*"]