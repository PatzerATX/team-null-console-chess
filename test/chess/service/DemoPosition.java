package chess.service;

import chess.model.PieceColor;
import chess.model.Position;
import chess.pieces.Piece;
import chess.pieces.PieceFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Ready-made test positions for rules that take many moves to reach from the
 * start: stalemate, castling, en passant, promotion and checkmate. Each one is
 * loaded with {@link Game#loadPosition(List, PieceColor)}.
 */
public enum DemoPosition {

    /** White to move; G5 G6 leaves Black with no legal move and not in check. */
    STALEMATE("Stalemate in one", PieceColor.WHITE,
            "White to move. Play G5 G6: Black's king has no safe square and isn't in check."),

    /** Both sides can castle on either side. */
    CASTLING("Castling", PieceColor.WHITE,
            "Either side can castle. Move the king two squares: E1 G1 (kingside) or E1 C1 (queenside)."),

    /** Black's pawn can move two squares past White's pawn, which can then capture en passant. */
    EN_PASSANT("En passant", PieceColor.BLACK,
            "Black to move. Play D7 D5, then White captures en passant with E5 D6."),

    /** A white pawn is one step from promotion. */
    PROMOTION("Pawn promotion", PieceColor.WHITE,
            "White to move. Play B7 B8 and choose the piece to promote to."),

    /** Three moves from checkmate, as in the assignment's checkmate test. */
    CHECKMATE_SETUP("Checkmate in one", PieceColor.BLACK,
            "Black to move. Play D8 H4 for checkmate (the end of F2-F3, E7-E5, G2-G4, D8-H4).");

    /** A short name for the position. */
    private final String title;

    /** The side to move first. */
    private final PieceColor toMove;

    /** What to do with the position. */
    private final String instructions;

    /**
     * Creates a demo position entry.
     *
     * @param title        a short name
     * @param toMove       the side to move first
     * @param instructions what to play
     */
    DemoPosition(String title, PieceColor toMove, String instructions) {
        this.title = title;
        this.toMove = toMove;
        this.instructions = instructions;
    }

    /**
     * Builds the pieces for this position. A new list of new pieces is made
     * each time, so a demo can be loaded again after it's been played.
     *
     * @return the pieces to place on an empty board
     */
    public List<Piece> createPieces() {
        List<Piece> pieces = new ArrayList<>();
        PieceColor w = PieceColor.WHITE;
        PieceColor b = PieceColor.BLACK;
        if (this == STALEMATE) {
            pieces.add(PieceFactory.create('K', b, "H8"));
            pieces.add(PieceFactory.create('K', w, "F7"));
            pieces.add(PieceFactory.create('Q', w, "G5"));
        } else if (this == CASTLING) {
            for (PieceColor color : PieceColor.values()) {
                int back = ChessRules.lastRow(color.opposite());
                int pawnRow = back + color.getForwardDirection();
                pieces.add(PieceFactory.create('K', color, new Position(back, 4)));
                pieces.add(PieceFactory.create('R', color, new Position(back, 0)));
                pieces.add(PieceFactory.create('R', color, new Position(back, 7)));
                for (int col = 0; col < Position.BOARD_SIZE; col++) {
                    pieces.add(PieceFactory.create('P', color, new Position(pawnRow, col)));
                }
            }
        } else if (this == EN_PASSANT) {
            pieces.add(PieceFactory.create('K', w, "E1"));
            pieces.add(PieceFactory.create('K', b, "E8"));
            pieces.add(PieceFactory.createMoved('P', w, "E5"));
            pieces.add(PieceFactory.create('P', b, "D7"));
            pieces.add(PieceFactory.create('P', b, "H7"));
            pieces.add(PieceFactory.create('P', w, "H2"));
        } else if (this == PROMOTION) {
            pieces.add(PieceFactory.create('K', w, "E1"));
            pieces.add(PieceFactory.create('K', b, "H6"));
            pieces.add(PieceFactory.createMoved('P', w, "B7"));
            pieces.add(PieceFactory.create('N', b, "G8"));
        } else {
            pieces.addAll(checkmateSetup());
        }
        return pieces;
    }

    /**
     * Builds the position after F2-F3, E7-E5 and G2-G4.
     *
     * @return the pieces for the checkmate demo
     */
    private static List<Piece> checkmateSetup() {
        List<Piece> pieces = new ArrayList<>();
        PieceColor w = PieceColor.WHITE;
        PieceColor b = PieceColor.BLACK;
        for (int col = 0; col < Position.BOARD_SIZE; col++) {
            char letter = PieceFactory.BACK_RANK.charAt(col);
            pieces.add(PieceFactory.create(letter, w, new Position(7, col)));
            pieces.add(PieceFactory.create(letter, b, new Position(0, col)));
            if (col != 5 && col != 6) {
                pieces.add(PieceFactory.create('P', w, new Position(6, col)));
            }
            if (col != 4) {
                pieces.add(PieceFactory.create('P', b, new Position(1, col)));
            }
        }
        pieces.add(PieceFactory.createMoved('P', w, "F3"));
        pieces.add(PieceFactory.createMoved('P', w, "G4"));
        pieces.add(PieceFactory.createMoved('P', b, "E5"));
        return pieces;
    }

    /**
     * Returns the short name.
     *
     * @return the title, such as "Castling"
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the side to move first.
     *
     * @return {@code WHITE} or {@code BLACK}
     */
    public PieceColor getToMove() {
        return toMove;
    }

    /**
     * Returns what to play in this position.
     *
     * @return short instructions for the demo
     */
    public String getInstructions() {
        return instructions;
    }
}
