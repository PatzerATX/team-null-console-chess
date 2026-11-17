package chess.gui;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The main window: the board, the side panel and the menus.
 */
@SuppressWarnings("serial") // Swing windows are never saved to a file, so they need no serial ID.
public final class ChessFrame extends JFrame {

    /** Runs the game. */
    private final GameController controller;

    /** Creates the window and starts a game. */
    public ChessFrame() {
        super("Chess");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        BoardPanel board = new BoardPanel();
        SidePanel side = new SidePanel();
        getContentPane().add(board, BorderLayout.CENTER);
        getContentPane().add(side, BorderLayout.EAST);
        controller = new GameController(this, board, side);
        connectButtons(side);
        setJMenuBar(buildMenus());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    /**
     * Tells the side panel's buttons what to do when clicked.
     *
     * @param side the side panel
     */
    private void connectButtons(SidePanel side) {
        side.getNewGameButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.newGame();
            }
        });
        side.getRestartButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.restart();
            }
        });
    }

    /**
     * Builds the Game menu.
     *
     * @return the menu bar
     */
    private JMenuBar buildMenus() {
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        JMenuItem newGame = item("New Game…", KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcut));
        newGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.newGame();
            }
        });
        JMenuItem restart = item("Restart", KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcut));
        restart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.restart();
            }
        });
        JMenuItem exit = item("Exit", KeyStroke.getKeyStroke(KeyEvent.VK_Q, shortcut));
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        JMenu gameMenu = new JMenu("Game");
        gameMenu.add(newGame);
        gameMenu.add(restart);
        gameMenu.addSeparator();
        gameMenu.add(exit);

        JMenuBar bar = new JMenuBar();
        bar.add(gameMenu);
        return bar;
    }

    /**
     * Makes a menu item with a keyboard shortcut.
     *
     * @param text the item text
     * @param key  its keyboard shortcut
     * @return the item
     */
    private static JMenuItem item(String text, KeyStroke key) {
        JMenuItem item = new JMenuItem(text);
        item.setAccelerator(key);
        return item;
    }

    /** Asks before closing the program. */
    private void exit() {
        int answer = JOptionPane.showConfirmDialog(this, "Quit chess?", "Exit",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer == JOptionPane.OK_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    /**
     * Returns the controller, for screenshots and tests.
     *
     * @return the controller
     */
    public GameController getController() {
        return controller;
    }
}
