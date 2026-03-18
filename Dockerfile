FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Install curl + unzip (for Gradle distribution)
RUN apt-get update && apt-get install -y curl unzip && rm -rf /var/lib/apt/lists/*

ARG GRADLE_VERSION=9.0.0
RUN curl -fsSL https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o /tmp/gradle.zip \
    && unzip -q /tmp/gradle.zip -d /opt \
    && ln -s /opt/gradle-${GRADLE_VERSION}/bin/gradle /usr/local/bin/gradle \
    && rm -f /tmp/gradle.zip

COPY . .

RUN gradle --no-daemon clean bootJar

FROM eclipse-temurin:25-jdk
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=build /app/build/libs/*.jar /app/app.jar
RUN mkdir -p /app/storage
EXPOSE 8888
ENTRYPOINT ["bash","-lc","java $JAVA_OPTS -jar /app/app.jar"]
