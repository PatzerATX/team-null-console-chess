package chess.service;

/**
 * The outcome of trying a move: whether it worked, and a message explaining
 * what happened or why it was rejected.
 *
 * <p>{@code Game} returns a {@code MoveResult} instead of printing anything,
 * so the console (and later the GUI) decides how to show the message.</p>
 */
public final class MoveResult {

    /** {@code true} if the move was carried out. */
    private final boolean success;

    /** A message for the player, such as "White pawn moves E2 to E4." */
    private final String message;

    /**
     * Creates a result. Use {@link #success(String)} or {@link #failure(String)} instead.
     *
     * @param success whether the move was carried out
     * @param message the message to show
     */
    private MoveResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Creates a result for a move that was carried out.
     *
     * @param message what happened
     * @return a successful result
     */
    public static MoveResult success(String message) {
        return new MoveResult(true, message);
    }

    /**
     * Creates a result for a move that was rejected. The board and turn are unchanged.
     *
     * @param message why the move was rejected
     * @return a failed result
     */
    public static MoveResult failure(String message) {
        return new MoveResult(false, message);
    }

    /**
     * Returns whether the move was carried out.
     *
     * @return {@code true} if successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the message for the player.
     *
     * @return what happened, or why the move was rejected
     */
    public String getMessage() {
        return message;
    }
}
