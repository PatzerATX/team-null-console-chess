package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;

import java.util.List;

/**
 * A king, which moves exactly one square in any direction.
 *
 * <p>This class only knows the king's basic one-square moves. Moving into
 * check and castling are handled by {@code ChessRules}, because they depend
 * on the whole board and on whether the king and rook have moved.</p>
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

    /**
     * Returns the king's value for the computer opponent.
     *
     * @return {@code 0} centipawns
     */
    @Override
    public int getValue() {
        return 0;
    }

    /**
     * Creates a new king of the same color on the same square.
     *
     * @return a fresh King
     */
    @Override
    protected Piece createCopy() {
        return new King(getColor(), getPosition());
    }

    /**
     * Identifies this piece as a king.
     *
     * @return {@code true}
     */
    @Override
    public boolean isKing() {
        return true;
    }
}
