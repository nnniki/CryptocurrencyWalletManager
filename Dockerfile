# build stage 
FROM gradle:8.5.0-jdk17 as builder
WORKDIR /home/gradle/src
ADD --chown=gradle:gradle . /home/gradle/src
RUN chmod a+x ./app/gradlew
RUN ./app/gradlew build

# final stage
FROM openjdk:17-ea-17-oracle
RUN mkdir /home/gradle/src
COPY --from=builder /home/gradle/src/app/build/libs/*.jar /home/gradle/src/CryptoCurrencyWalletManager.jar
ENTRYPOINT ["java", "-cp", "/home/gradle/src/CryptoCurrencyWalletManager.jar", "bg.sofia.uni.fmi.mjt.cryptowallet.CryptoServer"]
