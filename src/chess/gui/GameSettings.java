package chess.gui;

import chess.ai.Difficulty;
import chess.model.PieceColor;

/**
 * The choices made in the New Game dialog. Immutable.
 */
public final class GameSettings {

    /** The human's name. */
    private final String playerName;

    /** {@code true} to play the computer, {@code false} for two people at one screen. */
    private final boolean versusComputer;

    /** The human's color when playing the computer. */
    private final PieceColor humanColor;

    /** The computer's level. */
    private final Difficulty difficulty;

    /** Name of the second human in a two-player game. */
    private final String secondName;

    /**
     * Creates a set of choices.
     *
     * @param playerName     the (first) human's name
     * @param versusComputer whether the other side is the computer
     * @param humanColor     the human's color against the computer
     * @param difficulty     the computer's level
     * @param secondName     the second human's name in a two-player game
     */
    public GameSettings(String playerName, boolean versusComputer, PieceColor humanColor,
                        Difficulty difficulty, String secondName) {
        this.playerName = playerName;
        this.versusComputer = versusComputer;
        this.humanColor = humanColor;
        this.difficulty = difficulty;
        this.secondName = secondName;
    }

    /**
     * The settings used when the program opens: you as White against the
     * Medium computer.
     *
     * @return the default settings
     */
    public static GameSettings defaults() {
        return new GameSettings("You", true, PieceColor.WHITE, Difficulty.MEDIUM, "Player 2");
    }

    /**
     * Returns the name for one side.
     *
     * @param color the side
     * @return the player's or computer's name
     */
    public String nameFor(PieceColor color) {
        if (!versusComputer) {
            if (color == PieceColor.WHITE) {
                return playerName;
            }
            return secondName;
        }
        if (color == humanColor) {
            return playerName;
        }
        return "Computer";
    }

    /**
     * Reports whether a side is played by the computer.
     *
     * @param color the side
     * @return {@code true} for the computer's side
     */
    public boolean isComputer(PieceColor color) {
        return versusComputer && color != humanColor;
    }

    /**
     * Returns the (first) human's name.
     *
     * @return the name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Reports whether this is a game against the computer.
     *
     * @return {@code true} against the computer
     */
    public boolean isVersusComputer() {
        return versusComputer;
    }

    /**
     * Returns the human's color against the computer.
     *
     * @return the color
     */
    public PieceColor getHumanColor() {
        return humanColor;
    }

    /**
     * Returns the computer's level.
     *
     * @return the difficulty
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Returns the second human's name.
     *
     * @return the name
     */
    public String getSecondName() {
        return secondName;
    }
}
