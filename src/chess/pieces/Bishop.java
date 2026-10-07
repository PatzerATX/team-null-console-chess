package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;

import java.util.List;

/**
 * A bishop, which moves any number of squares diagonally.
 * Its path must be clear, and it can capture the first enemy piece in its way.
 */
public class Bishop extends Piece {

    /** The four diagonal directions, as {rowChange, colChange}. */
    private static final int[][] DIRECTIONS = {
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    /**
     * Creates a bishop.
     *
     * @param color    the side the bishop belongs to
     * @param position the square it starts on
     */
    public Bishop(PieceColor color, Position position) {
        super(color, position);
    }

    /**
     * Returns every square the bishop can reach diagonally.
     *
     * @param board the current board
     * @return reachable squares on all four diagonals
     */
    @Override
    public List<Position> possibleMoves(Board board) {
        return slide(board, DIRECTIONS);
    }

    /**
     * Returns the bishop's letter.
     *
     * @return {@code 'B'}
     */
    @Override
    protected char getLetter() {
        return 'B';
    }
}
