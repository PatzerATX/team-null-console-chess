package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;

import java.util.List;

/**
 * A king, which moves exactly one square in any direction.
 *
 * <p>This simplified version does not check whether a move puts the king
 * in check, and castling is not supported.</p>
 */
public class King extends Piece {

    /** The eight neighboring squares, as {rowChange, colChange}. */
    private static final int[][] NEIGHBORS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1}, {0, 1},
            {1, -1}, {1, 0}, {1, 1}
    };

    /**
     * Creates a king.
     *
     * @param color    the side the king belongs to
     * @param position the square it starts on
     */
    public King(PieceColor color, Position position) {
        super(color, position);
    }

    /**
     * Returns every neighboring square the king can move to.
     *
     * @param board the current board
     * @return reachable squares one step away
     */
    @Override
    public List<Position> possibleMoves(Board board) {
        return step(board, NEIGHBORS);
    }

    /**
     * Returns the king's letter.
     *
     * @return {@code 'K'}
     */
    @Override
    protected char getLetter() {
        return 'K';
    }
}
