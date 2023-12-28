package bg.sofia.uni.fmi.mjt.cryptowallet.coinapi.dto;

import com.google.gson.annotations.SerializedName;

public record CryptoInformation(@SerializedName("asset_id") String assetID, @SerializedName("name") String assetName,
                                @SerializedName("type_is_crypto") int isCrypto,
                                @SerializedName("price_usd") double price) { }
