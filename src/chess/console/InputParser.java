package chess.console;

import chess.model.Position;

/**
 * Turns what the player types into a command or a move.
 *
 * <p>Commands are single words: {@code help}, {@code history},
 * {@code captured}, {@code restart} and {@code quit}, in any case.
 * Anything else is read as a move. The standard move format is
 * {@code FROM TO}, such as {@code E2 E4}; {@code E2-E4} and {@code E2E4}
 * are also accepted, in upper- or lowercase.</p>
 */
public class InputParser {

    /** Creates a parser. It has no settings; each method works only on the text it is given. */
    public InputParser() {
    }

    /**
     * Works out which command the player typed, ignoring case and extra spaces.
     *
     * @param input the text the player typed
     * @return the matching command, or {@link Command#MOVE} if it isn't a command word
     */
    public Command getCommand(String input) {
        if (input == null) {
            return Command.MOVE;
        }
        switch (input.trim().toLowerCase()) {
            case "help":
                return Command.HELP;
            case "history":
                return Command.HISTORY;
            case "captured":
                return Command.CAPTURED;
            case "restart":
                return Command.RESTART;
            case "quit":
                return Command.QUIT;
            default:
                return Command.MOVE;
        }
    }

    /**
     * Reads a move such as {@code "E2 E4"}, {@code "e2-e4"} or {@code "E2E4"} into its two squares.
     *
     * @param input the text the player typed
     * @return an array of two positions: the starting square, then the destination
     * @throws IllegalArgumentException if the input isn't two valid squares
     */
    public Position[] parseMove(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Please enter a move like E2 E4, or type help.");
        }
        String text = input.trim().replace('-', ' ');
        String[] parts = text.split("\\s+");
        if (parts.length == 1 && parts[0].length() == 4) {
            parts = new String[] {parts[0].substring(0, 2), parts[0].substring(2)};
        }
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Enter two squares, like E2 E4. Type help to see all commands.");
        }
        Position from = Position.fromNotation(parts[0]);
        Position to = Position.fromNotation(parts[1]);
        return new Position[] {from, to};
    }
}
