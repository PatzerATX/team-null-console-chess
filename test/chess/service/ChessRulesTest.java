package chess.service;

import chess.TestPositions;
import chess.model.Board;
import chess.model.Move;
import chess.model.MoveType;
import chess.model.PieceColor;
import chess.model.Position;
import chess.pieces.Piece;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the rules engine: legal moves, king safety,
 * castling, en passant, promotion, checkmate and stalemate.
 */
public class ChessRulesTest {

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
     * Finds the legal move between two squares in a game.
     *
     * @param game the game
     * @param from the start square
     * @param to   the destination
     * @return the move, or {@code null} if it isn't legal
     */
    private static Move legal(Game game, String from, String to) {
        for (Move move : game.getLegalMoves(at(from))) {
            if (move.matches(at(from), at(to))) {
                return move;
            }
        }
        return null;
    }

    /** The starting position has 20 legal moves: 16 pawn moves and 4 knight moves. */
    @Test
    public void twentyMovesAtStart() {
        Game game = new Game("", "");
        assertEquals(20, game.getAllLegalMoves().size());
        assertEquals(2, game.getLegalMoves(at("E2")).size());
        assertEquals(2, game.getLegalMoves(at("G1")).size());
        assertTrue(game.getLegalMoves(at("A1")).isEmpty());
    }

    /** A pinned piece may not move off the line between the attacker and its king. */
    @Test
    public void pinnedPieceCannotExposeKing() {
        Game game = TestPositions.game(PieceColor.WHITE, "wK E1", "wN E2", "bR E8", "bK G8");
        assertTrue(game.getLegalMoves(at("E2")).isEmpty());
        MoveResult result = game.tryMove(at("E2"), at("C3"));
        assertFalse(result.isSuccess());
        assertEquals("That move would leave your king in check.", result.getMessage());
    }

    /** A king may not step onto an attacked square. */
    @Test
    public void kingCannotMoveIntoCheck() {
        Game game = TestPositions.game(PieceColor.WHITE, "wK E1", "bR D2", "bK E8");
        assertNull(legal(game, "E1", "E2"));
        assertNull(legal(game, "E1", "F2"));
        assertNotNull(legal(game, "E1", "D2"));
        assertNotNull(legal(game, "E1", "F1"));
    }

    /** In check, only moves that end the check are allowed. */
    @Test
    public void mustGetOutOfCheck() {
        Game game = TestPositions.game(PieceColor.WHITE, "wK E1", "wR A1", "wp A2", "bR H1", "bK E8");
        assertEquals(GameStatus.CHECK, game.getStatus());
        MoveResult result = game.tryMove(at("A2"), at("A3"));
        assertFalse(result.isSuccess());
        assertEquals("Your king is in check, and that move doesn't get it out of check.", result.getMessage());
        for (Move move : game.getAllLegalMoves()) {
            Board trial = game.getBoard().copy();
            ChessRules.applyMove(trial, move);
            assertFalse(ChessRules.isInCheck(trial, PieceColor.WHITE));
        }
    }

    /** Both castles work when the path is clear and nothing is attacked; the rook jumps over. */
    @Test
    public void castlingBothSides() {
        Game game = new Game("", "");
        game.loadPosition(DemoPosition.CASTLING.createPieces(), PieceColor.WHITE);
        assertTrue(game.tryMove(at("E1"), at("G1")).isSuccess());
        assertEquals("wK", game.getBoard().getPiece(at("G1")).getSymbol());
        assertEquals("wR", game.getBoard().getPiece(at("F1")).getSymbol());
        assertNull(game.getBoard().getPiece(at("H1")));
        MoveResult black = game.tryMove(at("E8"), at("C8"));
        assertTrue(black.isSuccess());
        assertEquals(MoveType.CASTLE_QUEENSIDE, black.getMove().getType());
        assertEquals("bR", game.getBoard().getPiece(at("D8")).getSymbol());
        assertEquals("O-O-O", black.getMove().toNotation());
    }

    /** Castling is refused after the king has moved, even if it moves back. */
    @Test
    public void noCastlingAfterKingMoves() {
        Game game = new Game("", "");
        game.loadPosition(DemoPosition.CASTLING.createPieces(), PieceColor.WHITE);
        game.tryMove(at("E1"), at("F1"));
        game.tryMove(at("A7"), at("A6"));
        game.tryMove(at("F1"), at("E1"));
        game.tryMove(at("A6"), at("A5"));
        MoveResult result = game.tryMove(at("E1"), at("G1"));
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().startsWith("You can't castle now."));
    }

    /** Castling is refused through an attacked square, out of check, or past a piece. */
    @Test
    public void noCastlingThroughCheckOrBlockedPath() {
        Game throughCheck = TestPositions.game(PieceColor.WHITE, "wK E1", "wR A1", "wR H1", "bR F3", "bK E8");
        assertNull(legal(throughCheck, "E1", "G1"));
        assertNotNull(legal(throughCheck, "E1", "C1"));

        Game inCheck = TestPositions.game(PieceColor.WHITE, "wK E1", "wR A1", "wR H1", "bR E3", "bK E8");
        assertNull(legal(inCheck, "E1", "G1"));
        assertNull(legal(inCheck, "E1", "C1"));

        Game blocked = TestPositions.game(PieceColor.WHITE, "wK E1", "wR A1", "wN B1", "wN G1", "wR H1", "bK E8");
        assertNull(legal(blocked, "E1", "G1"));
        assertNull(legal(blocked, "E1", "C1"));
    }

    /** En passant works right after a two-square pawn move, removing the passed pawn. */
    @Test
    public void enPassantCapture() {
        Game game = new Game("", "");
        game.loadPosition(DemoPosition.EN_PASSANT.createPieces(), DemoPosition.EN_PASSANT.getToMove());
        assertTrue(game.tryMove(at("D7"), at("D5")).isSuccess());
        MoveResult capture = game.tryMove(at("E5"), at("D6"));
        assertTrue(capture.isSuccess());
        assertEquals(MoveType.EN_PASSANT, capture.getMove().getType());
        assertNull(game.getBoard().getPiece(at("D5")));
        assertEquals("wp", game.getBoard().getPiece(at("D6")).getSymbol());
        assertEquals(1, game.getBoard().getCapturedPieces(PieceColor.BLACK).size());
        assertTrue(capture.getMessage().contains("en passant"));
    }

    /** En passant is only allowed on the very next move. */
    @Test
    public void enPassantExpiresAfterOneMove() {
        Game game = new Game("", "");
        game.loadPosition(DemoPosition.EN_PASSANT.createPieces(), DemoPosition.EN_PASSANT.getToMove());
        game.tryMove(at("D7"), at("D5"));
        game.tryMove(at("H2"), at("H3"));
        game.tryMove(at("H7"), at("H6"));
        assertNull(legal(game, "E5", "D6"));
        assertFalse(game.tryMove(at("E5"), at("D6")).isSuccess());
    }

    /** A pawn reaching the far rank becomes the chosen piece. */
    @Test
    public void promotionToChosenPiece() {
        Game game = new Game("", "");
        game.loadPosition(DemoPosition.PROMOTION.createPieces(), PieceColor.WHITE);
        assertTrue(game.isPromotionMove(at("B7"), at("B8")));
        MoveResult result = game.tryMove(at("B7"), at("B8"), chess.pieces.PromotionPiece.KNIGHT);
        assertTrue(result.isSuccess());
        Piece promoted = game.getBoard().getPiece(at("B8"));
        assertEquals("wN", promoted.getSymbol());
        assertEquals("b7-b8=N", result.getMove().toNotation());
    }

    /** The checkmate sequence from the assignment ends the game with Black winning. */
    @Test
    public void foolsMateIsCheckmate() {
        Game game = new Game("", "");
        assertTrue(game.tryMove(at("F2"), at("F3")).isSuccess());
        assertTrue(game.tryMove(at("E7"), at("E5")).isSuccess());
        assertTrue(game.tryMove(at("G2"), at("G4")).isSuccess());
        MoveResult mate = game.tryMove(at("D8"), at("H4"));
        assertTrue(mate.isSuccess());
        assertEquals(GameStatus.CHECKMATE, game.getStatus());
        assertTrue(game.isOver());
        assertEquals(PieceColor.BLACK, game.getWinner().getColor());
        assertTrue(mate.getMessage().endsWith("Checkmate! Black wins."));
        assertEquals("Qd8-h4#", mate.getMove().toNotation());
        assertFalse(game.tryMove(at("A2"), at("A3")).isSuccess());
        assertTrue(game.getAllLegalMoves().isEmpty());
    }

    /** The stalemate demo ends in a draw with no winner. */
    @Test
    public void stalemateIsADraw() {
        Game game = new Game("", "");
        game.loadPosition(DemoPosition.STALEMATE.createPieces(), PieceColor.WHITE);
        MoveResult result = game.tryMove(at("G5"), at("G6"));
        assertTrue(result.isSuccess());
        assertEquals(GameStatus.STALEMATE, game.getStatus());
        assertNull(game.getWinner());
        assertTrue(game.isOver());
    }

    /** Every demo position loads and has legal moves for the side to move. */
    @Test
    public void demoPositionsAreValid() {
        for (DemoPosition demo : DemoPosition.values()) {
            Game game = new Game("", "");
            game.loadPosition(demo.createPieces(), demo.getToMove());
            assertFalse(demo.name(), game.getAllLegalMoves().isEmpty());
            assertFalse(demo.name(), game.isOver());
        }
    }

    /** The checkmate demo is one move from mate. */
    @Test
    public void checkmateDemoMatesInOne() {
        Game game = new Game("", "");
        game.loadPosition(DemoPosition.CHECKMATE_SETUP.createPieces(), PieceColor.BLACK);
        game.tryMove(at("D8"), at("H4"));
        assertEquals(GameStatus.CHECKMATE, game.getStatus());
    }

    /** Pawns attack diagonally even when the square is empty. */
    @Test
    public void pawnAttacksEmptyDiagonals() {
        Board board = new Board();
        board.placePiece(new chess.pieces.Pawn(PieceColor.WHITE, at("E4")));
        assertTrue(ChessRules.isSquareAttacked(board, at("D5"), PieceColor.WHITE));
        assertTrue(ChessRules.isSquareAttacked(board, at("F5"), PieceColor.WHITE));
        assertFalse(ChessRules.isSquareAttacked(board, at("E5"), PieceColor.WHITE));
    }

    /** Working out legal moves never changes the real board. */
    @Test
    public void legalMoveGenerationDoesNotChangeBoard() {
        Game game = new Game("", "");
        String before = snapshot(game.getBoard());
        game.getAllLegalMoves();
        assertEquals(before, snapshot(game.getBoard()));
    }

    /**
     * Describes every square, for comparing boards.
     *
     * @param board the board
     * @return a 64-symbol description
     */
    private static String snapshot(Board board) {
        StringBuilder text = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                if (piece == null) {
                    text.append("--");
                } else {
                    text.append(piece.getSymbol());
                }
            }
        }
        return text.toString();
    }
}
