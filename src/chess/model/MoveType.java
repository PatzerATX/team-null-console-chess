package chess.model;

/**
 * The kinds of moves in chess. Most moves are {@link #NORMAL}; the others need
 * extra handling when the move is carried out.
 */
public enum MoveType {

    /** An ordinary move or capture, including pawn promotion. */
    NORMAL,

    /** Castling toward the king's side: the king moves two squares toward the H-file rook. */
    CASTLE_KINGSIDE,

    /** Castling toward the queen's side: the king moves two squares toward the A-file rook. */
    CASTLE_QUEENSIDE,

    /** A pawn capturing an enemy pawn that just moved two squares and passed it. */
    EN_PASSANT;

    /**
     * Checks whether this is either kind of castling.
     *
     * @return {@code true} for kingside or queenside castling
     */
    public boolean isCastle() {
        return this == CASTLE_KINGSIDE || this == CASTLE_QUEENSIDE;
    }
}
