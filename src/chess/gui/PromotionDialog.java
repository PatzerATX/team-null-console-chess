package chess.gui;

import chess.model.PieceColor;
import chess.pieces.PromotionPiece;

import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * Asks which piece a pawn should become, using a standard
 * {@link JOptionPane} with one button per choice. Closing the box chooses a queen.
 */
public final class PromotionDialog {

    /** Prevents creating objects; this class only holds a static method. */
    private PromotionDialog() {
    }

    /**
     * Shows the choice and waits for an answer.
     *
     * @param owner the window to center over
     * @param color the promoting pawn's color
     * @return the chosen piece
     */
    public static PromotionPiece ask(Component owner, PieceColor color) {
        PromotionPiece[] choices = PromotionPiece.values();
        String[] names = new String[choices.length];
        for (int i = 0; i < choices.length; i++) {
            names[i] = choices[i].getDisplayName();
        }
        int picked = JOptionPane.showOptionDialog(owner,
                "Choose a piece for your " + color.displayName().toLowerCase() + " pawn:",
                "Promote pawn", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, names, names[0]);
        if (picked < 0) {
            return PromotionPiece.QUEEN;
        }
        return choices[picked];
    }
}
