package chess.ai;

import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;
import chess.service.ChessRules;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wraps another strategy and checks its answer. If the inner strategy throws
 * an exception, returns nothing, or returns an illegal move, this logs a
 * warning and plays a random legal move instead, so the game can always
 * continue. This is the <b>Decorator pattern</b>: it has the same interface as
 * the strategy it wraps and adds behavior around it.
 */
public class SafeMoveSelector implements MoveStrategy {

    /** Logger for warnings about bad AI answers. */
    private static final Logger LOG = Logger.getLogger(SafeMoveSelector.class.getName());

    /** The strategy being checked. */
    private final MoveStrategy inner;

    /** Used for the fallback move. */
    private final Random random;

    /**
     * Wraps a strategy.
     *
     * @param inner  the strategy to check
     * @param random random number generator for the fallback
     */
    public SafeMoveSelector(MoveStrategy inner, Random random) {
        this.inner = inner;
        this.random = random;
    }

    /**
     * Asks the inner strategy for a move and checks it is one of the legal moves.
     *
     * @param board    a copy of the board
     * @param side     the side to move
     * @param lastMove the previous move, or {@code null}
     * @return a legal move, or {@code null} only if there are none
     */
    @Override
    public Move chooseMove(Board board, PieceColor side, Move lastMove) {
        List<Move> legal = ChessRules.legalMoves(board, side, lastMove);
        if (legal.isEmpty()) {
            return null;
        }
        Move chosen = null;
        try {
            chosen = inner.chooseMove(board.copy(), side, lastMove);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, inner.getName() + " failed; playing a random move.", e);
        }
        if (chosen != null) {
            for (Move move : legal) {
                if (move.matches(chosen.getFrom(), chosen.getTo())) {
                    return withSamePromotion(move, chosen);
                }
            }
            LOG.warning(inner.getName() + " chose an illegal move (" + chosen + "); playing a random move.");
        } else {
            LOG.warning(inner.getName() + " returned no move; playing a random move.");
        }
        return legal.get(random.nextInt(legal.size()));
    }

    /**
     * Keeps the promotion piece the inner strategy picked, if any.
     *
     * @param legal  the matching legal move
     * @param chosen the strategy's move
     * @return the legal move with the chosen promotion
     */
    private static Move withSamePromotion(Move legal, Move chosen) {
        if (legal.isPromotion() && chosen.isPromotion()) {
            return legal.withPromotion(chosen.getPromotion());
        }
        return legal;
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
