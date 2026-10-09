package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;

import java.util.List;

/**
 * A knight, which moves in an L shape: two squares in one direction and one
 * square to the side. It is the only piece that can jump over other pieces.
 */
public class Knight extends Piece {

    /** The eight L-shaped jumps, as {rowChange, colChange}. */
    private static final int[][] JUMPS = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
    };

    /**
     * Creates a knight.
     *
     * @param color    the side the knight belongs to
     * @param position the square it starts on
     */
    public Knight(PieceColor color, Position position) {
        super(color, position);
    }

    /**
     * Returns every square the knight can jump to. Pieces in between don't
     * matter; only the landing square must be empty or hold an enemy.
     *
     * @param board the current board
     * @return reachable L-shaped destinations
     */
    @Override
    public List<Position> possibleMoves(Board board) {
        return step(board, JUMPS);
    }

    /**
     * Returns the knight's letter. Chess uses N because K is the king.
     *
     * @return {@code 'N'}
     */
    @Override
    protected char getLetter() {
        return 'N';
    }
}
