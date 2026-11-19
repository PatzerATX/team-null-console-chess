package chess.ai;

import chess.TestPositions;
import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;
import chess.model.Position;
import chess.service.ChessRules;
import chess.service.Game;
import chess.service.GameStatus;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the computer players and the safety wrapper.
 */
public class AiTest {

    /**
     * Shorthand for a square.
     *
     * @param square such as "E2"
     * @return the position
     */
    private static Position at(String square) {
        return Position.fromNotation(square);
    }

    /** Every level returns a legal move from the start. */
    @Test
    public void everyLevelPlaysLegalMoves() {
        for (Difficulty level : Difficulty.values()) {
            Game game = new Game("", "");
            MoveStrategy ai = level.createStrategy(new Random(7));
            for (int i = 0; i < 6 && !game.isOver(); i++) {
                Move move = ai.chooseMove(game.getBoard().copy(), game.getCurrentTurn(), game.getLastMove());
                assertNotNull(level.name(), move);
                assertTrue(level.name() + " " + move, game.tryMove(move).isSuccess());
            }
        }
    }

    /** Medium (one move ahead) takes a free queen. */
    @Test
    public void mediumTakesFreeQueen() {
        Game game = TestPositions.game(PieceColor.WHITE, "wK E1", "wR D1", "bQ D5", "bK E8");
        Move move = new MinimaxAI(1, new Random(1)).chooseMove(game.getBoard().copy(), PieceColor.WHITE, null);
        assertEquals("Rd1xd5", move.toNotation());
    }

    /** Hard (three moves ahead) won't trade its queen for a knight that a pawn defends. */
    @Test
    public void hardAvoidsDefendedPiece() {
        Game game = TestPositions.game(PieceColor.WHITE, "wK E1", "wQ D1", "bN D5", "bp C6", "bK E8");
        Move move = new MinimaxAI(3, new Random(1)).chooseMove(game.getBoard().copy(), PieceColor.WHITE, null);
        assertFalse("Hard should not play " + move, move.toNotation().equals("Qd1xd5"));
    }

    /** Hard finds a checkmate in one: the rook to the back row behind Black's pawns. */
    @Test
    public void hardFindsMateInOne() {
        Game game = TestPositions.game(PieceColor.WHITE, "wK G1", "wR A1", "bK G8", "bp F7", "bp G7", "bp H7");
        Move move = new MinimaxAI(3, new Random(1)).chooseMove(game.getBoard().copy(), PieceColor.WHITE, null);
        game.tryMove(move);
        assertEquals(GameStatus.CHECKMATE, game.getStatus());
    }

    /** The search must not change the board it is given. */
    @Test
    public void searchLeavesBoardAlone() {
        Game game = new Game("", "");
        Board board = game.getBoard();
        new MinimaxAI(3, new Random(1)).chooseMove(board, PieceColor.WHITE, null);
        assertEquals(20, ChessRules.legalMoves(board, PieceColor.WHITE, null).size());
        assertEquals("wp", board.getPiece(at("E2")).getSymbol());
    }

    /** With no legal moves, every level returns nothing. */
    @Test
    public void noMoveWhenNoneAreLegal() {
        Game game = TestPositions.game(PieceColor.BLACK, "bK H8", "wQ F7", "wK G6");
        for (Difficulty level : Difficulty.values()) {
            assertNull(level.createStrategy(new Random(1)).chooseMove(game.getBoard().copy(), PieceColor.BLACK, null));
        }
    }

    /** A strategy that crashes is replaced by a random legal move. */
    @Test
    public void fallbackWhenStrategyThrows() {
        MoveStrategy broken = new MoveStrategy() {
            @Override
            public Move chooseMove(Board board, PieceColor side, Move lastMove) {
                throw new IllegalStateException("broken on purpose");
            }

            @Override
            public String getName() {
                return "Broken";
            }
        };
        assertFallbackIsLegal(broken);
    }

    /** A strategy that returns an illegal move is overruled. */
    @Test
    public void fallbackWhenStrategyCheats() {
        MoveStrategy cheat = new MoveStrategy() {
            @Override
            public Move chooseMove(Board board, PieceColor side, Move lastMove) {
                return new Move(at("E2"), at("E5"), board.getPiece(at("E2")), null,
                        chess.model.MoveType.NORMAL, null);
            }

            @Override
            public String getName() {
                return "Cheat";
            }
        };
        assertFallbackIsLegal(cheat);
    }

    /**
     * Checks that a bad strategy, wrapped, still gives a legal move.
     *
     * @param bad the misbehaving strategy
     */
    private static void assertFallbackIsLegal(MoveStrategy bad) {
        java.util.logging.Logger.getLogger(SafeMoveSelector.class.getName()).setLevel(java.util.logging.Level.OFF);
        Game game = new Game("", "");
        Move move = new SafeMoveSelector(bad, new Random(3))
                .chooseMove(game.getBoard().copy(), PieceColor.WHITE, null);
        assertNotNull(move);
        assertTrue(game.tryMove(move).isSuccess());
    }

    /** Depth below one is refused. */
    @Test(expected = IllegalArgumentException.class)
    public void depthMustBePositive() {
        new MinimaxAI(0, new Random());
    }

    /** Decorators stack: logging around the safety check around a broken strategy still gives a legal move. */
    @Test
    public void decoratorsStack() {
        java.util.logging.Logger.getLogger(SafeMoveSelector.class.getName()).setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("chess.ai").setLevel(java.util.logging.Level.OFF);
        MoveStrategy broken = new MoveStrategy() {
            @Override
            public Move chooseMove(Board board, PieceColor side, Move lastMove) {
                return null;
            }

            @Override
            public String getName() {
                return "Broken";
            }
        };
        MoveStrategy stacked = new LoggingMoveStrategy(new SafeMoveSelector(broken, new Random(5)));
        assertEquals("Broken", stacked.getName());
        Game game = new Game("", "");
        assertTrue(game.tryMove(stacked.chooseMove(game.getBoard().copy(), PieceColor.WHITE, null)).isSuccess());
    }
}
