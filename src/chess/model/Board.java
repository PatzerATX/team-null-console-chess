package chess.model;

import chess.pieces.Piece;

/**
 * The 8 &times; 8 chess board, stored as a grid of pieces.
 *
 * <p>Each cell holds a {@link Piece} or {@code null} for an empty square.
 * Cells are indexed with {@link Position#getRow()} and {@link Position#getCol()},
 * so {@code grid[0][0]} is A8 and {@code grid[7][7]} is H1.</p>
 *
 * <p>This is the basic version: it can hold pieces and report what is on a
 * square. Setting up the starting position, moving pieces and tracking
 * captures are added in a later step.</p>
 */
public class Board {

    /** The squares of the board; {@code null} means empty. */
    private final Piece[][] grid;

    /** Creates an empty board with no pieces on it. */
    public Board() {
        grid = new Piece[Position.BOARD_SIZE][Position.BOARD_SIZE];
    }

    /**
     * Returns the piece on a square.
     *
     * @param position the square to look at
     * @return the piece there, or {@code null} if the square is empty
     */
    public Piece getPiece(Position position) {
        return grid[position.getRow()][position.getCol()];
    }

    /**
     * Checks whether a square has no piece on it.
     *
     * @param position the square to check
     * @return {@code true} if the square is empty
     */
    public boolean isEmpty(Position position) {
        return getPiece(position) == null;
    }

    /**
     * Puts a piece on the board at the piece's own position,
     * replacing anything already there.
     *
     * @param piece the piece to place
     * @throws IllegalArgumentException if piece is null
     */
    public void placePiece(Piece piece) {
        if (piece == null) {
            throw new IllegalArgumentException("Can't place a missing piece.");
        }
        Position position = piece.getPosition();
        grid[position.getRow()][position.getCol()] = piece;
    }
}
