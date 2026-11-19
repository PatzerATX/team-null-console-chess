package chess;

import chess.model.PieceColor;
import chess.model.Position;
import chess.pieces.Piece;
import chess.pieces.PieceFactory;
import chess.service.Game;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper for the tests: sets up a game with only the pieces a test needs.
 *
 * <p>Each piece is written the way the console board shows it, a color letter
 * and a piece letter, followed by its square. For example:</p>
 * <pre>
 *     TestPositions.game(PieceColor.WHITE, "wK E1", "bK E8", "bR E2");
 * </pre>
 */
public final class TestPositions {

    /** Prevents creating objects; this class only holds static helpers. */
    private TestPositions() {
    }

    /**
     * Creates pieces from short descriptions such as "wK E1" or "bp D7".
     * A pawn that isn't on its starting row counts as having moved, so it
     * can't jump two squares.
     *
     * @param descriptions one description per piece
     * @return the pieces
     */
    public static List<Piece> pieces(String... descriptions) {
        List<Piece> pieces = new ArrayList<>();
        for (String description : descriptions) {
            PieceColor color = PieceColor.BLACK;
            if (description.charAt(0) == 'w') {
                color = PieceColor.WHITE;
            }
            char letter = description.charAt(1);
            Position square = Position.fromNotation(description.substring(3));
            Piece piece = PieceFactory.create(letter, color, square);
            if (piece.isPawn()) {
                int startRow = 1;
                if (color == PieceColor.WHITE) {
                    startRow = 6;
                }
                piece.setMoved(square.getRow() != startRow);
            }
            pieces.add(piece);
        }
        return pieces;
    }

    /**
     * Creates a game that starts from the given pieces.
     *
     * @param toMove       the side to move first
     * @param descriptions one description per piece, such as "wK E1"
     * @return the game
     */
    public static Game game(PieceColor toMove, String... descriptions) {
        Game game = new Game("White", "Black");
        game.loadPosition(pieces(descriptions), toMove);
        return game;
    }
}
