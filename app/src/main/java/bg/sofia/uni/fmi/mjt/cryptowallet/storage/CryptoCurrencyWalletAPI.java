package bg.sofia.uni.fmi.mjt.cryptowallet.storage;

import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InsufficientAvailabilityException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InvalidSellingException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.UserAlreadyDefined;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.UserNotFound;
import bg.sofia.uni.fmi.mjt.cryptowallet.user.BoughtCryptocurrency;
import bg.sofia.uni.fmi.mjt.cryptowallet.user.SoldCryptocurrency;
import bg.sofia.uni.fmi.mjt.cryptowallet.user.User;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface CryptoCurrencyWalletAPI {

    User register(String username, String password) throws UserAlreadyDefined;

    User login(SocketChannel channel, String username, String password) throws UserNotFound;

    void depositMoney(SocketChannel channel, double amount) throws UserNotFound;

    StringBuilder listOfferings();

    BoughtCryptocurrency buy(SocketChannel channel, String assetID, double investingAmount) throws UserNotFound,
            InsufficientAvailabilityException;

    SoldCryptocurrency sell(SocketChannel channel, String assetID) throws UserNotFound, InvalidSellingException;

    StringBuilder getWalletSummary(SocketChannel channel) throws UserNotFound;

    StringBuilder getWalletOverAllSummary(SocketChannel channel) throws UserNotFound;

    User disconnect(SocketChannel channel) throws UserNotFound, IOException;
}
