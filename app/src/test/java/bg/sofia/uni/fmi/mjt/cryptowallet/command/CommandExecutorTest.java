package bg.sofia.uni.fmi.mjt.cryptowallet.command;

import bg.sofia.uni.fmi.mjt.cryptowallet.coinapi.dto.CryptoInformation;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InsufficientAvailabilityException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InvalidSellingException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.UserAlreadyDefined;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.UserNotFound;
import bg.sofia.uni.fmi.mjt.cryptowallet.log.Log;
import bg.sofia.uni.fmi.mjt.cryptowallet.storage.CryptoCurrencyWallet;
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
import java.nio.channels.SocketChannel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommandExecutorTest {

    private static final String INVALID_INPUT = "User's input is invalid, check the help menu";
    private static final String SUCCESSFUL_REGISTRATION = "User is registered successfully";
    private static final String SUCCESSFUL_LOGIN = "User logged successfully";
    private static final String SUCCESSFUL_DEPOSIT = "User's deposit was successful";
    private static final String SUCCESSFUL_DISCONNECT = "User saved and disconnected successfully";
    private static final String INVALID_DEPOSIT = "Deposited amount of money must be positive";
    private static final String SUCCESSFUL_BUY = "You successfully bought";
    private static final String SUCCESSFUL_SELL = "You successfully sold";
    private CommandExecutor commandExecutor;
    private SocketChannel channel;

    @Mock
    private CryptoCurrencyWallet cryptoCurrencyWallet;

    @Mock
    private Log log;

    @BeforeEach
    void setUp() throws IOException {
        commandExecutor = new CommandExecutor(cryptoCurrencyWallet, log);
        channel = SocketChannel.open();
    }

    @AfterEach
    void closeChannel() throws IOException {
        channel.close();
    }

    @Test
    void testRegisterInvalidNumberOfParameters() {
        doNothing().when(log).saveServerError(any(String.class));

        Command add = new Command(CommandType.register, new String[]{"user"});
        assertEquals(INVALID_INPUT, commandExecutor.execute(add, channel),
                "Error: unexpected output, when arguments are invalid");
    }

    @Test
    void testRegisterEmptyArgument() {
        doNothing().when(log).saveServerError(any(String.class));

        Command add = new Command(CommandType.register, new String[]{});
        assertEquals(INVALID_INPUT, commandExecutor.execute(add, channel),
                "Error: unexpected output, when arguments are empty");
    }

    @Test
    void testRegisterUserAlreadyDefined() throws UserAlreadyDefined {
        doNothing().when(log).saveServerException(any(Exception.class));
        when(cryptoCurrencyWallet.register("userName", "pass")).
                thenThrow(new UserAlreadyDefined("User with this name already existed"));

        Command cmd = new Command(CommandType.register, new String[]{"userName", "pass"});
        assertEquals("User with this name already existed", commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when user with this name already exists");
    }

    @Test
    void testRegisterSuccessfully() throws UserAlreadyDefined {
        when(cryptoCurrencyWallet.register("userName", "pass")).
                thenReturn(new User("userName", "pass"));

        Command cmd = new Command(CommandType.register, new String[]{"userName", "pass"});
        assertEquals(SUCCESSFUL_REGISTRATION, commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when user is registered successfully");
    }

    @Test
    void testLoginInvalidNumberOfParameters() {
        doNothing().when(log).saveServerError(any(String.class));

        Command add = new Command(CommandType.login, new String[]{"user"});
        assertEquals(INVALID_INPUT, commandExecutor.execute(add, channel),
                "Error: unexpected output, when arguments are invalid");
    }

    @Test
    void testLoginEmptyArgument() {
        doNothing().when(log).saveServerError(any(String.class));

        Command add = new Command(CommandType.login, new String[]{" "});
        assertEquals(INVALID_INPUT, commandExecutor.execute(add, channel),
                "Error: unexpected output, when arguments are blank");
    }

    @Test
    void testLoginUserNotFound() throws UserNotFound {
        doNothing().when(log).saveUserError(any(String.class), any(Exception.class));
        when(cryptoCurrencyWallet.login(channel,"userName", "pass")).
                thenThrow(new UserNotFound("User didn't logged successfully"));

        Command cmd = new Command(CommandType.login, new String[]{"userName", "pass"});
        assertEquals("User didn't logged successfully", commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when user with this name is not registered");
    }

    @Test
    void testLoginSuccessfully() throws UserNotFound {
        when(cryptoCurrencyWallet.login(channel ,"userName", "pass")).
                thenReturn(new User("userName", "pass"));

        Command cmd = new Command(CommandType.login, new String[]{"userName", "pass"});
        assertEquals(SUCCESSFUL_LOGIN, commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when user logged successfully");
    }

    @Test
    void testDepositInvalidNumberOfParameters() {
        doNothing().when(log).saveServerError(any(String.class));

        Command add = new Command(CommandType.deposit_money, new String[]{"user", "1500"});
        assertEquals(INVALID_INPUT, commandExecutor.execute(add, channel),
                "Error: unexpected output, when arguments are invalid");
    }

    @Test
    void testDepositNegativeAmount() {
        doNothing().when(log).saveServerError(any(String.class));

        Command add = new Command(CommandType.deposit_money, new String[]{"-1500"});
        assertEquals(INVALID_DEPOSIT, commandExecutor.execute(add, channel),
                "Error: unexpected output, when deposited amount is negative");
    }

    @Test
    void testDepositUserNotFound() throws UserNotFound {
        doNothing().when(log).saveServerException(any(Exception.class));
        doThrow(new UserNotFound("User didn't logged successfully")).when(cryptoCurrencyWallet).depositMoney(channel,1500);

        Command cmd = new Command(CommandType.deposit_money, new String[]{"1500"});
        assertEquals("User didn't logged successfully", commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when user is not logged");
    }

    @Test
    void testDepositSuccessfully() throws UserNotFound {
        doNothing().when(cryptoCurrencyWallet).depositMoney(channel,1500);

        Command cmd = new Command(CommandType.deposit_money, new String[]{"1500"});
        assertEquals(SUCCESSFUL_DEPOSIT, commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when user deposited successfully");
    }

    @Test
    void testListOfferingsInvalidParameters() {
        doNothing().when(log).saveServerError(any(String.class));

        Command cmd = new Command(CommandType.list_offerings, new String[]{"error_param"});
        assertEquals(INVALID_INPUT, commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when parameters are invalid");
    }

    @Test
    void testListOfferingsSuccessfully() {
        Command cmd = new Command(CommandType.list_offerings, new String[]{});
        StringBuilder builder = new StringBuilder();
        builder.append("ID:BTC Name:Bitcoin Price:20235.632  ");
        when(cryptoCurrencyWallet.listOfferings()).thenReturn(builder);

        assertEquals("ID:BTC Name:Bitcoin Price:20235.632  ", commandExecutor.execute(cmd, channel),
                "Error: unexpected output for list_offerings");
    }

    @Test
    void testBuyInvalidNumberOfParameters() {
        doNothing().when(log).saveServerError(any(String.class));

        Command add = new Command(CommandType.buy, new String[]{"BTC"});
        assertEquals(INVALID_INPUT, commandExecutor.execute(add, channel),
                "Error: unexpected output, when arguments are invalid");
    }

    @Test
    void testBuyNegativeAmountOfMoney() {
        doNothing().when(log).saveServerError(any(String.class));

        Command add = new Command(CommandType.buy, new String[]{"BTC", "-1500"});
        assertEquals(INVALID_INPUT, commandExecutor.execute(add, channel),
                "Error: unexpected output, when invested amount of money is negative");
    }

    @Test
    void testBuyNotEnoughMoney() throws UserNotFound, InsufficientAvailabilityException {
        when(cryptoCurrencyWallet.buy(channel, "BTC", 1500)).
                thenThrow(new InsufficientAvailabilityException("You don't have enough money"));

        Command cmd = new Command(CommandType.buy, new String[]{"BTC", "1500"});
        assertEquals("You don't have enough money", commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when investing more money than available");
    }

    @Test
    void testBuyCryptoNotAvailable() throws UserNotFound, InsufficientAvailabilityException {
        doNothing().when(log).saveServerException(any(Exception.class));
        when(cryptoCurrencyWallet.buy(channel, "TMP", 1500)).
                thenThrow(new IllegalArgumentException("Current cryptocurrency is missing"));

        Command cmd = new Command(CommandType.buy, new String[]{"TMP", "1500"});
        assertEquals("Current cryptocurrency is missing", commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when trying to buy unavailable cryptocurrency");
    }

    @Test
    void testBuySuccessfully() throws UserNotFound, InsufficientAvailabilityException {
        CryptoInformation cryptoInformation = new CryptoInformation("BTC", "Bitcoin",1,22000);
        BoughtCryptocurrency bought = new BoughtCryptocurrency(cryptoInformation, 1500, 14.66);
        when(cryptoCurrencyWallet.buy(channel, "BTC", 1500)).
                thenReturn(bought);

        Command cmd = new Command(CommandType.buy, new String[]{"BTC", "1500"});
        assertEquals(SUCCESSFUL_BUY + " BTC", commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when successfully buying cryptocurrency");
    }

    @Test
    void testSellInvalidNumberOfParameters() {
        doNothing().when(log).saveServerError(any(String.class));

        Command add = new Command(CommandType.buy, new String[]{});
        assertEquals(INVALID_INPUT, commandExecutor.execute(add, channel),
                "Error: unexpected output, when arguments are invalid");
    }

    @Test
    void testSellEmptyArgument() {
        doNothing().when(log).saveServerError(any(String.class));

        Command add = new Command(CommandType.buy, new String[]{""});
        assertEquals(INVALID_INPUT, commandExecutor.execute(add, channel),
                "Error: unexpected output, when arguments are invalid");
    }

    @Test
    void testSellCryptoUserNotFound() throws UserNotFound, InvalidSellingException {
        doNothing().when(log).saveServerException(any(Exception.class));
        when(cryptoCurrencyWallet.sell(channel, "BTC")).
                thenThrow(new UserNotFound("User is not currently logged"));

        Command cmd = new Command(CommandType.sell, new String[]{"BTC"});
        assertEquals("User is not currently logged", commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when trying to sell without logging in");
    }

    @Test
    void testSellCryptocurrencyWithoutBuyingIt() throws UserNotFound, InvalidSellingException {
        when(cryptoCurrencyWallet.sell(channel, "BTC")).
                thenThrow(new InvalidSellingException("You can't sell cryptocurrency that you haven't bought"));

        Command cmd = new Command(CommandType.sell, new String[]{"BTC"});
        assertEquals("You can't sell cryptocurrency that you haven't bought", commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when trying to sell crypto before buying it");
    }

    @Test
    void testSellSuccessfully() throws UserNotFound, InvalidSellingException {
        CryptoInformation cryptoInformation = new CryptoInformation("BTC", "Bitcoin",1,22000.0);
        SoldCryptocurrency sold = new SoldCryptocurrency(cryptoInformation, 22000.0, 0.0);
        when(cryptoCurrencyWallet.sell(channel, "BTC")).
                thenReturn(sold);

        Command cmd = new Command(CommandType.sell, new String[]{"BTC"});
        assertEquals(SUCCESSFUL_SELL + " BTC", commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when successfully selling cryptocurrency");
    }

    @Test
    void testGetWalletSummaryInvalidNumberOfParameters() {
        doNothing().when(log).saveServerError(any(String.class));
        Command cmd = new Command(CommandType.get_wallet_summary, new String[]{"BTC"});

        assertEquals(INVALID_INPUT, commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when arguments are invalid");
    }

    @Test
    void testGetWalletSummaryUserNotFound() throws UserNotFound {
        doNothing().when(log).saveServerException(any(Exception.class));
        when(cryptoCurrencyWallet.getWalletSummary(channel)).thenThrow(new UserNotFound("User is not currently logged in"));

        Command cmd = new Command(CommandType.get_wallet_summary, new String[]{});

        assertEquals("User is not currently logged in", commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when using get_wallet_summary without first logging in");
    }

    @Test
    void testGetWalletSummarySuccessfully() throws UserNotFound {
        StringBuilder builder = new StringBuilder();
        builder.append("Money: 1500 ActiveInvestments: ID:BTC Name:Bitcoin boughtPrice:1250.0 boughtCount: 0.04836  ");
        when(cryptoCurrencyWallet.getWalletSummary(channel)).thenReturn(builder);

        Command cmd = new Command(CommandType.get_wallet_summary, new String[]{});
        assertEquals(builder.toString(), commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when using get_wallet_summary");
    }

    @Test
    void testGetWalletOverallSummaryInvalidNumberOfParameters() {
        doNothing().when(log).saveServerError(any(String.class));
        Command cmd = new Command(CommandType.get_wallet_overall_summary, new String[]{"BTC"});

        assertEquals(INVALID_INPUT, commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when arguments are invalid");
    }

    @Test
    void testGetWalletOverallSummaryUserNotFound() throws UserNotFound {
        doNothing().when(log).saveServerException(any(Exception.class));
        when(cryptoCurrencyWallet.getWalletOverAllSummary(channel)).thenThrow(new UserNotFound("User is not currently logged in"));

        Command cmd = new Command(CommandType.get_wallet_overall_summary, new String[]{});

        assertEquals("User is not currently logged in", commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when using get_wallet_overall_summary without first logging in");
    }

    @Test
    void testGetWalletOverallSummarySuccessfully() throws UserNotFound {
        StringBuilder builder = new StringBuilder();
        builder.append("ActiveInvestments: ID:BTC Name:Bitcoin boughtPrice:1250.0 currentPrice:1263.1213 " +
                "currentProfit:13.1213  FinishedInvestments: ID:ETH Name:Ethereum currentProfit:575.36  overallProfit:589.4813  ");
        when(cryptoCurrencyWallet.getWalletSummary(channel)).thenReturn(builder);

        Command cmd = new Command(CommandType.get_wallet_summary, new String[]{});
        assertEquals(builder.toString(), commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when using get_wallet_overall_summary");
    }

    @Test
    void testDisconnectInvalidNumberOfParameters() {
        doNothing().when(log).saveServerError(any(String.class));

        Command cmd = new Command(CommandType.disconnect, new String[]{"errorInput"});
        assertEquals(INVALID_INPUT, commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when using disconnect with invalid parameters");
    }

    @Test
    void testDisconnectUserNotFound() throws UserNotFound, IOException {
        doNothing().when(log).saveServerException(any(Exception.class));
        when(cryptoCurrencyWallet.disconnect(channel)).thenThrow(new UserNotFound("User is not currently logged in"));

        Command cmd = new Command(CommandType.disconnect, new String[]{});

        assertEquals("User is not currently logged in", commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when using disconnect without first logging in");
    }

    @Test
    void testDisconnectSuccessfully() throws UserNotFound, IOException {
        when(cryptoCurrencyWallet.disconnect(channel)).thenReturn(new User("user", "pass"));

        Command cmd = new Command(CommandType.disconnect, new String[]{});
        assertEquals(SUCCESSFUL_DISCONNECT, commandExecutor.execute(cmd, channel),
                "Error: unexpected output, when disconnecting successfully");
    }
}