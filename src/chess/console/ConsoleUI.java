package chess.console;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;
import chess.pieces.Piece;
import chess.service.Game;
import chess.service.MoveResult;

import java.util.List;
import java.util.Scanner;

/**
 * The text interface: shows the board, reads moves from the keyboard,
 * and prints the result of each move.
 *
 * <p>This is the only class that uses {@code Scanner} and {@code System.out}.
 * All rules are handled by {@link Game}.</p>
 */
public class ConsoleUI {

    /** Text shown for an empty square. */
    private static final String EMPTY_SQUARE = "##";

    /** Reads what the players type. */
    private final Scanner scanner;

    /** Turns typed text into moves or commands. */
    private final InputParser parser;

    /** The game being played; created once the players enter their names. */
    private Game game;

    /**
     * Creates the console interface.
     *
     * @param scanner where input is read from, usually {@code new Scanner(System.in)}
     */
    public ConsoleUI(Scanner scanner) {
        this.scanner = scanner;
        this.parser = new InputParser();
    }

    /**
     * Runs the program: asks for player names, then repeats the play loop
     * until a king is captured, the players quit, or input ends.
     */
    public void run() {
        System.out.println("=== Console Chess (simplified prototype) ===");
        System.out.println("Enter moves like E2 E4. Type help for commands, or quit to exit.");
        System.out.println();

        String whiteName = askName("White");
        String blackName = askName("Black");
        game = new Game(whiteName, blackName);

        boolean showBoard = true;
        while (true) {
            if (showBoard) {
                System.out.println();
                printBoard();
            }
            if (game.isOver()) {
                System.out.println("Game over. " + game.getWinner() + " wins!");
                System.out.print("Type restart to play again, or quit to exit: ");
            } else {
                System.out.print(game.getCurrentPlayer() + " to move: ");
            }
            if (!scanner.hasNextLine()) {
                System.out.println();
                break;
            }
            String input = scanner.nextLine();
            Command command = parser.getCommand(input);
            if (command == Command.QUIT) {
                break;
            }
            showBoard = handleInput(command, input);
        }
        System.out.println("Thanks for playing!");
    }

    /**
     * Carries out one command or move.
     *
     * @param command the command the player typed, or {@code MOVE}
     * @param input   the full text, used when it is a move
     * @return {@code true} if the board changed and should be shown again
     */
    private boolean handleInput(Command command, String input) {
        switch (command) {
            case HELP:
                printHelp();
                return false;
            case HISTORY:
                printHistory();
                return false;
            case CAPTURED:
                printAllCaptured();
                return false;
            case RESTART:
                game.start();
                System.out.println("New game started. White moves first.");
                return true;
            default:
                return handleMove(input);
        }
    }

    /**
     * Asks for one player's name.
     *
     * @param colorName "White" or "Black", used in the prompt
     * @return the typed name, or an empty string if none was entered
     */
    private String askName(String colorName) {
        System.out.print("Name for " + colorName + " (press Enter to skip): ");
        if (!scanner.hasNextLine()) {
            return "";
        }
        return scanner.nextLine();
    }

    /**
     * Parses one move, asks the game to make it, and prints the result.
     * Invalid input prints a message and leaves the game unchanged.
     *
     * @param input the text the player typed
     * @return {@code true} if the move was made and the board changed
     */
    private boolean handleMove(String input) {
        try {
            Position[] move = parser.parseMove(input);
            MoveResult result = game.tryMove(move[0], move[1]);
            if (result.isSuccess()) {
                System.out.println(result.getMessage());
                return true;
            }
            System.out.println("Invalid move: " + result.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
        }
        return false;
    }

    /** Prints the move format and the list of commands. */
    private void printHelp() {
        System.out.println("How to play:");
        System.out.println("  Type a move as FROM TO, for example E2 E4 (E2-E4 and e2e4 also work).");
        System.out.println("  Capture by moving onto an opponent's piece.");
        System.out.println("  A pawn that reaches the far rank becomes a queen.");
        System.out.println("  The game ends when a king is captured.");
        System.out.println("Commands:");
        System.out.println("  help      show this message");
        System.out.println("  history   list the moves made so far");
        System.out.println("  captured  show each side's captured pieces");
        System.out.println("  restart   start a new game with the same players");
        System.out.println("  quit      exit the program");
    }

    /** Prints every move made so far, or a note if there are none. */
    private void printHistory() {
        List<String> history = game.getMoveHistory();
        if (history.isEmpty()) {
            System.out.println("No moves yet.");
            return;
        }
        System.out.println("Moves so far:");
        for (String move : history) {
            System.out.println("  " + move);
        }
    }

    /** Prints both sides' captured pieces, including when a side has lost none. */
    private void printAllCaptured() {
        Board board = game.getBoard();
        for (PieceColor color : PieceColor.values()) {
            List<Piece> lost = board.getCapturedPieces(color);
            if (lost.isEmpty()) {
                System.out.println(color.displayName() + " pieces captured: none");
            } else {
                System.out.println(color.displayName() + " pieces captured: " + lost);
            }
        }
    }

    /** Prints the board with files across the top, ranks down the left, and captured pieces below. */
    private void printBoard() {
        Board board = game.getBoard();
        StringBuilder text = new StringBuilder("    ");
        for (int col = 0; col < Position.BOARD_SIZE; col++) {
            text.append(" ").append((char) ('A' + col)).append("  ");
        }
        System.out.println(text);

        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            Position rowStart = new Position(row, 0);
            StringBuilder line = new StringBuilder(" " + rowStart.getRank() + "  ");
            for (int col = 0; col < Position.BOARD_SIZE; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                if (piece == null) {
                    line.append(EMPTY_SQUARE);
                } else {
                    line.append(piece.getSymbol());
                }
                line.append("  ");
            }
            line.append(rowStart.getRank());
            System.out.println(line);
        }
        System.out.println(text);
        printCaptured(board, PieceColor.WHITE);
        printCaptured(board, PieceColor.BLACK);
    }

    /**
     * Prints the pieces one side has lost, if any.
     *
     * @param board the game board
     * @param color the side whose captured pieces to show
     */
    private void printCaptured(Board board, PieceColor color) {
        List<Piece> lost = board.getCapturedPieces(color);
        if (!lost.isEmpty()) {
            System.out.println(color.displayName() + " pieces captured: " + lost);
        }
    }
}
