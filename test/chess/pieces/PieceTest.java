package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * Tests each piece's movement pattern on an otherwise empty board, plus copying.
 */
public class PieceTest {

    /**
     * Shorthand for a square.
     *
     * @param square such as "E2"
     * @return the position
     */
    private static Position at(String square) {
        return Position.fromNotation(square);
    }

    /**
     * Places one piece on an empty board and counts its moves.
     *
     * @param piece the piece
     * @return how many squares it can move to
     */
    private static int movesAlone(Piece piece) {
        Board board = new Board();
        board.placePiece(piece);
        return piece.possibleMoves(board).size();
    }

    /** Move counts from the center and the corner. */
    @Test
    public void movementPatterns() {
        assertEquals(14, movesAlone(new Rook(PieceColor.WHITE, at("D4"))));
        assertEquals(13, movesAlone(new Bishop(PieceColor.WHITE, at("D4"))));
        assertEquals(7, movesAlone(new Bishop(PieceColor.WHITE, at("A1"))));
        assertEquals(27, movesAlone(new Queen(PieceColor.WHITE, at("D4"))));
        assertEquals(8, movesAlone(new Knight(PieceColor.WHITE, at("D4"))));
        assertEquals(2, movesAlone(new Knight(PieceColor.WHITE, at("A1"))));
        assertEquals(8, movesAlone(new King(PieceColor.WHITE, at("D4"))));
        assertEquals(3, movesAlone(new King(PieceColor.WHITE, at("H8"))));
    }

    /** Pawns move two from the start, one afterwards, and capture diagonally. */
    @Test
    public void pawnMoves() {
        assertEquals(2, movesAlone(new Pawn(PieceColor.WHITE, at("E2"))));
        assertEquals(2, movesAlone(new Pawn(PieceColor.BLACK, at("E7"))));
        Board board = new Board();
        Pawn pawn = new Pawn(PieceColor.WHITE, at("E3"));
        pawn.setMoved(true);
        board.placePiece(pawn);
        board.placePiece(new Knight(PieceColor.BLACK, at("D4")));
        board.placePiece(new Knight(PieceColor.BLACK, at("E4")));
        assertEquals(1, pawn.possibleMoves(board).size());
        assertTrue(pawn.canMoveTo(board, at("D4")));
        assertFalse(pawn.canMoveTo(board, at("E4")));
    }

    /** A copy is a separate object with the same color, square and moved flag. */
    @Test
    public void copyIsIndependent() {
        Piece rook = new Rook(PieceColor.BLACK, at("A8"));
        rook.move(at("A5"));
        Piece copy = rook.copy();
        assertNotSame(rook, copy);
        assertEquals("bR", copy.getSymbol());
        assertEquals(at("A5"), copy.getPosition());
        assertTrue(copy.hasMoved());
        copy.move(at("A1"));
        assertEquals(at("A5"), rook.getPosition());
    }

    /** Promotion choices build the right pieces. */
    @Test
    public void promotionPieces() {
        assertEquals("wN", PromotionPiece.KNIGHT.create(PieceColor.WHITE, at("B8")).getSymbol());
        assertEquals("bQ", PromotionPiece.fromLetter('q').create(PieceColor.BLACK, at("B1")).getSymbol());
    }

    /** The factory builds every piece type from its letter, in either case. */
    @Test
    public void factoryBuildsEachPiece() {
        String letters = "KQRBNP";
        String expected = "KQRBNp";
        for (int i = 0; i < letters.length(); i++) {
            Piece white = PieceFactory.create(letters.charAt(i), PieceColor.WHITE, at("D4"));
            Piece black = PieceFactory.create(Character.toLowerCase(letters.charAt(i)), PieceColor.BLACK, "D5");
            assertEquals("w" + expected.charAt(i), white.getSymbol());
            assertEquals("b" + expected.charAt(i), black.getSymbol());
            assertFalse(white.hasMoved());
        }
        assertTrue(PieceFactory.createMoved('P', PieceColor.WHITE, "E5").hasMoved());
    }

    /** The standard setup built by the factory matches the expected back rank. */
    @Test
    public void factorySetsUpBackRank() {
        Board board = new Board();
        board.initialize();
        StringBuilder rank = new StringBuilder();
        for (int col = 0; col < 8; col++) {
            rank.append(board.getPiece(new Position(7, col)).getTypeLetter());
        }
        assertEquals(PieceFactory.BACK_RANK, rank.toString());
    }

    /** An unknown letter is refused. */
    @Test(expected = IllegalArgumentException.class)
    public void factoryRejectsUnknownLetter() {
        PieceFactory.create('X', PieceColor.WHITE, at("A1"));
    }
}
