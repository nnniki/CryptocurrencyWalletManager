# build stage 
FROM gradle:8.5.0-jdk17 as builder
WORKDIR /app
ADD --chown=gradle:gradle /* /app
RUN chmod a+x ./gradlew
RUN ./gradlew build

# final stage
FROM openjdk:17-ea-17-oracle
RUN mkdir /app
COPY --from=builder /app/build/libs/*.jar /app/CryptoCurrencyWalletManager.jar
ENTRYPOINT ["java", "-cp", "/app/CryptoCurrencyWalletManager.jar", "bg.sofia.uni.fmi.mjt.cryptowallet.CryptoServer"]
