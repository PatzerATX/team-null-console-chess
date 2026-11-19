package chess.service;

import chess.model.Move;
import chess.model.Player;
import chess.model.Position;

/**
 * Something that wants to be told when the game changes: the GUI panels, the
 * logger and so on. This is the <b>Observer pattern</b>.
 *
 * <p>{@link Game} keeps a list of listeners and calls these methods after each
 * change, without knowing what the listeners are. That lets the back end stay
 * independent of Swing: the GUI redraws itself when told to, and the game never
 * calls GUI code directly.</p>
 *
 * <p>Every method has an empty default, so a listener only overrides the
 * events it cares about.</p>
 */
public interface GameListener {

    /**
     * Called when a new game starts, the game restarts, or a position is loaded.
     */
    default void onGameStarted() {
    }

    /**
     * Called after a legal move has been carried out.
     *
     * @param move   the move that was played
     * @param status the game's status for the side now to move
     */
    default void onMoveMade(Move move, GameStatus status) {
    }

    /**
     * Called when a move is rejected. The board and turn are unchanged.
     *
     * @param from   the starting square that was tried
     * @param to     the destination that was tried
     * @param reason why the move was rejected
     */
    default void onMoveRejected(Position from, Position to, String reason) {
    }

    /**
     * Called when the game ends by checkmate or stalemate.
     *
     * @param result checkmate or stalemate
     * @param winner the winning player, or {@code null} for a draw
     */
    default void onGameOver(GameStatus result, Player winner) {
    }
}
