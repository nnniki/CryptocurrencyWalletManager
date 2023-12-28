package bg.sofia.uni.fmi.mjt.cryptowallet.log;

import bg.sofia.uni.fmi.mjt.cryptowallet.coinapi.CoinApiThread;
import bg.sofia.uni.fmi.mjt.cryptowallet.coinapi.dto.Cryptocurrencies;
import bg.sofia.uni.fmi.mjt.cryptowallet.user.User;
import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.Callable;

public class DataSaver {

    private static final Gson GSON = new Gson();
    private static final String CRYPTO_INFO_FILE = "CryptoInformation.txt";
    private static final String USERS_INFO_FILE = "UsersInformation.txt";
    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final int VALID_MINUTES = 30;
    private final Log log = new Log();

    public void saveUsersInfoToFile(Set<User> registeredUsers) throws IOException {
        try (Writer writer = new FileWriter(USERS_INFO_FILE, false)) {
            GSON.toJson(registeredUsers, writer);
        }
    }

    public void saveCryptocurrenciesToFile(Cryptocurrencies cryptocurrencies) throws IOException {
        try (Writer writer = new FileWriter(CRYPTO_INFO_FILE)) {
            GSON.toJson(cryptocurrencies, writer);
        }
    }

    public boolean checkCryptocurrenciesForUpdate(Cryptocurrencies cryptocurrencies) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIME_FORMAT);
        LocalDateTime dateTime = LocalDateTime.parse(cryptocurrencies.lastUpdateOfInformation(), formatter);

        LocalDateTime validUntilTime = dateTime.plusMinutes(VALID_MINUTES);

        return validUntilTime.isBefore(LocalDateTime.now());
    }

    public Cryptocurrencies getCryptocurrenciesFromApi() {
        Cryptocurrencies cryptocurrencies;
        Callable<Cryptocurrencies> callable = new CoinApiThread();
        try {
            cryptocurrencies = callable.call();
            saveCryptocurrenciesToFile(cryptocurrencies);

            return cryptocurrencies;
        } catch (Exception e) {
            log.saveServerException(e);
            throw new RuntimeException(e);
        }
    }
}
