package bg.sofia.uni.fmi.mjt.cryptowallet;

import bg.sofia.uni.fmi.mjt.cryptowallet.command.CommandType;
import bg.sofia.uni.fmi.mjt.cryptowallet.log.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class CryptoClient {

    private static final String CS_NAME = "UTF-8";
    private static final String HELP = "help";
    private static final String INVALID_INPUT = "User's input is invalid, check the help menu";
    private static final String DISCONNECT = "User saved and disconnected successfully";
    private static final String NOT_LOGGED = "User is not currently logged in";
    private static final String SPACE = " ";
    private static final Log log = new Log();

    public static StringBuilder help() {
        StringBuilder builder = new StringBuilder();
        builder.append("register <username> <password> - Register into the system");
        builder.append(System.lineSeparator());
        builder.append("login <username> <password> - Login into your profile");
        builder.append(System.lineSeparator());
        builder.append("deposit_money <amount_of_money> - Deposit money into your account");
        builder.append(System.lineSeparator());
        builder.append("list_offerings - See information about currently available cryptocurrencies");
        builder.append(System.lineSeparator());
        builder.append("buy <cryptoID> <invested_money> - Buy cryptocurrency into your wallet");
        builder.append(System.lineSeparator());
        builder.append("sell <cryptoID> - Sell cryptocurrency from your wallet");
        builder.append(System.lineSeparator());
        builder.append("get_wallet_summary - See your amount of money and currently active investments");
        builder.append(System.lineSeparator());
        builder.append("get_wallet_overall_summary - See all your Active and Finished investments" +
                    " and your currently profit/loss");
        builder.append(System.lineSeparator());
        builder.append("disconnect - Save your current activity and exit");

        return builder;
    }

    private static boolean checkIfCommandIsList(String message, String serverAnswer) {
        return message.equals(CommandType.list_offerings.name()) && !serverAnswer.equals(INVALID_INPUT);
    }

    private static boolean checkIfCommandIsSummary(String message, String serverAnswer) {
        return message.equals(CommandType.get_wallet_summary.name()) && !serverAnswer.equals(INVALID_INPUT)
                && !serverAnswer.equals(NOT_LOGGED);
    }

    private static boolean checkIfCommandIsOverallSummary(String message, String serverAnswer) {
        return message.equals(CommandType.get_wallet_overall_summary.name()) && !serverAnswer.equals(NOT_LOGGED)
                && !serverAnswer.equals(INVALID_INPUT);
    }

    public static String formatStringOutput(String message, String serverAnswer) {

        if (checkIfCommandIsList(message, serverAnswer) || checkIfCommandIsSummary(message, serverAnswer)
                || checkIfCommandIsOverallSummary(message, serverAnswer)) {

            return serverAnswer.replace(SPACE , System.lineSeparator());
        }

        return serverAnswer;
    }

    public static void main(String[] args) {
        try (SocketChannel socketChannel = SocketChannel.open();
            BufferedReader reader = new BufferedReader(Channels.newReader(socketChannel, CS_NAME));
            PrintWriter writer = new PrintWriter(Channels.newWriter(socketChannel, CS_NAME), true);
            Scanner scanner = new Scanner(System.in)) {

            String HOST_NAME = System.getenv("HOST_NAME");
            String SERVER_PORT = System.getenv("SERVER_PORT");
            socketChannel.connect(new InetSocketAddress(HOST_NAME, SERVER_PORT));

            System.out.println("Connected to the server.");
            System.out.println("You can enter help to see the instructions");

            while (true) {
                System.out.print("Enter message: ");
                String message = scanner.nextLine();
                if (message.equals(HELP)) {
                    System.out.println(help());
                    continue;
                }

                writer.println(message);
                String reply = reader.readLine();

                if (reply.equals(DISCONNECT)) {
                    break;
                }

                reply = formatStringOutput(message, reply);

                System.out.println("The server replied: " + System.lineSeparator() + reply + System.lineSeparator());
            }
        } catch (IOException e) {
            log.saveServerException(e);
            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }
}
