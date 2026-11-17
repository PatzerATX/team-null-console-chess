package chess.gui;

import chess.model.Board;
import chess.model.Move;
import chess.model.Position;
import chess.pieces.Piece;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws the chessboard and turns mouse clicks into squares.
 *
 * <p>This is the "view" in Model-View-Controller: it reads the board to draw
 * it, but never changes the game. Clicks are passed to the controller through
 * a callback.</p>
 */
@SuppressWarnings("serial") // Swing windows are never saved to a file, so they need no serial ID.
public final class BoardPanel extends JPanel {

    /** Color of the light squares. */
    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);

    /** Color of the dark squares. */
    private static final Color DARK_SQUARE = new Color(181, 136, 99);

    /** Tint for the selected piece's square. */
    private static final Color SELECTED = new Color(255, 214, 10, 150);

    /** Color of the square under a king in check. */
    private static final Color CHECK = new Color(230, 70, 70);

    /** Color of the legal-move dots and capture rings. */
    private static final Color HINT = new Color(20, 30, 20, 70);

    /** Coordinate label color. */
    private static final Color LABEL = new Color(90, 90, 90);

    /** The board being drawn. */
    private Board board;

    /** The selected square, or {@code null}. */
    private Position selected;

    /** Legal moves from the selected square. */
    private List<Move> targets = new ArrayList<>();

    /** Square of a king in check, or {@code null}. */
    private Position checkSquare;

    /** Told about each click on a square; set by the controller. */
    private GameController controller;

    /** Creates the board panel. */
    public BoardPanel() {
        setPreferredSize(new Dimension(640, 640));
        setMinimumSize(new Dimension(360, 360));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Position square = squareAt(e.getX(), e.getY());
                if (square != null && controller != null) {
                    controller.squareClicked(square);
                }
            }
        });
    }

    /**
     * Sets the board to draw.
     *
     * @param board the board
     */
    public void setBoard(Board board) {
        this.board = board;
        repaint();
    }

    /**
     * Sets the controller that handles clicks on the board.
     *
     * @param controller the game controller
     */
    public void setController(GameController controller) {
        this.controller = controller;
    }

    /**
     * Sets the selected square and the moves to highlight from it.
     *
     * @param square  the selected square, or {@code null} for none
     * @param targets legal moves from that square
     */
    public void setSelection(Position square, List<Move> targets) {
        this.selected = square;
        this.targets = new ArrayList<>(targets);
        repaint();
    }

    /**
     * Sets the square of a king in check, which is colored red.
     *
     * @param checkSquare the checked king's square, or {@code null}
     */
    public void setCheckSquare(Position checkSquare) {
        this.checkSquare = checkSquare;
        repaint();
    }

    /**
     * Returns the width of the margin that holds the coordinate labels.
     *
     * @return margin in pixels
     */
    private int margin() {
        return Math.max(18, Math.min(getWidth(), getHeight()) / 28);
    }

    /**
     * Returns the size of one square in pixels.
     *
     * @return the square size
     */
    private double squareSize() {
        int m = margin();
        return Math.min(getWidth() - 2 * m, getHeight() - 2 * m) / 8.0;
    }

    /**
     * Returns the left edge of the board.
     *
     * @return x in pixels
     */
    private double boardLeft() {
        return (getWidth() - squareSize() * 8) / 2;
    }

    /**
     * Returns the top edge of the board.
     *
     * @return y in pixels
     */
    private double boardTop() {
        return (getHeight() - squareSize() * 8) / 2;
    }

    /**
     * Finds the board square at a pixel.
     *
     * @param x pixel x
     * @param y pixel y
     * @return the square, or {@code null} if the point is off the board
     */
    private Position squareAt(int x, int y) {
        double size = squareSize();
        int col = (int) Math.floor((x - boardLeft()) / size);
        int row = (int) Math.floor((y - boardTop()) / size);
        if (!Position.isOnBoard(row, col)) {
            return null;
        }
        return new Position(row, col);
    }

    /**
     * Draws everything: squares, labels, pieces and move hints.
     *
     * @param graphics the graphics context
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        drawSquares(g);
        drawLabels(g);
        if (board != null) {
            drawPieces(g);
            drawHints(g);
        }
        g.dispose();
    }

    /**
     * Draws the 64 squares, the selected square and a king in check.
     *
     * @param g the graphics context
     */
    private void drawSquares(Graphics2D g) {
        double size = squareSize();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                double x = boardLeft() + col * size;
                double y = boardTop() + row * size;
                Rectangle2D square = new Rectangle2D.Double(x, y, size + 0.5, size + 0.5);
                Position position = new Position(row, col);
                if (position.equals(checkSquare)) {
                    g.setColor(CHECK);
                } else if ((row + col) % 2 == 0) {
                    g.setColor(LIGHT_SQUARE);
                } else {
                    g.setColor(DARK_SQUARE);
                }
                g.fill(square);
                if (position.equals(selected)) {
                    g.setColor(SELECTED);
                    g.fill(square);
                }
            }
        }
    }

    /**
     * Draws the file letters below the board and rank numbers beside it.
     *
     * @param g the graphics context
     */
    private void drawLabels(Graphics2D g) {
        double size = squareSize();
        int m = margin();
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(11, m * 6 / 10)));
        g.setColor(LABEL);
        FontMetrics metrics = g.getFontMetrics();
        for (int i = 0; i < 8; i++) {
            Position square = new Position(i, i);
            String file = String.valueOf(square.getFile());
            String rank = String.valueOf(square.getRank());
            double fileX = boardLeft() + i * size + size / 2 - metrics.stringWidth(file) / 2.0;
            double fileY = boardTop() + 8 * size + (m + metrics.getAscent()) / 2.0;
            g.drawString(file, (float) fileX, (float) fileY);
            double rankX = boardLeft() - (m + metrics.stringWidth(rank)) / 2.0;
            double rankY = boardTop() + i * size + size / 2 + metrics.getAscent() / 2.5;
            g.drawString(rank, (float) rankX, (float) rankY);
        }
        g.setColor(Color.GRAY);
        g.draw(new Rectangle2D.Double(boardLeft(), boardTop(), size * 8, size * 8));
    }

    /**
     * Draws every piece on its square.
     *
     * @param g the graphics context
     */
    private void drawPieces(Graphics2D g) {
        double size = squareSize();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                if (piece != null) {
                    PieceGlyphs.draw(g, piece.getTypeLetter(), piece.getColor(),
                            boardLeft() + col * size, boardTop() + row * size, size);
                }
            }
        }
    }

    /**
     * Draws a dot on each empty legal destination and a ring around each
     * piece that can be captured.
     *
     * @param g the graphics context
     */
    private void drawHints(Graphics2D g) {
        double size = squareSize();
        g.setColor(HINT);
        g.setStroke(new BasicStroke((float) (size * 0.08)));
        for (Move move : targets) {
            Position to = move.getTo();
            double cx = boardLeft() + to.getCol() * size + size / 2;
            double cy = boardTop() + to.getRow() * size + size / 2;
            if (move.isCapture()) {
                double r = size * 0.46;
                g.draw(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
            } else {
                double r = size * 0.15;
                g.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
            }
        }
    }
}
