package bg.sofia.uni.fmi.mjt.cryptowallet.command;

public class CommandCreator {
    private static final String SPACE = " ";

    public static Command of(String clientInput) {
        String[] words = clientInput.split(SPACE);

        CommandType type = CommandType.valueOf(words[0].strip());
        String[] arguments = new String[words.length - 1];
        System.arraycopy(words, 1, arguments, 0, words.length - 1);

        return new Command(type, arguments);
    }
}
