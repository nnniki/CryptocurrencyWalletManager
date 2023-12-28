package bg.sofia.uni.fmi.mjt.cryptowallet.coinapi;

import bg.sofia.uni.fmi.mjt.cryptowallet.coinapi.dto.Cryptocurrencies;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.BadRequestToRestApiException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.TooManyRequestsException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.UnauthorizedException;
import bg.sofia.uni.fmi.mjt.cryptowallet.log.Log;

import java.net.http.HttpClient;
import java.util.concurrent.Callable;

public class CoinApiThread implements Callable<Cryptocurrencies> {
    private final Log log = new Log();

    @Override
    public Cryptocurrencies call() {

        HttpClient client = HttpClient.newBuilder().build();
        CoinApiData data = new CoinApiData(client);

        Cryptocurrencies crypto;
        try {
            crypto = data.getCryptocurrenciesInfo();
            return crypto;
        } catch (BadRequestToRestApiException | TooManyRequestsException | UnauthorizedException e) {
            log.saveServerException(e);
            throw new RuntimeException(e);
        }
    }
}
