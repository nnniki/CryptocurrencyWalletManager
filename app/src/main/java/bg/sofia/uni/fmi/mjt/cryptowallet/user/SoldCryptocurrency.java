package bg.sofia.uni.fmi.mjt.cryptowallet.user;

import bg.sofia.uni.fmi.mjt.cryptowallet.coinapi.dto.CryptoInformation;

public record SoldCryptocurrency(CryptoInformation soldCrypto, double sellingPrice, double profit) { }
