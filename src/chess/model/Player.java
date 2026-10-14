package chess.model;

/**
 * One of the two people playing, identified by a name and the color they play.
 */
public class Player {

    /** The name shown in messages, such as "Ryan" or "White". */
    private final String name;

    /** The side this player controls. */
    private final PieceColor color;

    /**
     * Creates a player. A blank name is replaced with the color's name,
     * so every player always has something to display.
     *
     * @param name  the player's name, or blank to use "White" or "Black"
     * @param color the side the player controls
     * @throws IllegalArgumentException if color is null
     */
    public Player(String name, PieceColor color) {
        if (color == null) {
            throw new IllegalArgumentException("A player needs a color.");
        }
        this.color = color;
        if (name == null || name.isBlank()) {
            this.name = color.displayName();
        } else {
            this.name = name.trim();
        }
    }

    /**
     * Returns the player's name.
     *
     * @return the display name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the side this player controls.
     *
     * @return {@code WHITE} or {@code BLACK}
     */
    public PieceColor getColor() {
        return color;
    }

    /**
     * Returns the name and color, such as "Ryan (White)". If the player didn't
     * enter a name, the name already is the color, so it returns just "White".
     *
     * @return a short description of the player
     */
    @Override
    public String toString() {
        if (name.equals(color.displayName())) {
            return name;
        }
        return name + " (" + color.displayName() + ")";
    }
}
