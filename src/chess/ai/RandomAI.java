package chess.ai;

import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;
import chess.service.ChessRules;

import java.util.List;
import java.util.Random;

/**
 * The Easy level: picks any legal move at random.
 */
public class RandomAI implements MoveStrategy {

    /** Source of random choices; a fixed seed makes tests repeatable. */
    private final Random random;

    /**
     * Creates a random player.
     *
     * @param random the random number generator to use
     */
    public RandomAI(Random random) {
        this.random = random;
    }

    /**
     * Picks a random legal move.
     *
     * @param board    the board
     * @param side     the side to move
     * @param lastMove the previous move, or {@code null}
     * @return a random legal move, or {@code null} if there are none
     */
    @Override
    public Move chooseMove(Board board, PieceColor side, Move lastMove) {
        List<Move> moves = ChessRules.legalMoves(board, side, lastMove);
        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(random.nextInt(moves.size()));
    }

    /**
     * Returns the menu name.
     *
     * @return "Easy (random moves)"
     */
    @Override
    public String getName() {
        return "Easy (random moves)";
    }
}
