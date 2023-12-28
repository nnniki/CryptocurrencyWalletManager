package bg.sofia.uni.fmi.mjt.cryptowallet.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Log {

    private static final String LOG = "log";
    private static final String SERVER = "server";
    private static final String USER = "user";
    private static final String TIME_FORMAT = "yyyy-MM-dd HH-mm-ss";

    public void saveUserError(String username, Exception e) {
        Path currPath = Path.of(LOG + File.separator + USER + File.separator + username);

        try {
            Files.createDirectories(currPath);
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
            String formattedTime = dateFormat.format(date);
            Path filename = Path.of(currPath + File.separator + formattedTime);
            Writer writer = new FileWriter(filename.toFile(), true);
            writer.write(e.getMessage());
            writer.write(System.lineSeparator());
            writer.flush();
            writer.write(Arrays.toString(e.getStackTrace()));
            writer.write(System.lineSeparator());
            writer.flush();
            writer.close();

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void saveServerException(Exception e) {
        Path currPath = Path.of(LOG + File.separator + SERVER);

        try {
            Files.createDirectories(currPath);
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
            String formattedTime = dateFormat.format(date);
            Path filename = Path.of(currPath + File.separator + formattedTime);
            Writer writer = new FileWriter(filename.toFile(), true);
            writer.write(e.getMessage());
            writer.write(System.lineSeparator());
            writer.flush();
            writer.write(Arrays.toString(e.getStackTrace()));
            writer.write(System.lineSeparator());
            writer.flush();
            writer.close();

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void saveServerError(String msg) {
        Path currPath = Path.of(LOG + File.separator + SERVER);

        try {
            Files.createDirectories(currPath);
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
            String formattedTime = dateFormat.format(date);
            Path filename = Path.of(currPath + File.separator + formattedTime);
            Writer writer = new FileWriter(filename.toFile(), true);
            writer.write(msg);
            writer.write(System.lineSeparator());
            writer.flush();
            writer.close();

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
