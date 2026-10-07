package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;

import java.util.List;

/**
 * A rook, which moves any number of squares horizontally or vertically.
 * Its path must be clear, and it can capture the first enemy piece in its way.
 */
public class Rook extends Piece {

    /** Up, down, left and right, as {rowChange, colChange}. */
    private static final int[][] DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };

    /**
     * Creates a rook.
     *
     * @param color    the side the rook belongs to
     * @param position the square it starts on
     */
    public Rook(PieceColor color, Position position) {
        super(color, position);
    }

    /**
     * Returns every square the rook can reach in straight lines.
     *
     * @param board the current board
     * @return reachable squares up, down, left and right
     */
    @Override
    public List<Position> possibleMoves(Board board) {
        return slide(board, DIRECTIONS);
    }

    /**
     * Returns the rook's letter.
     *
     * @return {@code 'R'}
     */
    @Override
    protected char getLetter() {
        return 'R';
    }
}
