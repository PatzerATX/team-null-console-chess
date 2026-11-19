package chess.service;

/**
 * The state of the game from the point of view of the side about to move.
 */
public enum GameStatus {

    /** The game continues normally. */
    IN_PROGRESS,

    /** The side to move is in check but has a way out. */
    CHECK,

    /** The side to move is in check and has no legal move: the other side wins. */
    CHECKMATE,

    /** The side to move is not in check but has no legal move: the game is a draw. */
    STALEMATE;

    /**
     * Checks whether this status ends the game.
     *
     * @return {@code true} for checkmate or stalemate
     */
    public boolean isGameOver() {
        return this == CHECKMATE || this == STALEMATE;
    }
}
