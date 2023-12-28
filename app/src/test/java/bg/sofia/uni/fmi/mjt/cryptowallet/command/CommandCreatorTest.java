package bg.sofia.uni.fmi.mjt.cryptowallet.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandCreatorTest {

    @Test
    void testCreateCommandOneArgument() {
        String input = "sell BTC";
        String[] args = {"BTC"};

        Command cmd = CommandCreator.of(input);
        assertEquals("sell", cmd.type().name(), "Error: command type is invalid");
        assertArrayEquals(args, cmd.arguments(), "Error: command arguments are invalid");
        assertDoesNotThrow(() -> IllegalArgumentException.class, "Error: Exception is not expected, when command is valid");
    }

    @Test
    void testCreateCommandInvalidCommand() {
        String input = "view crypto";

        assertThrows(IllegalArgumentException.class, () -> CommandCreator.of(input),
                "IllegalArgumentException was expected, when command is invalid");
    }

    @Test
    void testCreateCommandWithoutArguments() {
        String input = "list_offerings";

        Command cmd = CommandCreator.of(input);
        assertEquals("list_offerings", cmd.type().name(), "Error: command type is invalid");
        assertEquals(0, cmd.arguments().length, "Error: command's arguments was expected to be 0");
        assertDoesNotThrow(() -> IllegalArgumentException.class, "Error: Exception is not expected, when command is valid");
    }
}
