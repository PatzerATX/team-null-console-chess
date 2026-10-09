package chess.model;

import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Piece;
import chess.pieces.Queen;
import chess.pieces.Rook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The 8 &times; 8 chess board, stored as a grid of pieces, plus a record of
 * every piece that has been captured.
 *
 * <p>Each cell holds a {@link Piece} or {@code null} for an empty square.
 * Cells are indexed with {@link Position#getRow()} and {@link Position#getCol()},
 * so {@code grid[0][0]} is A8 and {@code grid[7][7]} is H1.</p>
 *
 * <p>The board stores pieces and carries out moves, but it does not decide
 * whether a move is legal. {@code Game} checks the rules first and only then
 * calls {@link #movePiece(Position, Position)}.</p>
 */
public class Board {

    /** The squares of the board; {@code null} means empty. */
    private final Piece[][] grid;

    /** Every piece captured so far, in the order they were taken. */
    private final List<Piece> capturedPieces;

    /** Creates an empty board with no pieces on it. Call {@link #initialize()} to set up a game. */
    public Board() {
        grid = new Piece[Position.BOARD_SIZE][Position.BOARD_SIZE];
        capturedPieces = new ArrayList<>();
    }

    /**
     * Clears the board and places all 32 pieces in the standard starting position.
     * White fills ranks 1 and 2, and Black fills ranks 7 and 8.
     */
    public void initialize() {
        clear();
        placeBackRank(PieceColor.WHITE, 7);
        placePawns(PieceColor.WHITE, 6);
        placePawns(PieceColor.BLACK, 1);
        placeBackRank(PieceColor.BLACK, 0);
    }

    /** Removes every piece from the board and empties the captured list. */
    public void clear() {
        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            for (int col = 0; col < Position.BOARD_SIZE; col++) {
                grid[row][col] = null;
            }
        }
        capturedPieces.clear();
    }

    /**
     * Places one side's back-rank pieces in order: rook, knight, bishop,
     * queen, king, bishop, knight, rook (files A to H).
     *
     * @param color the side to place
     * @param row   the array row of that side's back rank
     */
    private void placeBackRank(PieceColor color, int row) {
        placePiece(new Rook(color, new Position(row, 0)));
        placePiece(new Knight(color, new Position(row, 1)));
        placePiece(new Bishop(color, new Position(row, 2)));
        placePiece(new Queen(color, new Position(row, 3)));
        placePiece(new King(color, new Position(row, 4)));
        placePiece(new Bishop(color, new Position(row, 5)));
        placePiece(new Knight(color, new Position(row, 6)));
        placePiece(new Rook(color, new Position(row, 7)));
    }

    /**
     * Places a full row of eight pawns for one side.
     *
     * @param color the side to place
     * @param row   the array row for the pawns
     */
    private void placePawns(PieceColor color, int row) {
        for (int col = 0; col < Position.BOARD_SIZE; col++) {
            placePiece(new Pawn(color, new Position(row, col)));
        }
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

    /**
     * Returns every piece of one color still on the board.
     *
     * @param color the side to collect
     * @return that side's pieces, scanned from A8 to H1
     */
    public List<Piece> getPieces(PieceColor color) {
        List<Piece> pieces = new ArrayList<>();
        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            for (int col = 0; col < Position.BOARD_SIZE; col++) {
                Piece piece = grid[row][col];
                if (piece != null && piece.getColor() == color) {
                    pieces.add(piece);
                }
            }
        }
        return pieces;
    }

    /**
     * Finds where one side's king is.
     *
     * @param color the side whose king to find
     * @return the king's square, or {@code null} if that king has been captured
     */
    public Position findKing(PieceColor color) {
        for (Piece piece : getPieces(color)) {
            if (piece instanceof King) {
                return piece.getPosition();
            }
        }
        return null;
    }

    /**
     * Checks whether every square strictly between two squares is empty.
     * This only applies to straight or diagonal lines; for any other pair of
     * squares (such as a knight's jump) there is nothing in between, so it
     * returns {@code true}.
     *
     * @param from the starting square
     * @param to   the ending square
     * @return {@code true} if nothing blocks the line between them
     */
    public boolean isPathClear(Position from, Position to) {
        int rowDiff = to.getRow() - from.getRow();
        int colDiff = to.getCol() - from.getCol();
        boolean straight = rowDiff == 0 || colDiff == 0;
        boolean diagonal = Math.abs(rowDiff) == Math.abs(colDiff);
        if (!straight && !diagonal) {
            return true;
        }
        int rowStep = Integer.signum(rowDiff);
        int colStep = Integer.signum(colDiff);
        Position current = from.offset(rowStep, colStep);
        while (current != null && !current.equals(to)) {
            if (!isEmpty(current)) {
                return false;
            }
            current = current.offset(rowStep, colStep);
        }
        return true;
    }

    /**
     * Moves the piece on one square to another. If an enemy piece is on the
     * destination, it is removed and added to the captured list.
     *
     * <p>This method does not check the rules. Call it only after the move
     * has been validated.</p>
     *
     * @param from the square the piece moves from
     * @param to   the square the piece moves to
     * @return the captured piece, or {@code null} if nothing was captured
     * @throws IllegalArgumentException if there is no piece on {@code from}
     */
    public Piece movePiece(Position from, Position to) {
        Piece moving = getPiece(from);
        if (moving == null) {
            throw new IllegalArgumentException("There is no piece on " + from + " to move.");
        }
        Piece captured = getPiece(to);
        if (captured != null) {
            capturedPieces.add(captured);
        }
        grid[from.getRow()][from.getCol()] = null;
        grid[to.getRow()][to.getCol()] = moving;
        moving.move(to);
        return captured;
    }

    /**
     * Returns the pieces captured so far. The list can be read but not changed.
     *
     * @return captured pieces in the order they were taken
     */
    public List<Piece> getCapturedPieces() {
        return Collections.unmodifiableList(capturedPieces);
    }

    /**
     * Returns the pieces of one color that have been captured.
     *
     * @param color the side whose lost pieces to list
     * @return that side's captured pieces, in the order they were taken
     */
    public List<Piece> getCapturedPieces(PieceColor color) {
        List<Piece> result = new ArrayList<>();
        for (Piece piece : capturedPieces) {
            if (piece.getColor() == color) {
                result.add(piece);
            }
        }
        return result;
    }
}
