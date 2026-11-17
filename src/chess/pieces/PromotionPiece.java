package chess.pieces;

import chess.model.PieceColor;
import chess.model.Position;

/**
 * The four pieces a pawn can be promoted to when it reaches the far rank.
 *
 * <p>Each value knows its display name and its notation letter. The piece
 * itself is built by {@link PieceFactory}.</p>
 */
public enum PromotionPiece {

    /** Promote to a queen, the usual choice. */
    QUEEN("Queen", 'Q'),

    /** Promote to a rook. */
    ROOK("Rook", 'R'),

    /** Promote to a bishop. */
    BISHOP("Bishop", 'B'),

    /** Promote to a knight. */
    KNIGHT("Knight", 'N');

    /** The name shown to the player, such as "Queen". */
    private final String displayName;

    /** The letter used in move notation, such as 'Q'. */
    private final char letter;

    /**
     * Creates a promotion choice.
     *
     * @param displayName the name shown to the player
     * @param letter      the notation letter
     */
    PromotionPiece(String displayName, char letter) {
        this.displayName = displayName;
        this.letter = letter;
    }

    /**
     * Creates the promoted piece.
     *
     * @param color    the side the new piece belongs to
     * @param position the square it stands on
     * @return a new queen, rook, bishop or knight
     */
    public Piece create(PieceColor color, Position position) {
        Piece piece = PieceFactory.create(letter, color, position);
        piece.setMoved(true);
        return piece;
    }

    /**
     * Returns the name shown to the player.
     *
     * @return "Queen", "Rook", "Bishop" or "Knight"
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the notation letter.
     *
     * @return 'Q', 'R', 'B' or 'N'
     */
    public char getLetter() {
        return letter;
    }

    /**
     * Finds the promotion choice for a letter, ignoring case.
     *
     * @param letter 'Q', 'R', 'B' or 'N' (upper- or lowercase)
     * @return the matching choice
     * @throws IllegalArgumentException if the letter isn't one of the four
     */
    public static PromotionPiece fromLetter(char letter) {
        char upper = Character.toUpperCase(letter);
        for (PromotionPiece choice : values()) {
            if (choice.letter == upper) {
                return choice;
            }
        }
        throw new IllegalArgumentException(
                "\"" + letter + "\" is not a promotion choice. Use Q, R, B or N.");
    }
}
