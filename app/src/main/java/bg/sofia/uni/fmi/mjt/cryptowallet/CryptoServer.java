package bg.sofia.uni.fmi.mjt.cryptowallet;

import bg.sofia.uni.fmi.mjt.cryptowallet.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.cryptowallet.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.cryptowallet.log.DataSaver;
import bg.sofia.uni.fmi.mjt.cryptowallet.log.Log;
import bg.sofia.uni.fmi.mjt.cryptowallet.storage.CryptoCurrencyWallet;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class CryptoServer {

    private static final int BUFFER_SIZE = 10000;
    private static final int SERVER_PORT = 7777;
    private static final String HOST = "localhost";
    private static final String INVALID_REQUEST = "This request is invalid, please check help menu!";
    private static final String DISCONNECT = "User saved and disconnected successfully";
    private final CommandExecutor commandExecutor;
    private final int port;
    private final Log log;
    private boolean isServerWorking;
    private ByteBuffer buffer;
    private Selector selector;

    public CryptoServer(int port, CommandExecutor commandExecutor) {
        log = new Log();
        this.port = port;
        this.commandExecutor = commandExecutor;
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
            isServerWorking = true;
            while (isServerWorking) {
                try {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }

                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        if (key.isReadable()) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            String clientInput = getClientInput(clientChannel);
                            if (clientInput == null) {
                                continue;
                            }
                            System.out.println("Client " + clientChannel.getRemoteAddress() + ": " + clientInput);

                            String response = "";
                            try {
                                response = commandExecutor.execute(CommandCreator.of(clientInput), clientChannel);
                                if (!response.endsWith(System.lineSeparator())) {
                                    response += System.lineSeparator();
                                }
                            } catch (IllegalArgumentException e) {
                                log.saveServerException(e);
                                response = INVALID_REQUEST + System.lineSeparator();
                            }
                            finally {
                                writeClientOutput(clientChannel, response);
                            }
                        } else if (key.isAcceptable()) {
                            accept(selector, key);
                        }
                        keyIterator.remove();
                    }
                } catch (IOException e) {
                    log.saveServerException(e);
                    System.out.println("Error occurred while processing client request");
                }
            }
        } catch (IOException e) {
            log.saveServerException(e);
            throw new UncheckedIOException("Failed to start server", e);
        }
    }

    public void stop() {
        this.isServerWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST, this.port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes <= 0) {
            System.out.println("Client has closed the connection");
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        clientChannel.write(buffer);
        if (output.equals(DISCONNECT)) {
            clientChannel.close();
        }
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    public static void main(String[] args) {
        DataSaver saver = new DataSaver();
        CryptoCurrencyWallet wallet = new CryptoCurrencyWallet(saver);
        Log log = new Log();
        CommandExecutor executor = new CommandExecutor(wallet, log);
        CryptoServer server = new CryptoServer(SERVER_PORT, executor);
        server.start();
    }
}