package chess.ai;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;
import chess.pieces.Piece;

/**
 * Scores a position so the computer can compare moves. A positive score is
 * good for the side asked about.
 *
 * <p>The score is material (each piece's {@link Piece#getValue()}) plus a small
 * bonus or penalty for where each piece stands, from a "piece-square table".
 * The tables encode simple advice: knights belong in the center, pawns should
 * advance, the king should stay tucked away behind its pawns.</p>
 */
public final class BoardEvaluator {

    /** Pawn bonuses, from White's side (row 0 is rank 8): advance and take the center. */
    private static final int[][] PAWN_TABLE = {
        {0, 0, 0, 0, 0, 0, 0, 0},
        {50, 50, 50, 50, 50, 50, 50, 50},
        {10, 10, 20, 30, 30, 20, 10, 10},
        {5, 5, 10, 25, 25, 10, 5, 5},
        {0, 0, 0, 20, 20, 0, 0, 0},
        {5, -5, -10, 0, 0, -10, -5, 5},
        {5, 10, 10, -20, -20, 10, 10, 5},
        {0, 0, 0, 0, 0, 0, 0, 0}
    };

    /** Knight bonuses: strong in the center, weak on the edge. */
    private static final int[][] KNIGHT_TABLE = {
        {-50, -40, -30, -30, -30, -30, -40, -50},
        {-40, -20, 0, 0, 0, 0, -20, -40},
        {-30, 0, 10, 15, 15, 10, 0, -30},
        {-30, 5, 15, 20, 20, 15, 5, -30},
        {-30, 0, 15, 20, 20, 15, 0, -30},
        {-30, 5, 10, 15, 15, 10, 5, -30},
        {-40, -20, 0, 5, 5, 0, -20, -40},
        {-50, -40, -30, -30, -30, -30, -40, -50}
    };

    /** Bishop bonuses: long diagonals, away from corners. */
    private static final int[][] BISHOP_TABLE = {
        {-20, -10, -10, -10, -10, -10, -10, -20},
        {-10, 0, 0, 0, 0, 0, 0, -10},
        {-10, 0, 5, 10, 10, 5, 0, -10},
        {-10, 5, 5, 10, 10, 5, 5, -10},
        {-10, 0, 10, 10, 10, 10, 0, -10},
        {-10, 10, 10, 10, 10, 10, 10, -10},
        {-10, 5, 0, 0, 0, 0, 5, -10},
        {-20, -10, -10, -10, -10, -10, -10, -20}
    };

    /** King bonuses: stay on the back rank, ideally castled. */
    private static final int[][] KING_TABLE = {
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-20, -30, -30, -40, -40, -30, -30, -20},
        {-10, -20, -20, -20, -20, -20, -20, -10},
        {20, 20, 0, 0, 0, 0, 20, 20},
        {20, 30, 10, 0, 0, 10, 30, 20}
    };

    /** Prevents creating objects; this class only holds static methods. */
    private BoardEvaluator() {
    }

    /**
     * Scores the board for one side.
     *
     * @param board the board
     * @param side  the side the score is for
     * @return positive if {@code side} is better off, negative if worse
     */
    public static int evaluate(Board board, PieceColor side) {
        return scoreFor(board, side) - scoreFor(board, side.opposite());
    }

    /**
     * Adds up material and position bonuses for one color.
     *
     * @param board the board
     * @param color the color to total
     * @return that color's score
     */
    private static int scoreFor(Board board, PieceColor color) {
        int total = 0;
        for (Piece piece : board.getPieces(color)) {
            total += piece.getValue() + positionBonus(piece);
        }
        return total;
    }

    /**
     * Looks up a piece's bonus for its square. Black's tables are White's
     * flipped top to bottom.
     *
     * @param piece the piece
     * @return the bonus (may be negative)
     */
    static int positionBonus(Piece piece) {
        Position at = piece.getPosition();
        int row = at.getRow();
        if (piece.getColor() == PieceColor.BLACK) {
            row = Position.BOARD_SIZE - 1 - row;
        }
        int[][] table = tableFor(piece.getTypeLetter());
        if (table == null) {
            return 0;
        }
        return table[row][at.getCol()];
    }

    /**
     * Picks the table for a piece type. Rooks and queens have none.
     *
     * @param letter the piece's type letter, such as 'N'
     * @return the table, or {@code null}
     */
    private static int[][] tableFor(char letter) {
        switch (letter) {
            case 'p':
                return PAWN_TABLE;
            case 'N':
                return KNIGHT_TABLE;
            case 'B':
                return BISHOP_TABLE;
            case 'K':
                return KING_TABLE;
            default:
                return null;
        }
    }
}
