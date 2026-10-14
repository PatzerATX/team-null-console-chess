package chess.console;

/**
 * The things a player can type at the move prompt besides a move.
 * {@link #MOVE} means the input should be read as a move such as {@code E2 E4}.
 */
public enum Command {

    /** A move such as {@code E2 E4}. */
    MOVE,

    /** Show the move format and the list of commands. */
    HELP,

    /** List every move made so far. */
    HISTORY,

    /** Show each side's captured pieces. */
    CAPTURED,

    /** Start a new game with the same players. */
    RESTART,

    /** Leave the program. */
    QUIT
}
