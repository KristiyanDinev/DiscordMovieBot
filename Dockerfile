FROM alpine/java:21-jre

ARG BOT_VERSION=1.0.0
ARG JAR_NAME=DiscordMovieBotShadowJar-${BOT_VERSION}.jar

WORKDIR /app

# Default Java options - includes the "elastic" flags we discussed
ENV JAVA_OPTS=""

COPY build/libs/${JAR_NAME} /app/DiscordMovieBot.jar

RUN adduser -D -u 1000 moviebot && chown -R moviebot:moviebot /app
USER moviebot

# We use 'sh -c' to ensure the JAVA_OPTS variable is parsed correctly
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/DiscordMovieBot.jar"]