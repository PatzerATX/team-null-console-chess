package chess.gui;

import chess.ai.Difficulty;
import chess.model.PieceColor;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Lets the user choose a game type: against the computer (color and
 * difficulty) or two players at one screen.
 */
@SuppressWarnings("serial") // Swing windows are never saved to a file, so they need no serial ID.
public final class NewGameDialog extends JDialog {

    /** The settings chosen, or {@code null} if cancelled. */
    private GameSettings result;

    /** "Play the computer" option. */
    private final JRadioButton computerMode = new JRadioButton("Play the computer");

    /** "Two players" option. */
    private final JRadioButton twoPlayerMode = new JRadioButton("Two players, one screen");

    /** The first player's name. */
    private final JTextField nameField = new JTextField(14);

    /** The second player's name. */
    private final JTextField secondField = new JTextField(14);

    /** The human's color against the computer. */
    private final JComboBox<String> colorBox = new JComboBox<>(new String[] {"White", "Black"});

    /** The computer's level. */
    private final JComboBox<Difficulty> levelBox = new JComboBox<>(Difficulty.values());

    /**
     * Builds the dialog, filled in with the previous settings.
     *
     * @param owner    the window to center over
     * @param previous the last settings used
     */
    private NewGameDialog(Window owner, GameSettings previous) {
        super(owner, "New Game", ModalityType.APPLICATION_MODAL);
        ButtonGroup modes = new ButtonGroup();
        modes.add(computerMode);
        modes.add(twoPlayerMode);
        computerMode.setSelected(previous.isVersusComputer());
        twoPlayerMode.setSelected(!previous.isVersusComputer());
        nameField.setText(previous.getPlayerName());
        secondField.setText(previous.getSecondName());
        if (previous.getHumanColor() == PieceColor.BLACK) {
            colorBox.setSelectedIndex(1);
        }
        levelBox.setSelectedItem(previous.getDifficulty());
        ActionListener modeChanged = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateEnabled();
            }
        };
        computerMode.addActionListener(modeChanged);
        twoPlayerMode.addActionListener(modeChanged);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 16, 6, 16));
        int row = 0;
        addRow(form, row++, null, computerMode);
        addRow(form, row++, "Your name:", nameField);
        addRow(form, row++, "You play:", colorBox);
        addRow(form, row++, "Difficulty:", levelBox);
        addRow(form, row++, null, twoPlayerMode);
        addRow(form, row, "Black's name:", secondField);

        JButton start = new JButton("Start");
        JButton cancel = new JButton("Cancel");
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = readSettings();
                dispose();
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancel);
        buttons.add(start);

        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(start);
        updateEnabled();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /**
     * Adds a label and a field to the form.
     *
     * @param form  the form panel
     * @param row   the row number
     * @param label the label text, or {@code null} for a full-width component
     * @param field the component
     */
    private static void addRow(JPanel form, int row, String label, java.awt.Component field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        if (label == null) {
            c.gridx = 0;
            c.gridwidth = 2;
            if (row > 0) {
                c.insets = new Insets(12, 4, 4, 4);
            }
            form.add(field, c);
            return;
        }
        c.gridx = 0;
        c.insets = new Insets(4, 26, 4, 4);
        form.add(new JLabel(label), c);
        c.gridx = 1;
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, c);
    }

    /** Greys out the fields that don't apply to the chosen mode. */
    private void updateEnabled() {
        boolean computer = computerMode.isSelected();
        colorBox.setEnabled(computer);
        levelBox.setEnabled(computer);
        secondField.setEnabled(!computer);
    }

    /**
     * Collects the choices, filling in blank names.
     *
     * @return the settings
     */
    private GameSettings readSettings() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            name = "You";
        }
        String second = secondField.getText().trim();
        if (second.isEmpty()) {
            second = "Player 2";
        }
        PieceColor color = PieceColor.WHITE;
        if (colorBox.getSelectedIndex() == 1) {
            color = PieceColor.BLACK;
        }
        return new GameSettings(name, computerMode.isSelected(), color,
                (Difficulty) levelBox.getSelectedItem(), second);
    }

    /**
     * Shows the dialog and waits.
     *
     * @param owner    the window to center over
     * @param previous the last settings, used as the starting values
     * @return the new settings, or {@code null} if cancelled
     */
    public static GameSettings ask(Window owner, GameSettings previous) {
        NewGameDialog dialog = new NewGameDialog(owner, previous);
        dialog.setVisible(true);
        return dialog.result;
    }
}
