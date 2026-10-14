package chess.console;

import java.util.Scanner;

/**
 * The entry point for console chess. It only starts the user interface;
 * all game logic lives in other classes.
 */
public class ChessApp {

    /** Prevents creating a ChessApp object; this class only holds {@code main}. */
    private ChessApp() {
    }

    /**
     * Starts the console chess game.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ConsoleUI ui = new ConsoleUI(scanner);
        ui.run();
        scanner.close();
    }
}
