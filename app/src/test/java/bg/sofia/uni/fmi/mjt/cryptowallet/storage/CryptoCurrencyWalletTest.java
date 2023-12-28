package bg.sofia.uni.fmi.mjt.cryptowallet.storage;

import bg.sofia.uni.fmi.mjt.cryptowallet.coinapi.dto.CryptoInformation;
import bg.sofia.uni.fmi.mjt.cryptowallet.coinapi.dto.Cryptocurrencies;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InsufficientAvailabilityException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InvalidSellingException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.UserAlreadyDefined;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.UserNotFound;
import bg.sofia.uni.fmi.mjt.cryptowallet.log.DataSaver;
import bg.sofia.uni.fmi.mjt.cryptowallet.user.BoughtCryptocurrency;
import bg.sofia.uni.fmi.mjt.cryptowallet.user.SoldCryptocurrency;
import bg.sofia.uni.fmi.mjt.cryptowallet.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CryptoCurrencyWalletTest {

    private SocketChannel channel;
    private CryptoCurrencyWallet cryptoCurrencyWallet;

    @Mock
    DataSaver infoSaver;

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.reset();
        m.update(password.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        return bigInt.toString(16);
    }

    @BeforeEach
    void setUp() throws IOException {
        channel = SocketChannel.open();
        cryptoCurrencyWallet = new CryptoCurrencyWallet(infoSaver);
        CryptoInformation BTC = new CryptoInformation("BTC", "Bitcoin", 1, 20253.00);
        CryptoInformation ETH = new CryptoInformation("ETH", "Ethereum", 1, 10265.00);
        Set<CryptoInformation> crypto = new HashSet<>();
        crypto.add(BTC);
        crypto.add(ETH);
        Cryptocurrencies cryptocurrencies = new Cryptocurrencies(crypto, "2023-09-05 11:00:00");
        cryptoCurrencyWallet.setCryptocurrencies(cryptocurrencies);
    }

    @AfterEach
    void closeChannel() throws IOException {
        channel.close();
    }

    @Test
    void testRegisterUserAlreadyDefined() throws UserAlreadyDefined {
        cryptoCurrencyWallet.register("User", "Pass");

        assertThrows(UserAlreadyDefined.class, () -> cryptoCurrencyWallet.register("User", "Pass"),
                "Error: UserAlreadyDefined exception was expected to be thrown, when trying to register with unavailable username");
    }

    @Test
    void testRegisterUserSuccessfully() throws IOException, NoSuchAlgorithmException, UserAlreadyDefined {
        doNothing().when(infoSaver).saveUsersInfoToFile(any(Set.class));

        User user = new User("Niki", hashPassword("147258"));
        assertEquals(user, cryptoCurrencyWallet.register("Niki","147258"),
                "Error: unexpected result, when registering new user successfully");
    }

    @Test
    void testLoginWithoutRegistration() {
        assertThrows(UserNotFound.class, () -> cryptoCurrencyWallet.login(channel, "Invalid", "AnyPass"),
                "Error: UserNotFound was expected to be thrown, when logging before registration");
    }

    @Test
    void testLoginSuccessfully() throws NoSuchAlgorithmException, UserNotFound, UserAlreadyDefined {
        User user = new User("User", hashPassword("Pass"));

        cryptoCurrencyWallet.register("User", "Pass");
        assertEquals(user, cryptoCurrencyWallet.login(channel, "User", "Pass"),
                "Error: unexpected output, when logging successfully");
    }

    @Test
    void testDepositMoneyWithoutLogin() {
        assertThrows(UserNotFound.class, () -> cryptoCurrencyWallet.depositMoney(channel, 1500),
                "Error: unexpected output, when depositing money without login");
    }

    @Test
    void testDepositMoneySuccessfully() throws UserNotFound, UserAlreadyDefined {
        cryptoCurrencyWallet.register("User", "Pass");
        cryptoCurrencyWallet.login(channel, "User", "Pass");
        cryptoCurrencyWallet.depositMoney(channel, 1500);

        assertDoesNotThrow(() -> UserNotFound.class, "Error: UserNotFound exception was not expected to be thrown");
        assertEquals(1500, cryptoCurrencyWallet.user(channel).money(),
                "Error: unexpected amount of money after deposit");
    }

    @Test
    void testListOfferingsWithoutUpdate() {

        when(infoSaver.checkCryptocurrenciesForUpdate(cryptoCurrencyWallet.getCryptocurrencies())).thenReturn(false);
        StringBuilder builder = new StringBuilder();
        builder.append("ID:ETH Name:Ethereum Price:10265.0  ");
        builder.append("ID:BTC Name:Bitcoin Price:20253.0  ");

        assertEquals(builder.toString(), cryptoCurrencyWallet.listOfferings().toString(),
                "Error: unexpected output, when using list_offerings");
    }

    @Test
    void testListOfferingsUpdateCryptocurrencies() {
        CryptoInformation BTC = new CryptoInformation("BTC", "Bitcoin", 1, 20253.00);
        CryptoInformation ETH = new CryptoInformation("ETH", "Ethereum", 1, 10265.00);
        CryptoInformation TIT = new CryptoInformation("TIT", "Titcoin", 1, 0.3284);
        Set<CryptoInformation> crypto = new HashSet<>();
        crypto.add(BTC);
        crypto.add(ETH);
        crypto.add(TIT);

        Cryptocurrencies cryptocurrencies = new Cryptocurrencies(crypto, "2023-09-05 11:00:00");

        when(infoSaver.checkCryptocurrenciesForUpdate(cryptoCurrencyWallet.getCryptocurrencies())).thenReturn(true);
        when(infoSaver.getCryptocurrenciesFromApi()).thenReturn(cryptocurrencies);
        StringBuilder builder = new StringBuilder();
        builder.append("ID:ETH Name:Ethereum Price:10265.0  ");
        builder.append("ID:TIT Name:Titcoin Price:0.3284  ");
        builder.append("ID:BTC Name:Bitcoin Price:20253.0  ");

        assertEquals(builder.toString(), cryptoCurrencyWallet.listOfferings().toString(),
                "Error: unexpected output, when using list_offerings. Make sure to update them if they are invalid");
    }

    @Test
    void testBuyUserNotFound() throws UserAlreadyDefined {

        cryptoCurrencyWallet.register("User", "Pass");
        assertThrows(UserNotFound.class, () -> cryptoCurrencyWallet.buy(channel, "BTC", 1250),
                "Error: UserNotFoundException was expected, when user is not logged");
    }

    @Test
    void testBuyInsufficientAvailability() throws UserAlreadyDefined, UserNotFound {

        cryptoCurrencyWallet.register("User", "Pass");
        cryptoCurrencyWallet.login(channel, "User", "Pass");
        cryptoCurrencyWallet.depositMoney(channel, 1500);

        assertThrows(InsufficientAvailabilityException.class, () -> cryptoCurrencyWallet.buy(channel, "BTC",
                        1750),"Error: InsufficientAvailabilityException was expected, when" +
                " user invests more money than available");
    }

    @Test
    void testBuyMissingCryptocurrency() throws UserAlreadyDefined, UserNotFound {

        cryptoCurrencyWallet.register("User", "Pass");
        cryptoCurrencyWallet.login(channel, "User", "Pass");
        cryptoCurrencyWallet.depositMoney(channel, 1500);

        assertThrows(IllegalArgumentException.class, () -> cryptoCurrencyWallet.buy(channel, "TTT",
                1250),"Error: IllegalArgumentException was expected, when" +
                "wanted cryptocurrency is not available");
    }

    @Test
    void testBuyCryptocurrencySuccessfully() throws UserAlreadyDefined, UserNotFound, InsufficientAvailabilityException {

        cryptoCurrencyWallet.register("User", "Pass");
        cryptoCurrencyWallet.login(channel, "User", "Pass");
        cryptoCurrencyWallet.depositMoney(channel, 1500);

        CryptoInformation BTC = new CryptoInformation("BTC", "Bitcoin", 1, 20253.00);
        BoughtCryptocurrency expectedBuying = new BoughtCryptocurrency(BTC, 1250, 0.0617);
        BoughtCryptocurrency actualBuying = cryptoCurrencyWallet.buy(channel, "BTC", 1250);

        assertEquals(expectedBuying.buyingCount(), actualBuying.buyingCount(), 4,
                "Error: unexpected count of bought cryptocurrency");
        assertEquals(expectedBuying.buyingPrice(), actualBuying.buyingPrice(),
                "Error: unexpected price of bought cryptocurrency");
        assertEquals(expectedBuying.boughtCrypto(), actualBuying.boughtCrypto(),
                "Error: unexpected data about the bought cryptocurrency");
    }

    @Test
    void testBuySameCryptocurrencyTwice() throws UserAlreadyDefined, UserNotFound, InsufficientAvailabilityException {

        cryptoCurrencyWallet.register("User", "Pass");
        cryptoCurrencyWallet.login(channel, "User", "Pass");
        cryptoCurrencyWallet.depositMoney(channel, 5000);

        CryptoInformation BTC = new CryptoInformation("BTC", "Bitcoin", 1, 20253.00);
        BoughtCryptocurrency expectedBuying = new BoughtCryptocurrency(BTC, 2500, 0.1234);
        cryptoCurrencyWallet.buy(channel, "BTC", 1250);
        cryptoCurrencyWallet.buy(channel, "BTC", 1250);

        BoughtCryptocurrency actualBuying = cryptoCurrencyWallet.user(channel).boughtCryptocurrencies().iterator().next();

        assertEquals(expectedBuying.buyingCount(), actualBuying.buyingCount(), 4,
                "Error: unexpected count of bought cryptocurrency");
        assertEquals(expectedBuying.buyingPrice(), actualBuying.buyingPrice(),
                "Error: unexpected price of bought cryptocurrency");
        assertEquals(expectedBuying.boughtCrypto(), actualBuying.boughtCrypto(),
                "Error: unexpected data about the bought cryptocurrency");
    }

    @Test
    void testSellUserNotFound() throws UserAlreadyDefined {

        cryptoCurrencyWallet.register("User", "Pass");
        assertThrows(UserNotFound.class, () -> cryptoCurrencyWallet.sell(channel, "BTC"),
                "Error: UserNotFound Exception was expected, when user is not logged");
    }

    @Test
    void testSellMissingCryptocurrency() throws UserAlreadyDefined, UserNotFound {

        cryptoCurrencyWallet.register("User", "Pass");
        cryptoCurrencyWallet.login(channel, "User", "Pass");
        cryptoCurrencyWallet.depositMoney(channel, 1500);

        assertThrows(IllegalArgumentException.class, () -> cryptoCurrencyWallet.sell(channel, "TTT"),
                "Error: IllegalArgumentException was expected, when wanted cryptocurrency is not available");
    }

    @Test
    void testSellCryptoBeforeBuyingIt() throws UserAlreadyDefined, UserNotFound {

        cryptoCurrencyWallet.register("User", "Pass");
        cryptoCurrencyWallet.login(channel, "User", "Pass");

        assertThrows(InvalidSellingException.class, () -> cryptoCurrencyWallet.sell(channel, "BTC"),
                "Error: InvalidSellingException was expected when, selling cryptocurrency before buying it");
    }

    @Test
    void testSellSamePriceAsBuying() throws UserAlreadyDefined, UserNotFound, InsufficientAvailabilityException,
            InvalidSellingException {

        cryptoCurrencyWallet.register("User", "Pass");
        cryptoCurrencyWallet.login(channel, "User", "Pass");
        cryptoCurrencyWallet.depositMoney(channel, 1500);

        CryptoInformation BTC = new CryptoInformation("BTC", "Bitcoin", 1, 20253.00);
        cryptoCurrencyWallet.buy(channel, "BTC", 1250);

        when(infoSaver.checkCryptocurrenciesForUpdate(cryptoCurrencyWallet.getCryptocurrencies())).thenReturn(false);
        SoldCryptocurrency actualSoldCryptocurrency = cryptoCurrencyWallet.sell(channel, "BTC");
        SoldCryptocurrency expectedSoldCrypto = new SoldCryptocurrency(BTC, 1250.0, 0.0);

        assertEquals(expectedSoldCrypto, actualSoldCryptocurrency,
                "Error: unexpected output after selling cryptocurrency");
        assertTrue(cryptoCurrencyWallet.user(channel).boughtCryptocurrencies().isEmpty(),
                "Error: sold cryptocurrency must be removed from bought");
    }

    @Test
    void testSellCryptoUpdatedPrice() throws UserAlreadyDefined, UserNotFound, InsufficientAvailabilityException,
            InvalidSellingException {

        cryptoCurrencyWallet.register("User", "Pass");
        cryptoCurrencyWallet.login(channel, "User", "Pass");
        cryptoCurrencyWallet.depositMoney(channel, 1500);

        cryptoCurrencyWallet.buy(channel, "BTC", 1000);
        CryptoInformation BTC = new CryptoInformation("BTC", "Bitcoin", 1, 20500.00);
        CryptoInformation ETH = new CryptoInformation("ETH", "Ethereum", 1, 10265.00);
        CryptoInformation TIT = new CryptoInformation("TIT", "Titcoin", 1, 0.3284);
        Set<CryptoInformation> crypto = new HashSet<>();
        crypto.add(BTC);
        crypto.add(ETH);
        crypto.add(TIT);

        Cryptocurrencies cryptocurrencies = new Cryptocurrencies(crypto, "2023-09-05 11:00:00");

        when(infoSaver.checkCryptocurrenciesForUpdate(cryptoCurrencyWallet.getCryptocurrencies())).thenReturn(true);
        when(infoSaver.getCryptocurrenciesFromApi()).thenReturn(cryptocurrencies);

        SoldCryptocurrency expectedSoldCryptocurrency = new SoldCryptocurrency(BTC, 1012.195, 12.195);
        SoldCryptocurrency actualSoldCryptocurrency = cryptoCurrencyWallet.sell(channel, "BTC");

        assertEquals(expectedSoldCryptocurrency.soldCrypto(), actualSoldCryptocurrency.soldCrypto(),
                "Error: unexpected output after selling cryptocurrency");
        assertEquals(expectedSoldCryptocurrency.sellingPrice(), actualSoldCryptocurrency.sellingPrice(), 3,
                "Error: unexpected selling price after selling cryptocurrency");
        assertEquals(expectedSoldCryptocurrency.profit(), actualSoldCryptocurrency.profit(), 3,
                "Error: unexpected profit after selling cryptocurrency");
    }

    @Test
    void testGetWalletSummaryWithoutLogin() throws UserAlreadyDefined {
        cryptoCurrencyWallet.register("User", "Pass");

        assertThrows(UserNotFound.class, () -> cryptoCurrencyWallet.getWalletSummary(channel),
                "Error: UserNotFoundException was expected, when user is not logged");
    }

    @Test
    void testGetWalletSummarySuccessfully() throws UserAlreadyDefined, UserNotFound, InsufficientAvailabilityException {
        cryptoCurrencyWallet.register("User", "Pass");
        cryptoCurrencyWallet.login(channel, "User", "Pass");
        cryptoCurrencyWallet.depositMoney(channel, 2500);

        CryptoInformation BTC = new CryptoInformation("BTC", "Bitcoin", 1, 20000.00);
        Set<CryptoInformation> crypto = new HashSet<>();
        crypto.add(BTC);
        Cryptocurrencies cryptocurrencies = new Cryptocurrencies(crypto, "2023-09-05 11:00:00");
        cryptoCurrencyWallet.setCryptocurrencies(cryptocurrencies);

        cryptoCurrencyWallet.buy(channel, "BTC", 1000);
        StringBuilder builder = new StringBuilder();
        builder.append("Money: 1500.0 ActiveInvestments:  ID:BTC Name:Bitcoin boughtPrice:1000.0 boughtCount:0.05  ");

        assertEquals(builder.toString(), cryptoCurrencyWallet.getWalletSummary(channel).toString(),
                "Error: unexpected output for get_wallet_summary");
    }

    @Test
    void testGetWalletOverallSummarySuccessfully() throws UserAlreadyDefined, UserNotFound,
            InsufficientAvailabilityException, InvalidSellingException {

        cryptoCurrencyWallet.register("User", "Pass");
        cryptoCurrencyWallet.login(channel, "User", "Pass");
        cryptoCurrencyWallet.depositMoney(channel, 3500);

        CryptoInformation BTC = new CryptoInformation("BTC", "Bitcoin", 1, 20000.00);
        CryptoInformation ETH = new CryptoInformation("ETH", "Ethereum", 1, 10000.00);
        Set<CryptoInformation> crypto = new HashSet<>();
        crypto.add(BTC);
        crypto.add(ETH);
        Cryptocurrencies cryptocurrencies = new Cryptocurrencies(crypto, "2023-09-05 11:00:00");
        cryptoCurrencyWallet.setCryptocurrencies(cryptocurrencies);

        cryptoCurrencyWallet.buy(channel, "BTC", 1000);
        cryptoCurrencyWallet.buy(channel, "ETH", 500);
        cryptoCurrencyWallet.sell(channel, "ETH");
        StringBuilder builder = new StringBuilder();
        builder.append("ActiveInvestments:  ID:BTC Name:Bitcoin boughtPrice:1000.0 currentPrice:1000.0 currentProfit:0.0  " +
                "FinishedInvestments: ID:ETH Name:Ethereum currentProfit:0.0  overallProfit:0.0  ");

        assertEquals(builder.toString(), cryptoCurrencyWallet.getWalletOverAllSummary(channel).toString(),
                "Error: unexpected output for get_wallet_overall_summary");
    }

    @Test
    void testGetWalletOverallSummaryWithoutLogin() throws UserAlreadyDefined {
        cryptoCurrencyWallet.register("User", "Pass");

        assertThrows(UserNotFound.class, () -> cryptoCurrencyWallet.getWalletOverAllSummary(channel),
                "Error: UserNotFoundException was expected, when user is not logged");
    }

    @Test
    void testDisconnectWithoutLogin() throws UserAlreadyDefined {
        cryptoCurrencyWallet.register("User", "Pass");

        assertThrows(UserNotFound.class, () -> cryptoCurrencyWallet.disconnect(channel),
                "Error: UserNotFoundException was expected, when user is not logged");
    }

    @Test
    void testDisconnectSuccessfully() throws UserAlreadyDefined, UserNotFound, IOException, NoSuchAlgorithmException {
        cryptoCurrencyWallet.register("User", "Pass");
        cryptoCurrencyWallet.login(channel, "User", "Pass");

        doNothing().when(infoSaver).saveUsersInfoToFile(any(Set.class));
        User expectedResult = new User("User", hashPassword("Pass"));
        assertEquals(expectedResult, cryptoCurrencyWallet.disconnect(channel),
                "Error: unexpected output for disconnect");
    }
}
