package chess.logging;

import chess.model.Move;
import chess.model.Player;
import chess.model.Position;
import chess.service.GameListener;
import chess.service.GameStatus;

import java.util.logging.Logger;

/**
 * Writes game events to the log: game start, each move, rejected moves and
 * the result. It's just another {@link GameListener}, so the game doesn't know
 * it is being logged; this is the Observer pattern again.
 */
public class GameLogger implements GameListener {

    /** Where the messages go. */
    private static final Logger LOG = Logger.getLogger(LoggingSetup.ROOT_LOGGER + ".game");

    /** Counts moves in the current game for numbered log lines. */
    private int moveNumber;

    /** Logs the start of a game. */
    @Override
    public void onGameStarted() {
        moveNumber = 0;
        LOG.info("New game started.");
    }

    /**
     * Logs a move in notation, with check if there is one.
     *
     * @param move   the move played
     * @param status the status afterwards
     */
    @Override
    public void onMoveMade(Move move, GameStatus status) {
        moveNumber++;
        LOG.info("Move " + moveNumber + ": " + move.getPiece().getColor().displayName()
                + " " + move.toNotation());
    }

    /**
     * Logs a rejected move and the reason.
     *
     * @param from   the start square tried
     * @param to     the destination tried
     * @param reason why it was rejected
     */
    @Override
    public void onMoveRejected(Position from, Position to, String reason) {
        LOG.info("Rejected " + from + " to " + to + ": " + reason);
    }

    /**
     * Logs the result.
     *
     * @param result checkmate or stalemate
     * @param winner the winner, or {@code null} for a draw
     */
    @Override
    public void onGameOver(GameStatus result, Player winner) {
        if (winner == null) {
            LOG.info("Game over: stalemate, a draw.");
        } else {
            LOG.info("Game over: checkmate. " + winner.getName() + " wins with " + winner.getColor().displayName() + ".");
        }
    }
}
