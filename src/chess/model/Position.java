package chess.model;

/**
 * A square on the chess board, stored as an array row and column.
 *
 * <p><b>Coordinate mapping:</b> row 0 is rank 8 and row 7 is rank 1;
 * column 0 is file A and column 7 is file H. For example, {@code E2}
 * is row 6, column 4.</p>
 *
 * <p>Positions are immutable and always on the board: the constructor
 * rejects any row or column outside 0&ndash;7.</p>
 */
public final class Position {

    /** Number of rows and columns on the board. */
    public static final int BOARD_SIZE = 8;

    /** Array row, where 0 is rank 8 and 7 is rank 1. */
    private final int row;

    /** Array column, where 0 is file A and 7 is file H. */
    private final int col;

    /**
     * Creates a position from an array row and column.
     *
     * @param row the array row, 0&ndash;7
     * @param col the array column, 0&ndash;7
     * @throws IllegalArgumentException if row or col is off the board
     */
    public Position(int row, int col) {
        if (!isOnBoard(row, col)) {
            throw new IllegalArgumentException(
                    "Position (" + row + ", " + col + ") is off the board.");
        }
        this.row = row;
        this.col = col;
    }

    /**
     * Creates a position from chess notation such as {@code "E2"}.
     * Letters may be upper- or lowercase, and surrounding spaces are ignored.
     *
     * @param notation a file letter A&ndash;H followed by a rank digit 1&ndash;8
     * @return the matching position
     * @throws IllegalArgumentException if the text is not a valid square
     */
    public static Position fromNotation(String notation) {
        if (notation == null) {
            throw new IllegalArgumentException("No square was entered.");
        }
        String text = notation.trim().toUpperCase();
        if (text.length() != 2) {
            throw new IllegalArgumentException(
                    "\"" + notation.trim() + "\" is not a square. Use a letter A-H and a number 1-8, like E2.");
        }
        char file = text.charAt(0);
        char rank = text.charAt(1);
        if (file < 'A' || file > 'H' || rank < '1' || rank > '8') {
            throw new IllegalArgumentException(
                    "\"" + text + "\" is not on the board. Use a letter A-H and a number 1-8, like E2.");
        }
        int col = file - 'A';
        int row = BOARD_SIZE - (rank - '0');
        return new Position(row, col);
    }

    /**
     * Checks whether a row and column are on the board.
     *
     * @param row the array row to check
     * @param col the array column to check
     * @return {@code true} if both are between 0 and 7
     */
    public static boolean isOnBoard(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    /**
     * Returns the position shifted by the given amounts, if it stays on the board.
     * Pieces use this to look at neighboring squares.
     *
     * @param rowChange the number of rows to move (negative is toward rank 8)
     * @param colChange the number of columns to move (negative is toward file A)
     * @return the new position, or {@code null} if it would be off the board
     */
    public Position offset(int rowChange, int colChange) {
        int newRow = row + rowChange;
        int newCol = col + colChange;
        if (isOnBoard(newRow, newCol)) {
            return new Position(newRow, newCol);
        } else {
            return null;
        }
    }

    /**
     * Returns the array row.
     *
     * @return the row, 0&ndash;7, where 0 is rank 8
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the array column.
     *
     * @return the column, 0&ndash;7, where 0 is file A
     */
    public int getCol() {
        return col;
    }

    /**
     * Returns the rank number shown on the board.
     *
     * @return the rank, 1&ndash;8
     */
    public int getRank() {
        return BOARD_SIZE - row;
    }

    /**
     * Returns the file letter shown on the board.
     *
     * @return the file, A to H
     */
    public char getFile() {
        return (char) ('A' + col);
    }

    /**
     * Returns this position in chess notation.
     *
     * @return text such as {@code "E2"}
     */
    @Override
    public String toString() {
        return "" + getFile() + getRank();
    }

    /**
     * Two positions are equal when they have the same row and column.
     *
     * @param other the object to compare with
     * @return {@code true} if other is a position on the same square
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Position)) {
            return false;
        }
        Position that = (Position) other;
        return row == that.row && col == that.col;
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     *
     * @return a number unique to each square
     */
    @Override
    public int hashCode() {
        return row * BOARD_SIZE + col;
    }
}
