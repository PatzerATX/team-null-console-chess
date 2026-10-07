package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * The superclass for all six chess pieces.
 *
 * <p>Every piece has a color and a position, prints as a two-letter symbol
 * such as {@code wR}, and can move to a new square. Each subclass decides
 * <i>where</i> it can move by overriding {@link #possibleMoves(Board)}.</p>
 *
 * <p>The sliding pieces (rook, bishop and queen) share the
 * {@link #slide(Board, int[][])} helper, which walks in straight lines
 * until it reaches the edge of the board or another piece.</p>
 */
public abstract class Piece {

    /** The side this piece belongs to. It never changes. */
    private final PieceColor color;

    /** The square this piece is on. It changes each time the piece moves. */
    private Position position;

    /**
     * Creates a piece of the given color on the given square.
     * Only subclasses can call this, because {@code Piece} is abstract.
     *
     * @param color    the side the piece belongs to
     * @param position the square the piece starts on
     * @throws IllegalArgumentException if color or position is null
     */
    protected Piece(PieceColor color, Position position) {
        if (color == null || position == null) {
            throw new IllegalArgumentException("A piece needs a color and a position.");
        }
        this.color = color;
        this.position = position;
    }

    /**
     * Returns every square this piece could move to from its current position.
     * Each subclass overrides this with its own movement rules.
     *
     * <p>The result follows the piece's movement pattern, path blocking and
     * captures. It does not check whether a move leaves the king in check.</p>
     *
     * @param board the current board, used to see which squares are occupied
     * @return a list of destination squares, empty if the piece can't move
     */
    public abstract List<Position> possibleMoves(Board board);

    /**
     * Returns the one-letter code for this kind of piece, such as {@code 'R'} for a rook.
     *
     * @return the piece letter used in the board display
     */
    protected abstract char getLetter();

    /**
     * Returns the two-character symbol shown on the board, such as {@code wR} or {@code bp}.
     *
     * @return the color prefix followed by the piece letter
     */
    public String getSymbol() {
        return "" + color.getPrefix() + getLetter();
    }

    /**
     * Checks whether this piece could move to the given square in its pattern.
     *
     * @param board       the current board
     * @param destination the square to check
     * @return {@code true} if the destination is in {@link #possibleMoves(Board)}
     */
    public boolean canMoveTo(Board board, Position destination) {
        return possibleMoves(board).contains(destination);
    }

    /**
     * Updates this piece's position after the board has accepted a move.
     * This does not check the rules; {@code Game} and {@code Board} do that first.
     *
     * @param newPosition the square the piece now occupies
     * @throws IllegalArgumentException if newPosition is null
     */
    public void move(Position newPosition) {
        if (newPosition == null) {
            throw new IllegalArgumentException("A piece can't move to a missing square.");
        }
        this.position = newPosition;
    }

    /**
     * Collects the squares reached by sliding in each given direction.
     * In each direction it keeps stepping over empty squares and stops when it
     * reaches the edge of the board or another piece. An enemy piece's square
     * is included (a capture); a friendly piece's square is not.
     *
     * @param board      the current board
     * @param directions pairs of {rowChange, colChange}, such as {@code {-1, 0}} for up
     * @return all reachable squares in those directions
     */
    protected List<Position> slide(Board board, int[][] directions) {
        List<Position> moves = new ArrayList<>();
        for (int[] direction : directions) {
            Position next = position.offset(direction[0], direction[1]);
            while (next != null) {
                Piece occupant = board.getPiece(next);
                if (occupant == null) {
                    moves.add(next);
                } else {
                    if (isEnemy(occupant)) {
                        moves.add(next);
                    }
                    break;
                }
                next = next.offset(direction[0], direction[1]);
            }
        }
        return moves;
    }

    /**
     * Checks whether another piece belongs to the other side.
     *
     * @param other the piece to compare with
     * @return {@code true} if other is not null and is the opposite color
     */
    protected boolean isEnemy(Piece other) {
        return other != null && other.getColor() == color.opposite();
    }

    /**
     * Checks whether this piece may end its move on the given square:
     * the square must be empty or hold an enemy piece.
     *
     * @param board  the current board
     * @param square the square to check
     * @return {@code true} if the square is empty or has an enemy on it
     */
    protected boolean canLandOn(Board board, Position square) {
        Piece occupant = board.getPiece(square);
        return occupant == null || isEnemy(occupant);
    }

    /**
     * Returns the side this piece belongs to.
     *
     * @return {@code WHITE} or {@code BLACK}
     */
    public PieceColor getColor() {
        return color;
    }

    /**
     * Returns the square this piece is on.
     *
     * @return the current position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Returns the piece's board symbol, so printing a piece shows {@code wR}.
     *
     * @return the same text as {@link #getSymbol()}
     */
    @Override
    public String toString() {
        return getSymbol();
    }
}
