package chess.gui;

import chess.logging.LoggingSetup;

import javax.swing.SwingUtilities;
import java.util.logging.Logger;

/**
 * Starts the graphical chess game.
 */
public final class ChessGuiApp {

    /** Prevents creating objects; this class only holds {@code main}. */
    private ChessGuiApp() {
    }

    /**
     * Sets up logging, then opens the window on Swing's event thread, where
     * all Swing code must run.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        LoggingSetup.configure();
        Logger.getLogger(LoggingSetup.ROOT_LOGGER).info("Chess GUI starting.");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChessFrame().setVisible(true);
            }
        });
    }
}
