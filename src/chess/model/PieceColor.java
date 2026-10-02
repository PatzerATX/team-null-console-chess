package chess.model;

/**
 * The two players in a chess game.
 *
 * <p>Each color knows the one-letter prefix used to display any of its pieces
 * (for example, {@code w} in {@code wp}) and the direction its pawns move
 * on the board array.</p>
 */
public enum PieceColor {

    /** The white side */
    WHITE('w', -1),

    /** The black side */
    BLACK('b', 1);

    /** The letter shown before a piece's symbol on the board. */
    private final char prefix;

    /**
     * The row change for one step "forward". White moves toward rank 8,
     * which is array row 0, so its direction is -1. Black's is +1 since
     * it's "forward" is the opposite direction.
     */
    private final int forwardDirection;

    /**
     * Creates a color with its display prefix and forward direction value.
     *
     * @param prefix           the letter shown before piece symbols
     * @param forwardDirection the row change for one step forward
     */
    PieceColor(char prefix, int forwardDirection) {
        this.prefix = prefix;
        this.forwardDirection = forwardDirection;
    }

    /**
     * Returns the piece color prefix.
     *
     * @return {@code 'w'} for white or {@code 'b'} for black
     */
    public char getPrefix() {
        return prefix;
    }

    /**
     * Returns the row change for one step forward for this color.
     *
     * @return -1 for white, +1 for black
     */
    public int getForwardDirection() {
        return forwardDirection;
    }

    /**
     * Returns the other color.
     *
     * @return {@code BLACK} if this is {@code WHITE}, otherwise {@code WHITE}
     */
    public PieceColor opposite() {
        if (this == WHITE) {
            return BLACK;
        } else {
            return WHITE;
        }
    }

    /**
     * Returns the color's name with only the first letter capitalized.
     *
     * @return "White" or "Black"
     */
    public String displayName() {
        if (this == WHITE) {
            return "White";
        } else {
            return "Black";
        }
    }
}
