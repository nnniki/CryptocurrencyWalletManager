package bg.sofia.uni.fmi.mjt.cryptowallet.exception;

public class UserAlreadyDefined extends Exception {
    public UserAlreadyDefined(String message) {
        super(message);
    }

    public UserAlreadyDefined(String message, Throwable cause) {
        super(message, cause);
    }
}
