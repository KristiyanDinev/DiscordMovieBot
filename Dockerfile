FROM alpine/java:21-jre

# Build-time args: set either BOT_VERSION or explicit JAR_NAME
ARG BOT_VERSION=1.0.0
ARG JAR_NAME=DiscordMovieBotShadowJar-${BOT_VERSION}.jar

WORKDIR /app

# Copy only the requested shadow jar into the image and store under a fixed name
COPY build/libs/${JAR_NAME} /app/DiscordMovieBot.jar

# Create a non-root user and use it
RUN adduser -D -u 1000 moviebot && chown -R moviebot:moviebot /app
USER moviebot

ENTRYPOINT ["java", "-jar", "/app/DiscordMovieBot.jar"]
