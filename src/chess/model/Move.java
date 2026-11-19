package chess.model;

import chess.pieces.Piece;
import chess.pieces.PromotionPiece;

/**
 * One chess move: which piece moved, where from and to, what it captured, and
 * what kind of move it was.
 *
 * <p>Moves are immutable. In Phase 1 the move history was a list of text; a
 * {@code Move} object can also be read back by the program, which en passant
 * (it needs the previous move), the computer opponent and the logger rely on.</p>
 */
public final class Move {

    /** The square the piece moved from. */
    private final Position from;

    /** The square the piece moved to. */
    private final Position to;

    /** The piece that moved, as it was before the move. */
    private final Piece piece;

    /** The piece captured by this move, or {@code null} if nothing was captured. */
    private final Piece captured;

    /** What kind of move this is. */
    private final MoveType type;

    /** The piece a pawn is promoted to, or {@code null} if this isn't a promotion. */
    private final PromotionPiece promotion;

    /** Text added to the notation after the move is played: "+" for check, "#" for checkmate. */
    private final String suffix;

    /**
     * Creates a move.
     *
     * @param from      the starting square
     * @param to        the destination square
     * @param piece     the piece that moves
     * @param captured  the piece captured, or {@code null}
     * @param type      the kind of move
     * @param promotion the promotion choice, or {@code null}
     * @throws IllegalArgumentException if from, to, piece or type is null
     */
    public Move(Position from, Position to, Piece piece, Piece captured,
                MoveType type, PromotionPiece promotion) {
        this(from, to, piece, captured, type, promotion, "");
    }

    /**
     * Creates a move with a notation suffix. Used by {@link #withSuffix(String)}.
     *
     * @param from      the starting square
     * @param to        the destination square
     * @param piece     the piece that moves
     * @param captured  the piece captured, or {@code null}
     * @param type      the kind of move
     * @param promotion the promotion choice, or {@code null}
     * @param suffix    "+", "#" or ""
     */
    private Move(Position from, Position to, Piece piece, Piece captured,
                 MoveType type, PromotionPiece promotion, String suffix) {
        if (from == null || to == null || piece == null || type == null) {
            throw new IllegalArgumentException("A move needs a start, a destination, a piece and a type.");
        }
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.captured = captured;
        this.type = type;
        this.promotion = promotion;
        this.suffix = suffix;
    }

    /**
     * Returns a copy of this move with a different promotion choice.
     *
     * @param choice the piece to promote to
     * @return the same move, promoting to {@code choice}
     */
    public Move withPromotion(PromotionPiece choice) {
        return new Move(from, to, piece, captured, type, choice, suffix);
    }

    /**
     * Returns a copy of this move with a check or checkmate mark for its notation.
     *
     * @param newSuffix "+" for check, "#" for checkmate, or ""
     * @return the same move with the new suffix
     */
    public Move withSuffix(String newSuffix) {
        return new Move(from, to, piece, captured, type, promotion, newSuffix);
    }

    /**
     * Checks whether this move goes between the given squares.
     *
     * @param start the starting square
     * @param end   the destination square
     * @return {@code true} if both squares match
     */
    public boolean matches(Position start, Position end) {
        return from.equals(start) && to.equals(end);
    }

    /**
     * Checks whether this move is a pawn moving two squares forward,
     * which makes an en passant reply possible.
     *
     * @return {@code true} for a pawn's two-square advance
     */
    public boolean isPawnDoubleStep() {
        return piece.isPawn() && Math.abs(to.getRow() - from.getRow()) == 2;
    }

    /**
     * Returns the starting square.
     *
     * @return where the piece moved from
     */
    public Position getFrom() {
        return from;
    }

    /**
     * Returns the destination square.
     *
     * @return where the piece moved to
     */
    public Position getTo() {
        return to;
    }

    /**
     * Returns the piece that moved.
     *
     * @return the moving piece
     */
    public Piece getPiece() {
        return piece;
    }

    /**
     * Returns the captured piece.
     *
     * @return the piece taken, or {@code null} if nothing was captured
     */
    public Piece getCaptured() {
        return captured;
    }

    /**
     * Checks whether this move captures a piece.
     *
     * @return {@code true} if a piece is captured
     */
    public boolean isCapture() {
        return captured != null;
    }

    /**
     * Returns the kind of move.
     *
     * @return normal, castling or en passant
     */
    public MoveType getType() {
        return type;
    }

    /**
     * Returns the promotion choice.
     *
     * @return the piece a pawn becomes, or {@code null} if this isn't a promotion
     */
    public PromotionPiece getPromotion() {
        return promotion;
    }

    /**
     * Checks whether this move promotes a pawn.
     *
     * @return {@code true} if a promotion choice is set
     */
    public boolean isPromotion() {
        return promotion != null;
    }

    /**
     * Returns the move in readable chess notation, such as {@code e2-e4},
     * {@code Ng1-f3}, {@code e4xd5}, {@code O-O} or {@code e7-e8=Q+}.
     *
     * @return the move's notation
     */
    public String toNotation() {
        String text;
        if (type == MoveType.CASTLE_KINGSIDE) {
            text = "O-O";
        } else if (type == MoveType.CASTLE_QUEENSIDE) {
            text = "O-O-O";
        } else {
            String letter = "";
            if (!piece.isPawn()) {
                letter = String.valueOf(piece.getTypeLetter());
            }
            String separator = "-";
            if (isCapture()) {
                separator = "x";
            }
            text = letter + from.toString().toLowerCase() + separator + to.toString().toLowerCase();
            if (promotion != null) {
                text += "=" + promotion.getLetter();
            }
            if (type == MoveType.EN_PASSANT) {
                text += " e.p.";
            }
        }
        return text + suffix;
    }

    /**
     * Describes the move in the same style as the Phase 1 move history, such
     * as "White pawn E2 - E4", "White knight F3 x E5" or "White pawn A7 - A8 =Q".
     * The rules added in Phase 2 follow the same style: "White castles
     * kingside", "... en passant", and "check" or "checkmate" at the end.
     *
     * @return a readable description of the move
     */
    public String toDescription() {
        String side = piece.getColor().displayName();
        String text;
        if (type == MoveType.CASTLE_KINGSIDE) {
            text = side + " castles kingside";
        } else if (type == MoveType.CASTLE_QUEENSIDE) {
            text = side + " castles queenside";
        } else {
            String separator = " - ";
            if (isCapture()) {
                separator = " x ";
            }
            text = side + " " + piece.getName().toLowerCase() + " " + from + separator + to;
            if (promotion != null) {
                text += " =" + promotion.getLetter();
            }
            if (type == MoveType.EN_PASSANT) {
                text += " en passant";
            }
        }
        if (suffix.equals("#")) {
            text += " checkmate";
        } else if (suffix.equals("+")) {
            text += " check";
        }
        return text;
    }

    /**
     * Returns the move's notation.
     *
     * @return the same text as {@link #toNotation()}
     */
    @Override
    public String toString() {
        return toNotation();
    }
}
