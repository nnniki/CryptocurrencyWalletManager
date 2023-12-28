package bg.sofia.uni.fmi.mjt.cryptowallet.user;

import bg.sofia.uni.fmi.mjt.cryptowallet.coinapi.dto.CryptoInformation;

public record BoughtCryptocurrency(CryptoInformation boughtCrypto, double buyingPrice, double buyingCount) { }
