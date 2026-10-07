package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;

import java.util.List;

/**
 * A queen, which moves any number of squares in a straight line or diagonally,
 * combining the rook's and bishop's movement. Its path must be clear.
 */
public class Queen extends Piece {

    /** All eight directions, as {rowChange, colChange}. */
    private static final int[][] DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    /**
     * Creates a queen.
     *
     * @param color    the side the queen belongs to
     * @param position the square it starts on
     */
    public Queen(PieceColor color, Position position) {
        super(color, position);
    }

    /**
     * Returns every square the queen can reach in all eight directions.
     *
     * @param board the current board
     * @return reachable squares in straight lines and diagonals
     */
    @Override
    public List<Position> possibleMoves(Board board) {
        return slide(board, DIRECTIONS);
    }

    /**
     * Returns the queen's letter.
     *
     * @return {@code 'Q'}
     */
    @Override
    protected char getLetter() {
        return 'Q';
    }
}
