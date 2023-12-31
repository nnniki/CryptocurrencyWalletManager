# build stage 
FROM gradle:8.5.0-jdk17 as builder
WORKDIR /home
ADD --chown=gradle:gradle . /home
RUN chmod a+x ./app/gradlew
RUN ./app/gradlew build

# final stage
FROM openjdk:17-ea-17-oracle
RUN mkdir /home
COPY --from=builder /home/app/build/libs/*.jar /home/CryptoCurrencyWalletManager.jar
ENTRYPOINT ["java", "-cp", "/home/CryptoCurrencyWalletManager.jar", "bg.sofia.uni.fmi.mjt.cryptowallet.CryptoServer"]
