package chess.console;

import chess.model.Position;
import chess.pieces.PromotionPiece;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests reading moves and commands typed in the console version.
 */
public class InputParserTest {

    /** The parser under test. */
    private final InputParser parser = new InputParser();

    /** Moves can be typed several ways, in either case. */
    @Test
    public void acceptsCommonFormats() {
        for (String text : new String[] {"E2 E4", "e2 e4", "e2-e4", "e2e4", "  E2   E4  "}) {
            Position[] move = parser.parseMove(text);
            assertEquals(text, Position.fromNotation("E2"), move[0]);
            assertEquals(text, Position.fromNotation("E4"), move[1]);
        }
    }

    /** A promotion letter can follow the move; the default is a queen. */
    @Test
    public void readsPromotionChoice() {
        assertEquals(PromotionPiece.QUEEN, parser.parsePromotion("B7 B8"));
        assertEquals(PromotionPiece.KNIGHT, parser.parsePromotion("B7 B8 N"));
        assertEquals(PromotionPiece.ROOK, parser.parsePromotion("b7b8r"));
    }

    /** Commands are recognised regardless of case; anything else is a move. */
    @Test
    public void recognisesCommands() {
        assertEquals(Command.HELP, parser.getCommand("HELP"));
        assertEquals(Command.QUIT, parser.getCommand(" quit "));
        assertEquals(Command.RESTART, parser.getCommand("restart"));
        assertEquals(Command.MOVE, parser.getCommand("E2 E4"));
    }

    /** Squares off the board are refused. */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsOffBoardSquare() {
        parser.parseMove("E9 E4");
    }

    /** A file letter past H is refused. */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsBadFile() {
        parser.parseMove("I2 I4");
    }

    /** Blank input is refused. */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlank() {
        parser.parseMove("   ");
    }

    /** A bad promotion letter is refused. */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsBadPromotion() {
        parser.parsePromotion("B7 B8 K");
    }
}
