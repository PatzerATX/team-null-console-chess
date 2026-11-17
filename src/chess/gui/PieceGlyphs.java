package chess.gui;

import chess.model.PieceColor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

/**
 * Draws chess pieces with the Unicode chess symbols (U+2654 to U+265F), so no
 * image files are needed. Each piece is drawn from the solid symbol, filled in
 * its color and outlined in the opposite color so it stands out on any square.
 */
public final class PieceGlyphs {

    /** Fonts known to include the chess symbols, best first. */
    private static final List<String> PREFERRED_FONTS = Arrays.asList(
            "Apple Symbols", "Segoe UI Symbol", "DejaVu Sans", "Arial Unicode MS", "Noto Sans Symbols2");

    /** The solid king symbol; other solid pieces follow it in Unicode order. */
    private static final char SOLID_KING = '♚';

    /** Fill color for white pieces. */
    private static final Color WHITE_FILL = new Color(255, 255, 250);

    /** Fill color for black pieces. */
    private static final Color BLACK_FILL = new Color(30, 30, 34);

    /** The font family chosen for this computer. */
    private static final String FAMILY = chooseFamily();

    /** Prevents creating objects; this class only holds static methods. */
    private PieceGlyphs() {
    }

    /**
     * Picks the first installed font that has the chess symbols.
     *
     * @return a font family name
     */
    private static String chooseFamily() {
        List<String> installed = Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String name : PREFERRED_FONTS) {
            if (installed.contains(name) && new Font(name, Font.PLAIN, 12).canDisplay(SOLID_KING)) {
                return name;
            }
        }
        return Font.DIALOG;
    }

    /**
     * Returns the Unicode symbol for a piece letter.
     *
     * @param letter 'K', 'Q', 'R', 'B', 'N' or 'p'
     * @return the solid chess symbol
     */
    public static char symbolFor(char letter) {
        switch (letter) {
            case 'K':
                return SOLID_KING;
            case 'Q':
                return '♛';
            case 'R':
                return '♜';
            case 'B':
                return '♝';
            case 'N':
                return '♞';
            default:
                return '♟';
        }
    }

    /**
     * Draws a piece centered in a square.
     *
     * @param g      the graphics to draw with (anti-aliasing should be on)
     * @param letter the piece letter
     * @param color  the piece color
     * @param x      left edge of the square
     * @param y      top edge of the square
     * @param size   width and height of the square
     */
    public static void draw(Graphics2D g, char letter, PieceColor color, double x, double y, double size) {
        Font font = new Font(FAMILY, Font.PLAIN, (int) Math.round(size * 0.78));
        GlyphVector glyph = font.createGlyphVector(g.getFontRenderContext(),
                String.valueOf(symbolFor(letter)));
        Shape outline = glyph.getOutline();
        Rectangle2D bounds = outline.getBounds2D();
        double dx = x + (size - bounds.getWidth()) / 2 - bounds.getX();
        double dy = y + (size - bounds.getHeight()) / 2 - bounds.getY();
        Shape placed = AffineTransform.getTranslateInstance(dx, dy).createTransformedShape(outline);

        Color fill = WHITE_FILL;
        Color edge = BLACK_FILL;
        if (color == PieceColor.BLACK) {
            fill = BLACK_FILL;
            edge = new Color(200, 200, 200);
        }
        g.setColor(fill);
        g.fill(placed);
        g.setStroke(new BasicStroke((float) Math.max(1.0, size / 40.0),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(edge);
        g.draw(placed);
    }
}
