package chess.gui;

import chess.model.PieceColor;
import chess.pieces.Piece;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The panel beside the board: whose turn it is, status messages, captured
 * pieces, the move list and the main buttons.
 */
@SuppressWarnings("serial") // Swing windows are never saved to a file, so they need no serial ID.
public final class SidePanel extends JPanel {

    /** Kinds of status message, each shown in its own color. */
    public enum Tone {
        /** Ordinary information. */
        INFO(new Color(60, 60, 60)),
        /** A rejected move. */
        ERROR(new Color(190, 30, 30)),
        /** A king in check. */
        WARNING(new Color(200, 110, 0)),
        /** The game is over. */
        RESULT(new Color(20, 90, 170));

        /** Text color for this tone. */
        private final Color color;

        /**
         * Creates a tone.
         *
         * @param color its text color
         */
        Tone(Color color) {
            this.color = color;
        }
    }

    /** Shows whose turn it is. */
    private final JLabel turnLabel = new JLabel();

    /** Shows the latest message; a text area so long messages wrap onto more lines. */
    private final JTextArea statusText = new JTextArea(3, 20);

    /** Names the White player. */
    private final JLabel whitePlayerLabel = new JLabel();

    /** Names the Black player. */
    private final JLabel blackPlayerLabel = new JLabel();

    /** Pieces White has captured. */
    private final CapturedStrip whiteCaptures = new CapturedStrip();

    /** Pieces Black has captured. */
    private final CapturedStrip blackCaptures = new CapturedStrip();

    /** The lines in the move list. */
    private final DefaultListModel<String> historyModel = new DefaultListModel<>();

    /** The move list, one numbered line per move. */
    private final JList<String> historyList = new JList<>(historyModel);

    /** Starts a new game. */
    private final JButton newGameButton = new JButton("New Game");

    /** Restarts the current game. */
    private final JButton restartButton = new JButton("Restart");

    /**
     * Builds the panel. {@link ChessFrame} connects the buttons to the controller.
     */
    public SidePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 14));
        setPreferredSize(new Dimension(310, 640));

        turnLabel.setFont(turnLabel.getFont().deriveFont(Font.BOLD, 20f));
        add(left(turnLabel));
        add(Box.createVerticalStrut(4));
        add(left(whitePlayerLabel));
        add(left(blackPlayerLabel));
        add(Box.createVerticalStrut(10));

        statusText.setFont(whitePlayerLabel.getFont().deriveFont(Font.PLAIN, 14f));
        statusText.setEditable(false);
        statusText.setFocusable(false);
        statusText.setLineWrap(true);
        statusText.setWrapStyleWord(true);
        statusText.setOpaque(false);
        JPanel statusBox = new JPanel(new BorderLayout());
        statusBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        statusBox.add(statusText);
        statusBox.setPreferredSize(new Dimension(260, 74));
        statusBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
        add(left(statusBox));
        add(Box.createVerticalStrut(8));

        add(left(sectionTitle("Captured")));
        add(left(whiteCaptures));
        add(left(blackCaptures));
        add(Box.createVerticalStrut(10));

        add(left(sectionTitle("Moves")));
        historyList.setFocusable(false);
        JScrollPane scroll = new JScrollPane(historyList);
        scroll.setPreferredSize(new Dimension(260, 220));
        add(left(scroll));
        add(Box.createVerticalStrut(10));

        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 0));
        for (JButton button : new JButton[] {newGameButton, restartButton}) {
            button.setFocusable(false);
            buttons.add(button);
        }
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        add(left(buttons));
    }

    /**
     * Left-aligns a component inside the vertical box.
     *
     * @param component the component
     * @return the same component
     */
    private static JComponent left(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        return component;
    }

    /**
     * Makes a small bold heading.
     *
     * @param text the heading
     * @return the label
     */
    private static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        label.setForeground(new Color(120, 120, 120));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return label;
    }

    /**
     * Returns the New Game button.
     *
     * @return the button
     */
    public JButton getNewGameButton() {
        return newGameButton;
    }

    /**
     * Returns the Restart button.
     *
     * @return the button
     */
    public JButton getRestartButton() {
        return restartButton;
    }

    /**
     * Shows whose turn it is.
     *
     * @param color the side to move
     * @param name  that player's name
     */
    public void setTurn(PieceColor color, String name) {
        turnLabel.setText(name + " (" + color.displayName() + ") to move");
    }

    /**
     * Replaces the turn indicator with the result.
     *
     * @param text such as "Black wins"
     */
    public void setGameOver(String text) {
        turnLabel.setText(text);
    }

    /**
     * Shows who is playing.
     *
     * @param white description of the White player
     * @param black description of the Black player
     */
    public void setPlayers(String white, String black) {
        whitePlayerLabel.setText("White: " + white);
        blackPlayerLabel.setText("Black: " + black);
    }

    /**
     * Shows a status message.
     *
     * @param message the message
     * @param tone    how to color it
     */
    public void setStatus(String message, Tone tone) {
        statusText.setForeground(tone.color);
        statusText.setText(message);
    }

    /**
     * Shows the captured pieces.
     *
     * @param byWhite pieces White has captured (black pieces)
     * @param byBlack pieces Black has captured (white pieces)
     */
    public void setCaptured(List<Piece> byWhite, List<Piece> byBlack) {
        whiteCaptures.show("White", byWhite);
        blackCaptures.show("Black", byBlack);
    }

    /**
     * Fills the move list, then scrolls to the newest move.
     *
     * @param lines the numbered history lines from {@link chess.service.Game#getHistoryLines()}
     */
    public void setHistory(List<String> lines) {
        historyModel.clear();
        for (String line : lines) {
            historyModel.addElement(line);
        }
        if (!lines.isEmpty()) {
            historyList.ensureIndexIsVisible(lines.size() - 1);
        }
    }

    /**
     * One row of captured pieces, drawn small.
     */
    @SuppressWarnings("serial")
    private static final class CapturedStrip extends JComponent {

        /** Which side did the capturing. */
        private String side = "";

        /** The captured pieces. */
        private List<Piece> pieces = new ArrayList<>();

        /** Creates an empty strip. */
        CapturedStrip() {
            setPreferredSize(new Dimension(260, 30));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        }

        /**
         * Updates the strip.
         *
         * @param side   "White" or "Black"
         * @param pieces the pieces that side captured
         */
        void show(String side, List<Piece> pieces) {
            this.side = side;
            this.pieces = new ArrayList<>(pieces);
            this.pieces.sort(new Comparator<Piece>() {
                @Override
                public int compare(Piece a, Piece b) {
                    return b.getValue() - a.getValue();
                }
            });
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            g.setColor(Color.DARK_GRAY);
            g.drawString(side + ":", 0, 19);
            double x = 46;
            for (Piece piece : pieces) {
                PieceGlyphs.draw(g, piece.getTypeLetter(), piece.getColor(), x, 2, 26);
                x += 15;
            }
            g.dispose();
        }
    }
}
