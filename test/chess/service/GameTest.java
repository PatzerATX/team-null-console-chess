package chess.service;

import chess.TestPositions;
import chess.model.Move;
import chess.model.PieceColor;
import chess.model.Player;
import chess.model.Position;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link Game} class: normal play, invalid input and listener events.
 */
public class GameTest {

    /**
     * Shorthand for a square.
     *
     * @param square such as "E2"
     * @return the position
     */
    private static Position at(String square) {
        return Position.fromNotation(square);
    }

    /** A new game starts with White to move, 20 legal moves and no history. */
    @Test
    public void newGameStartsWithWhite() {
        Game game = new Game("Ryan", "Sam");
        assertEquals(PieceColor.WHITE, game.getCurrentTurn());
        assertEquals("Ryan", game.getCurrentPlayer().getName());
        assertEquals(20, game.getAllLegalMoves().size());
        assertTrue(game.getMoveHistory().isEmpty());
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
    }

    /** A legal move changes the board, the turn and the history. */
    @Test
    public void legalMoveSwitchesTurn() {
        Game game = new Game("", "");
        MoveResult result = game.tryMove(at("E2"), at("E4"));
        assertTrue(result.isSuccess());
        assertEquals("White pawn moves E2 to E4.", result.getMessage());
        assertEquals(PieceColor.BLACK, game.getCurrentTurn());
        assertNull(game.getBoard().getPiece(at("E2")));
        assertEquals(1, game.getMoveHistory().size());
        assertEquals("e2-e4", game.getLastMove().toNotation());
    }

    /** Each kind of invalid move is refused with its own message, and nothing changes. */
    @Test
    public void invalidMovesLeaveGameUnchanged() {
        Game game = new Game("", "");
        assertRejected(game, "A3", "A4", "There is no piece on A3.");
        assertRejected(game, "E7", "E5", "The Black pawn on E7 isn't yours. It's White's turn.");
        assertRejected(game, "A1", "A2", "You can't move onto your own pawn on A2.");
        assertRejected(game, "E2", "E2", "A piece must move to a different square.");
        assertRejected(game, "A1", "A5", "The White rook's path from A1 to A5 is blocked.");
        assertRejected(game, "E2", "E5", "A pawn can't move from E2 to E5.");
        assertEquals(PieceColor.WHITE, game.getCurrentTurn());
        assertTrue(game.getMoveHistory().isEmpty());
    }

    /**
     * Checks that a move is refused with a message and the turn stays the same.
     *
     * @param game    the game
     * @param from    start square
     * @param to      destination
     * @param message the expected message
     */
    private static void assertRejected(Game game, String from, String to, String message) {
        MoveResult result = game.tryMove(at(from), at(to));
        assertFalse(from + "-" + to, result.isSuccess());
        assertEquals(message, result.getMessage());
    }

    /** Corner squares work, and a knight can jump from the corner. */
    @Test
    public void boundarySquares() {
        Game game = TestPositions.game(PieceColor.WHITE, "wN A1", "wK H1", "bK H8");
        assertEquals(2, game.getLegalMoves(at("A1")).size());
        assertTrue(game.tryMove(at("A1"), at("B3")).isSuccess());
        assertTrue(game.tryMove(at("H8"), at("G8")).isSuccess());
    }

    /** Listeners hear about moves, rejections and the end of the game. */
    @Test
    public void listenersAreNotified() {
        Game game = new Game("", "");
        List<String> events = new ArrayList<>();
        game.addListener(new GameListener() {
            @Override
            public void onGameStarted() {
                events.add("start");
            }

            @Override
            public void onMoveMade(Move move, GameStatus status) {
                events.add(move.toNotation());
            }

            @Override
            public void onMoveRejected(Position from, Position to, String reason) {
                events.add("rejected");
            }

            @Override
            public void onGameOver(GameStatus result, Player winner) {
                events.add(result + " " + winner.getColor());
            }
        });
        game.tryMove(at("E2"), at("E5"));
        game.tryMove(at("F2"), at("F3"));
        game.tryMove(at("E7"), at("E5"));
        game.tryMove(at("G2"), at("G4"));
        game.tryMove(at("D8"), at("H4"));
        game.start();
        assertEquals("[rejected, f2-f3, e7-e5, g2-g4, Qd8-h4#, CHECKMATE BLACK, start]", events.toString());
    }

    /** Restarting clears the board, history, captures and result. */
    @Test
    public void restartResetsEverything() {
        Game game = new Game("", "");
        game.tryMove(at("E2"), at("E4"));
        game.tryMove(at("D7"), at("D5"));
        game.tryMove(at("E4"), at("D5"));
        assertEquals(1, game.getBoard().getCapturedPieces().size());
        game.start();
        assertEquals(PieceColor.WHITE, game.getCurrentTurn());
        assertTrue(game.getMoveHistory().isEmpty());
        assertTrue(game.getBoard().getCapturedPieces().isEmpty());
        assertEquals("bp", game.getBoard().getPiece(at("D7")).getSymbol());
    }

    /** A position must have both kings. */
    @Test(expected = IllegalArgumentException.class)
    public void positionWithoutKingIsRejected() {
        new Game("", "").loadPosition(TestPositions.pieces("wK A1"), PieceColor.WHITE);
    }

    /** The history list can't be changed from outside. */
    @Test(expected = UnsupportedOperationException.class)
    public void historyIsReadOnly() {
        new Game("", "").getMoveHistory().clear();
    }

    /** The history uses the Phase 1 wording, numbered one line per move. */
    @Test
    public void historyLinesUsePhaseOneStyle() {
        Game game = new Game("", "");
        game.tryMove(at("F2"), at("F3"));
        game.tryMove(at("E7"), at("E5"));
        game.tryMove(at("G2"), at("G4"));
        game.tryMove(at("D8"), at("H4"));
        List<String> expected = new ArrayList<>();
        expected.add("1. White pawn F2 - F3");
        expected.add("2. Black pawn E7 - E5");
        expected.add("3. White pawn G2 - G4");
        expected.add("4. Black queen D8 - H4 checkmate");
        assertEquals(expected, game.getHistoryLines());
    }

    /** Captures, castling, promotion and en passant are described in the same style. */
    @Test
    public void specialMovesAreDescribed() {
        Game castle = new Game("", "");
        castle.loadPosition(DemoPosition.CASTLING.createPieces(), PieceColor.WHITE);
        assertEquals("White castles kingside", castle.tryMove(at("E1"), at("G1")).getMove().toDescription());
        Game promote = new Game("", "");
        promote.loadPosition(DemoPosition.PROMOTION.createPieces(), PieceColor.WHITE);
        Move promotion = promote.tryMove(at("B7"), at("B8"), chess.pieces.PromotionPiece.KNIGHT).getMove();
        assertEquals("White pawn B7 - B8 =N", promotion.toDescription());
        Game passant = new Game("", "");
        passant.loadPosition(DemoPosition.EN_PASSANT.createPieces(), PieceColor.BLACK);
        passant.tryMove(at("D7"), at("D5"));
        assertEquals("White pawn E5 x D6 en passant", passant.tryMove(at("E5"), at("D6")).getMove().toDescription());
        Game capture = new Game("", "");
        capture.tryMove(at("E2"), at("E4"));
        capture.tryMove(at("D7"), at("D5"));
        assertEquals("White pawn E4 x D5", capture.tryMove(at("E4"), at("D5")).getMove().toDescription());
    }
}
