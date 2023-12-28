package bg.sofia.uni.fmi.mjt.cryptowallet.storage;

import bg.sofia.uni.fmi.mjt.cryptowallet.coinapi.dto.CryptoInformation;
import bg.sofia.uni.fmi.mjt.cryptowallet.coinapi.dto.Cryptocurrencies;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InsufficientAvailabilityException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InvalidSellingException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.UserAlreadyDefined;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.UserNotFound;
import bg.sofia.uni.fmi.mjt.cryptowallet.log.DataSaver;
import bg.sofia.uni.fmi.mjt.cryptowallet.log.Log;
import bg.sofia.uni.fmi.mjt.cryptowallet.user.BoughtCryptocurrency;
import bg.sofia.uni.fmi.mjt.cryptowallet.user.SoldCryptocurrency;
import bg.sofia.uni.fmi.mjt.cryptowallet.user.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class CryptoCurrencyWallet implements CryptoCurrencyWalletAPI {

    private static final String CRYPTO_INFO_FILE = "CryptoInformation.txt";
    private static final String USERS_INFO_FILE = "UsersInformation.txt";
    private static final String SPACE = " ";
    private static final String ID = "ID:";
    private static final String PRICE = "Price:";
    private static final String NAME = "Name:";
    private static final String BOUGHT = "boughtPrice:";
    private static final String CURRENT = "currentPrice:";
    private static final String PROFIT = "currentProfit:";
    private static final String MONEY = "Money: ";
    private static final String OVERALL_PROFIT = "overallProfit:";
    private static final String COUNT = "boughtCount:";
    private static final String ACTIVE_INVESTMENTS = "ActiveInvestments: ";
    private static final String FINISHED_INVESTMENTS = "FinishedInvestments: ";
    private static final int HASHSUM_LENGTH = 16;
    private static final double INITIAL_MONEY = 0.0;
    private static final String HASH_ALG = "MD5";
    private static final Gson GSON = new Gson();
    private Cryptocurrencies cryptocurrencies;
    private Set<User> registeredUsers;
    private final Map<SocketChannel, User> userChannels;
    private final Log log = new Log();
    private final DataSaver infoSaver;

    public CryptoCurrencyWallet(DataSaver infoSaver) {
        userChannels = new HashMap<>();
        this.infoSaver = infoSaver;
        readCryptocurrenciesInfoFromFile();
        initializeUsers();
    }

    @Override
    public User register(String username, String password) throws UserAlreadyDefined {
        for (User curr : registeredUsers) {
            if (curr.username().equals(username)) {
                throw new UserAlreadyDefined("User with this name already existed");
            }
        }

        try {
            String hashedPassword = hashPassword(password);
            User newUser = new User(username, hashedPassword);
            registeredUsers.add(newUser);
            infoSaver.saveUsersInfoToFile(registeredUsers);

            return newUser;

        } catch (IOException | NoSuchAlgorithmException e) {
            log.saveServerException(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public User login(SocketChannel channel, String username, String password) throws UserNotFound {
        for (User currUser : registeredUsers) {
            if (currUser.username().equals(username)) {
                try {
                    String hashPass = hashPassword(password);
                    if (hashPass.equals(currUser.hashedPassword())) {
                        userChannels.put(channel, currUser);
                        return currUser;
                    }
                } catch (NoSuchAlgorithmException e) {
                    log.saveServerException(e);
                    throw new RuntimeException(e);
                }
            }
        }

        throw new UserNotFound("User didn't logged successfully");
    }

    @Override
    public void depositMoney(SocketChannel channel, double amount) throws UserNotFound {
        if (!userChannels.containsKey(channel)) {
            throw new UserNotFound("User is not currently logged");
        }
        double currMoney = userChannels.get(channel).money();
        userChannels.get(channel).setMoney(currMoney + amount);
    }

    @Override
    public StringBuilder listOfferings() {
        if (infoSaver.checkCryptocurrenciesForUpdate(cryptocurrencies)) {
            cryptocurrencies = infoSaver.getCryptocurrenciesFromApi();
        }

        StringBuilder builder = new StringBuilder();
        for (var currCrypto : cryptocurrencies.cryptocurrencies()) {
            builder.append(ID);
            builder.append(currCrypto.assetID());
            builder.append(SPACE);
            builder.append(NAME);
            builder.append(currCrypto.assetName());
            builder.append(SPACE);
            builder.append(PRICE);
            builder.append(currCrypto.price());
            builder.append(SPACE);
            builder.append(SPACE);
        }

        return builder;
    }

    @Override
    public BoughtCryptocurrency buy(SocketChannel channel, String assetID, double investingMoney) throws UserNotFound,
            InsufficientAvailabilityException {

        User currUser = checkForBuying(channel, investingMoney);
        CryptoInformation cryptoInfo = getCryptoInfo(assetID);

        currUser.setMoney(currUser.money() - investingMoney);
        double countBoughtCrypto = investingMoney / cryptoInfo.price();
        BoughtCryptocurrency boughtCrypto = new BoughtCryptocurrency(cryptoInfo, investingMoney, countBoughtCrypto);
        if (currUser.boughtCryptocurrencies().contains(boughtCrypto)) {
            BoughtCryptocurrency crypto = new BoughtCryptocurrency(cryptoInfo, investingMoney * 2,
                    countBoughtCrypto * 2);

            currUser.boughtCryptocurrencies().remove(boughtCrypto);
            currUser.boughtCryptocurrencies().add(crypto);
        }
        else {
            currUser.boughtCryptocurrencies().add(boughtCrypto);
        }
        userChannels.put(channel, currUser);

        return boughtCrypto;
    }

    @Override
    public SoldCryptocurrency sell(SocketChannel channel, String assetID) throws UserNotFound, InvalidSellingException {

        if (!userChannels.containsKey(channel)) {
            throw new UserNotFound("User is not currently logged");
        }
        User currUser = userChannels.get(channel);

        if (infoSaver.checkCryptocurrenciesForUpdate(cryptocurrencies)) {
            cryptocurrencies = infoSaver.getCryptocurrenciesFromApi();
        }

        CryptoInformation info = getCryptoInfo(assetID);

        double currentSellingPrice = info.price();
        double countBoughtCrypto = INITIAL_MONEY;
        double givenMoneyBoughtCrypto = INITIAL_MONEY;

        Iterator<BoughtCryptocurrency> iterator = currUser.boughtCryptocurrencies().iterator();
        while (iterator.hasNext()) {
            BoughtCryptocurrency boughtCryptocurrency = iterator.next();
            if (boughtCryptocurrency.boughtCrypto().assetID().equals(assetID.strip())) {
                countBoughtCrypto += boughtCryptocurrency.buyingCount();
                givenMoneyBoughtCrypto += boughtCryptocurrency.buyingPrice();
                iterator.remove();
            }
        }

        if (countBoughtCrypto == INITIAL_MONEY) {
            throw new InvalidSellingException("You can't sell cryptocurrency that you haven't bought");
        }
        double sumToEarn = countBoughtCrypto * currentSellingPrice;
        currUser.setMoney(currUser.money() + sumToEarn);
        double profit = sumToEarn - givenMoneyBoughtCrypto;
        SoldCryptocurrency soldCrypto = new SoldCryptocurrency(info, sumToEarn, profit);
        currUser.soldCryptocurrencies().add(soldCrypto);
        userChannels.put(channel, currUser);

        return soldCrypto;
    }

    @Override
    public StringBuilder getWalletSummary(SocketChannel channel) throws UserNotFound {
        if (!userChannels.containsKey(channel)) {
            throw new UserNotFound("User is not currently logged in");
        }
        User currUser = userChannels.get(channel);
        StringBuilder builder = new StringBuilder();

        builder.append(MONEY);
        builder.append(currUser.money());
        builder.append(SPACE);
        builder.append(ACTIVE_INVESTMENTS);
        builder.append(SPACE);

        for (var curr : currUser.boughtCryptocurrencies()) {
            builder.append(ID);
            builder.append(curr.boughtCrypto().assetID());
            builder.append(SPACE);
            builder.append(NAME);
            builder.append(curr.boughtCrypto().assetName());
            builder.append(SPACE);
            builder.append(BOUGHT);
            builder.append(curr.buyingPrice());
            builder.append(SPACE);
            builder.append(COUNT);
            builder.append(curr.buyingCount());
            builder.append(SPACE);
            builder.append(SPACE);
        }

        return builder;
    }

    @Override
    public StringBuilder getWalletOverAllSummary(SocketChannel channel) throws UserNotFound {

        if (!userChannels.containsKey(channel)) {
            throw new UserNotFound("User is not currently logged in");
        }
        User currUser = userChannels.get(channel);

        if (infoSaver.checkCryptocurrenciesForUpdate(cryptocurrencies)) {
            cryptocurrencies = infoSaver.getCryptocurrenciesFromApi();
        }

        StringBuilder builder = new StringBuilder();
        double overallProfit = INITIAL_MONEY;

        builder.append(ACTIVE_INVESTMENTS);
        builder.append(SPACE);
        for (var currCrypto : currUser.boughtCryptocurrencies()) {
            for (var curr : cryptocurrencies.cryptocurrencies()) {
                if (currCrypto.boughtCrypto().assetID().equals(curr.assetID())) {
                    double boughtPrice = currCrypto.buyingPrice();
                    double currPrice = curr.price();
                    double profit = (currPrice * currCrypto.buyingCount()) - boughtPrice;
                    overallProfit += profit;

                    builder.append(ID);
                    builder.append(curr.assetID());
                    builder.append(SPACE);
                    builder.append(NAME);
                    builder.append(curr.assetName());
                    builder.append(SPACE);
                    builder.append(BOUGHT);
                    builder.append(currCrypto.buyingPrice());
                    builder.append(SPACE);
                    builder.append(CURRENT);
                    builder.append(curr.price() * currCrypto.buyingCount());
                    builder.append(SPACE);
                    builder.append(PROFIT);
                    builder.append(profit);
                    builder.append(SPACE);
                    builder.append(SPACE);
                }
            }
        }
        for (var currCrypto : currUser.soldCryptocurrencies()) {
            overallProfit += currCrypto.profit();
        }
        builder.append(getFinishedInvestments(currUser));
        builder.append(OVERALL_PROFIT);
        builder.append(overallProfit);
        builder.append(SPACE);
        builder.append(SPACE);

        return builder;
    }

    @Override
    public User disconnect(SocketChannel channel) throws UserNotFound, IOException {
        if (!userChannels.containsKey(channel)) {
            throw new UserNotFound("User is not currently logged");
        }

        User user = userChannels.get(channel);
        userChannels.remove(channel);
        infoSaver.saveUsersInfoToFile(registeredUsers);

        return user;
    }

    public String getUsername(SocketChannel channel) {
        return userChannels.get(channel).username();
    }

    private void initializeUsers() {
        File usersFile = new File(USERS_INFO_FILE);
        try (Reader reader = new FileReader(usersFile)) {
            if (usersFile.length() == 0) {
                registeredUsers = new HashSet<>();
            } else {
                Type cryptoListType = new TypeToken<Set<User>>() {
                }.getType();
                registeredUsers = GSON.fromJson(reader, cryptoListType);
            }
        } catch (IOException e) {
            registeredUsers = new HashSet<>();
            log.saveServerException(e);
        }
    }

    private void readCryptocurrenciesInfoFromFile() {
        File cryptoFile = new File(CRYPTO_INFO_FILE);

        try (Reader reader = new FileReader(cryptoFile)) {
            if (cryptoFile.length() == 0) {
                infoSaver.getCryptocurrenciesFromApi();
            }
            else {
                cryptocurrencies = GSON.fromJson(reader, Cryptocurrencies.class);
            }
        } catch (IOException e) {
            infoSaver.getCryptocurrenciesFromApi();
            log.saveServerException(e);
        }
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance(HASH_ALG);
        m.reset();
        m.update(password.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        return bigInt.toString(HASHSUM_LENGTH);
    }

    private User checkForBuying(SocketChannel channel, double investingMoney) throws UserNotFound,
            InsufficientAvailabilityException {

        if (!userChannels.containsKey(channel)) {
            throw new UserNotFound("User is not currently logged in");
        }
        User currUser = userChannels.get(channel);

        if (investingMoney > currUser.money()) {
            throw new InsufficientAvailabilityException("You don't have enough money");
        }

        return currUser;
    }

    private CryptoInformation getCryptoInfo(String assetID) {
        CryptoInformation cryptoInfo = null;
        for (var currInfo : cryptocurrencies.cryptocurrencies()) {
            if (currInfo.assetID().equals(assetID.strip())) {
                cryptoInfo = currInfo;
                break;
            }
        }
        if (cryptoInfo == null) {
            throw new IllegalArgumentException("Current cryptocurrency is missing");
        }

        return cryptoInfo;
    }

    private StringBuilder getFinishedInvestments(User user) {
        StringBuilder builder = new StringBuilder();

        builder.append(FINISHED_INVESTMENTS);
        for (var currCrypto : user.soldCryptocurrencies()) {
            builder.append(ID);
            builder.append(currCrypto.soldCrypto().assetID());
            builder.append(SPACE);
            builder.append(NAME);
            builder.append(currCrypto.soldCrypto().assetName());
            builder.append(SPACE);
            builder.append(PROFIT);
            builder.append(currCrypto.profit());
            builder.append(SPACE);
            builder.append(SPACE);
        }

        return builder;
    }

    public User user(SocketChannel channel) {
        return userChannels.get(channel);
    }

    public Cryptocurrencies getCryptocurrencies() {
        return cryptocurrencies;
    }

    public void setCryptocurrencies(Cryptocurrencies crypto) {
        this.cryptocurrencies = crypto;
    }
}