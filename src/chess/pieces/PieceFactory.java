package chess.pieces;

import chess.model.PieceColor;
import chess.model.Position;

/**
 * Creates pieces from their one-letter codes. This is the <b>Simple Factory</b>
 * pattern.
 *
 * <p>Before this class, the "which class matches this letter?" decision was
 * written out four times: setting up the board, building demo positions,
 * promoting a pawn, and building test positions. Now there is one place to
 * create a piece, so adding a new piece type, or fixing how one is created,
 * means changing only this class.</p>
 */
public final class PieceFactory {

    /** The back rank from file A to H, as letters. */
    public static final String BACK_RANK = "RNBQKBNR";

    /** Prevents creating objects; this class only holds static methods. */
    private PieceFactory() {
    }

    /**
     * Creates a piece from its letter.
     *
     * @param letter   'K', 'Q', 'R', 'B', 'N' or 'P' (upper- or lowercase)
     * @param color    the piece's color
     * @param position the square it starts on
     * @return the new piece
     * @throws IllegalArgumentException if the letter isn't a piece
     */
    public static Piece create(char letter, PieceColor color, Position position) {
        switch (Character.toUpperCase(letter)) {
            case 'K':
                return new King(color, position);
            case 'Q':
                return new Queen(color, position);
            case 'R':
                return new Rook(color, position);
            case 'B':
                return new Bishop(color, position);
            case 'N':
                return new Knight(color, position);
            case 'P':
                return new Pawn(color, position);
            default:
                throw new IllegalArgumentException("'" + letter + "' is not a piece letter. Use K, Q, R, B, N or P.");
        }
    }

    /**
     * Creates a piece on a square given in chess notation, such as "E1".
     *
     * @param letter the piece letter
     * @param color  the piece's color
     * @param square the square, such as "E1"
     * @return the new piece
     */
    public static Piece create(char letter, PieceColor color, String square) {
        return create(letter, color, Position.fromNotation(square));
    }

    /**
     * Creates a piece that counts as having already moved, for positions set
     * up partway through a game (a pawn that has moved can't jump two squares,
     * and a king or rook that has moved can't castle).
     *
     * @param letter the piece letter
     * @param color  the piece's color
     * @param square the square, such as "E5"
     * @return the new piece, marked as moved
     */
    public static Piece createMoved(char letter, PieceColor color, String square) {
        Piece piece = create(letter, color, square);
        piece.setMoved(true);
        return piece;
    }
}
