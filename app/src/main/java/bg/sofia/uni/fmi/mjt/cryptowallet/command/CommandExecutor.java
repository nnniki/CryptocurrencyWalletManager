package bg.sofia.uni.fmi.mjt.cryptowallet.command;

import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InsufficientAvailabilityException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InvalidSellingException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.UserAlreadyDefined;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.UserNotFound;
import bg.sofia.uni.fmi.mjt.cryptowallet.log.Log;
import bg.sofia.uni.fmi.mjt.cryptowallet.storage.CryptoCurrencyWallet;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class CommandExecutor {

    private static final String INVALID_INPUT = "User's input is invalid, check the help menu";
    private static final String SUCCESSFUL_REGISTRATION = "User is registered successfully";
    private static final String SUCCESSFUL_LOGIN = "User logged successfully";
    private static final String SUCCESSFUL_DEPOSIT = "User's deposit was successful";
    private static final String SUCCESSFUL_DISCONNECT = "User saved and disconnected successfully";
    private static final String INVALID_DEPOSIT = "Deposited amount of money must be positive";
    private static final String SUCCESSFUL_BUY = "You successfully bought";
    private static final String SUCCESSFUL_SELL = "You successfully sold";
    private static final double INITIAL_MONEY = 0.0;
    private final CryptoCurrencyWallet cryptoCurrencyWallet;
    private final Log log;

    public CommandExecutor(CryptoCurrencyWallet cryptoCurrencyWallet, Log log) {
        this.cryptoCurrencyWallet = cryptoCurrencyWallet;
        this.log = log;
    }

    public String execute(Command cmd, SocketChannel channel) {
        return switch(cmd.type()) {
            case register -> register(cmd.arguments());
            case login -> login(channel, cmd.arguments());
            case list_offerings -> list(cmd.arguments());
            case deposit_money -> deposit(channel, cmd.arguments());
            case buy -> buy(channel, cmd.arguments());
            case sell -> sell(channel, cmd.arguments());
            case get_wallet_summary -> getWalletSummary(channel, cmd.arguments());
            case get_wallet_overall_summary -> getWalletOverallSummary(channel, cmd.arguments());
            case disconnect -> disconnect(channel, cmd.arguments());
        };
    }

    private boolean checkNullEmptyBlank(String arg) {
        return arg == null || arg.isEmpty() || arg.isBlank();
    }

    private String register(String[] args) {
        if (args.length != 2) {
            log.saveServerError("Error occurred: Invalid use of register - argument list is not as expected");
            return INVALID_INPUT;
        }
        if (checkNullEmptyBlank(args[0]) || checkNullEmptyBlank(args[1])) {
            log.saveServerError("Error occurred: Invalid use of register - some of the arguments are null or empty");
            return INVALID_INPUT;
        }

        String response = SUCCESSFUL_REGISTRATION;
        try {
            cryptoCurrencyWallet.register(args[0], args[1]);
        } catch (UserAlreadyDefined e) {
            log.saveServerException(e);
            response = e.getMessage();
        }

        return response;
    }

    private String login(SocketChannel channel, String[] args) {
        if (args.length != 2) {
            log.saveServerError("Error occurred: Invalid use of login - argument list is not as expected");
            return INVALID_INPUT;
        }
        if (checkNullEmptyBlank(args[0]) || checkNullEmptyBlank(args[1])) {
            log.saveServerError("Error occurred: Invalid use of login - some of the arguments are null or empty");
            return INVALID_INPUT;
        }

        String response = SUCCESSFUL_LOGIN;
        try {
            cryptoCurrencyWallet.login(channel, args[0], args[1]);
        } catch (UserNotFound e) {
            log.saveUserError(args[0], e);
            response = e.getMessage();
        }

        return response;
    }

    private String deposit(SocketChannel channel, String[] args) {
        if (args.length != 1) {
            log.saveServerError("Error occurred: Invalid use of deposit_money - argument list is not as expected");
            return INVALID_INPUT;
        }
        if (checkNullEmptyBlank(args[0])) {
            log.saveServerError("Error occurred: Invalid use of deposit_money - argument list is not as expected");
            return INVALID_INPUT;
        }

        double amount = Double.parseDouble(args[0]);
        if (amount <= INITIAL_MONEY) {
            log.saveServerError("Error occurred: Invalid use of deposit_money - negative amount");
            return INVALID_DEPOSIT;
        }
        String response = SUCCESSFUL_DEPOSIT;

        try {
            cryptoCurrencyWallet.depositMoney(channel, amount);
        } catch (UserNotFound e) {
            log.saveServerException(e);
            response = e.getMessage();
        }
        return response;
    }

    private String list(String[] args) {
        if (args.length != 0) {
            log.saveServerError("Error occurred: Invalid use of list_offerings - argument list is not as expected");
            return INVALID_INPUT;
        }

        StringBuilder response = cryptoCurrencyWallet.listOfferings();
        return response.toString();
    }

    private String buy(SocketChannel channel, String[] args) {
        if (args.length != 2) {
            log.saveServerError("Error occurred: Invalid use of buy - argument list is not as expected");
            return INVALID_INPUT;
        }
        double investingMoney = Double.parseDouble(args[1]);
        if (investingMoney <= INITIAL_MONEY || checkNullEmptyBlank(args[0])) {
            log.saveServerError("Error occurred: Invalid use of buy - argument is invalid");
            return INVALID_INPUT;
        }

        String response = SUCCESSFUL_BUY + " " + args[0];
        try {
            cryptoCurrencyWallet.buy(channel, args[0], investingMoney);

        } catch (InsufficientAvailabilityException e) {
            String currUsername = cryptoCurrencyWallet.getUsername(channel);
            log.saveUserError(currUsername, e);
            response = e.getMessage();
        } catch (IllegalArgumentException | UserNotFound e) {
            log.saveServerException(e);
            response = e.getMessage();
        }

        return response;
    }

    private String sell(SocketChannel channel, String[] args) {
        if (args.length != 1) {
            log.saveServerError("Error occurred: Invalid use of sell - argument list is not as expected");
            return INVALID_INPUT;
        }
        if (checkNullEmptyBlank(args[0])) {
            log.saveServerError("Error occurred: Invalid use of sell - argument is null or empty");
            return INVALID_INPUT;
        }
        String response = SUCCESSFUL_SELL + " " + args[0];

        try {
            cryptoCurrencyWallet.sell(channel, args[0]);
        } catch (UserNotFound | IllegalArgumentException e) {
            log.saveServerException(e);
            response = e.getMessage();
        } catch (InvalidSellingException e) {
            log.saveUserError(cryptoCurrencyWallet.getUsername(channel), e);
            response = e.getMessage();
        }

        return response;
    }

    private String getWalletSummary(SocketChannel channel, String[] args) {
        if (args.length != 0) {
            log.saveServerError("Error occurred: Invalid use of get_wallet_summary " +
                    "- argument list is not as expected");

            return INVALID_INPUT;
        }
        StringBuilder builder;

        try {
            builder = cryptoCurrencyWallet.getWalletSummary(channel);

        } catch (UserNotFound e) {
            log.saveServerException(e);
            builder = new StringBuilder(e.getMessage());
        }

        return builder.toString();
    }

    private String getWalletOverallSummary(SocketChannel channel, String[] args) {
        if (args.length != 0) {
            log.saveServerError("Error occurred: Invalid use of get_wallet_overall_summary " +
                    "- argument list is not as expected");

            return INVALID_INPUT;
        }
        StringBuilder builder;

        try {
            builder = cryptoCurrencyWallet.getWalletOverAllSummary(channel);

        } catch (UserNotFound e) {
            log.saveServerException(e);
            builder = new StringBuilder(e.getMessage());
        }

        return builder.toString();
    }

    private String disconnect(SocketChannel channel, String[] args) {
        if (args.length != 0) {
            log.saveServerError("Error occurred: Invalid use of disconnect - argument list is not as expected");
            return INVALID_INPUT;
        }
        String response = SUCCESSFUL_DISCONNECT;

        try {
            cryptoCurrencyWallet.disconnect(channel);
        } catch (UserNotFound e) {
            log.saveServerException(e);
            response = e.getMessage();
        } catch (IOException e) {
            log.saveServerException(e);
            throw new RuntimeException(e.getMessage());
        }

        return response;
    }
}