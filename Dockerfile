# build stage 
FROM gradle:8.5.0-jdk17 as builder
WORKDIR /app
ADD --chown=gradle:gradle /app /app
RUN chmod a+x app/gradlew
RUN app/gradlew build

# final stage
FROM openjdk:17-ea-17-oracle
RUN mkdir /app
COPY --from=builder /app/build/libs/*.jar /app/CryptoCurrencyWalletManager.jar
ENTRYPOINT ["java", "-jar", "/app/CryptoCurrencyWalletManager.jar"]
