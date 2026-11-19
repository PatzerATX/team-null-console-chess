package chess.ai;

import java.util.Random;

/**
 * The computer's difficulty levels. Each one knows how to build its
 * {@link MoveStrategy}, so the GUI only has to pick a level.
 */
public enum Difficulty {

    /** A random legal move. */
    EASY("Easy", "Random moves"),

    /** Looks one move ahead: grabs material but ignores your reply. */
    MEDIUM("Medium", "Looks 1 move ahead"),

    /** Looks three moves ahead with minimax and alpha-beta pruning. */
    HARD("Hard", "Looks 3 moves ahead");

    /** Name shown in menus. */
    private final String label;

    /** One-line description for the new-game dialog. */
    private final String description;

    /**
     * Creates a difficulty entry.
     *
     * @param label       the menu name
     * @param description the one-line description
     */
    Difficulty(String label, String description) {
        this.label = label;
        this.description = description;
    }

    /**
     * Builds the strategy for this level, wrapped in two decorators: the
     * inner one makes sure a bad answer can never break the game, and the
     * outer one logs every move.
     *
     * @param random the random number generator the strategy should use
     * @return the strategy
     */
    public MoveStrategy createStrategy(Random random) {
        MoveStrategy inner;
        if (this == EASY) {
            inner = new RandomAI(random);
        } else if (this == MEDIUM) {
            inner = new MinimaxAI(1, random);
        } else {
            inner = new MinimaxAI(3, random);
        }
        return new LoggingMoveStrategy(new SafeMoveSelector(inner, random));
    }

    /**
     * Returns the menu name.
     *
     * @return such as "Hard"
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the one-line description.
     *
     * @return such as "Looks 3 moves ahead"
     */
    public String getDescription() {
        return description;
    }

    /**
     * Shows the label and description, for combo boxes.
     *
     * @return such as "Hard - Looks 3 moves ahead"
     */
    @Override
    public String toString() {
        return label + " - " + description;
    }
}
