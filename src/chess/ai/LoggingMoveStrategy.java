package chess.ai;

import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;

import java.util.logging.Logger;

/**
 * Wraps another strategy and logs each move it chooses and how long it took.
 * This is a second <b>Decorator</b>: like {@link SafeMoveSelector}, it has
 * the same interface as the strategy it wraps, passes the request through,
 * and adds its own behavior afterwards.
 *
 * <p>Decorators can be stacked, like the condiments wrapped around a coffee in
 * the class example. The computer player is built as
 * {@code Logging(Safe(Minimax))}: the outer layer logs, the middle layer makes
 * sure the move is legal, and the inner object chooses it. None of them knows
 * how many layers there are.</p>
 */
public class LoggingMoveStrategy implements MoveStrategy {

    /** Where the messages go. */
    private static final Logger LOG = Logger.getLogger("chess.ai");

    /** The strategy being logged. */
    private final MoveStrategy inner;

    /**
     * Wraps a strategy.
     *
     * @param inner the strategy whose moves should be logged
     */
    public LoggingMoveStrategy(MoveStrategy inner) {
        this.inner = inner;
    }

    /**
     * Asks the wrapped strategy for a move, then logs the move and the time taken.
     *
     * @param board    a copy of the board
     * @param side     the side to move
     * @param lastMove the previous move, or {@code null}
     * @return whatever the wrapped strategy returned
     */
    @Override
    public Move chooseMove(Board board, PieceColor side, Move lastMove) {
        long start = System.currentTimeMillis();
        Move move = inner.chooseMove(board, side, lastMove);
        long elapsed = System.currentTimeMillis() - start;
        if (move == null) {
            LOG.info(inner.getName() + " (" + side.displayName() + ") has no legal moves.");
        } else {
            LOG.info(inner.getName() + " (" + side.displayName() + ") chose " + move + " in " + elapsed + " ms");
        }
        return move;
    }

    /**
     * Returns the wrapped strategy's name.
     *
     * @return the inner name
     */
    @Override
    public String getName() {
        return inner.getName();
    }
}
