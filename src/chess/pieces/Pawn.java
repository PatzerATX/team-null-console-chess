package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * A pawn, the only piece whose movement depends on its color.
 *
 * <ul>
 *   <li>Moves one square forward into an empty square.</li>
 *   <li>From its starting rank, may move two squares forward if both are empty.</li>
 *   <li>Captures one square diagonally forward, and only when an enemy is there.</li>
 * </ul>
 *
 * <p>White pawns move toward rank 8 and black pawns toward rank 1, using
 * {@link PieceColor#getForwardDirection()}. A pawn that reaches the far rank
 * is promoted to a queen automatically. En passant is not supported.</p>
 */
public class Pawn extends Piece {

    /** Array row of White's starting pawn rank (rank 2). */
    private static final int WHITE_START_ROW = 6;

    /** Array row of Black's starting pawn rank (rank 7). */
    private static final int BLACK_START_ROW = 1;

    /**
     * Creates a pawn.
     *
     * @param color    the side the pawn belongs to
     * @param position the square it starts on
     */
    public Pawn(PieceColor color, Position position) {
        super(color, position);
    }

    /**
     * Returns every square the pawn can move to: forward moves into empty
     * squares and diagonal captures of enemy pieces.
     *
     * @param board the current board
     * @return reachable squares for this pawn
     */
    @Override
    public List<Position> possibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();
        int forward = getColor().getForwardDirection();

        // One square forward, only if empty
        Position oneAhead = getPosition().offset(forward, 0);
        if (oneAhead != null && board.isEmpty(oneAhead)) {
            moves.add(oneAhead);

            // Two squares forward from the starting rank, only if both are empty
            Position twoAhead = getPosition().offset(2 * forward, 0);
            if (isOnStartingRank() && twoAhead != null && board.isEmpty(twoAhead)) {
                moves.add(twoAhead);
            }
        }

        // Diagonal captures, only if an enemy is there
        int[] sides = {-1, 1};
        for (int side : sides) {
            Position diagonal = getPosition().offset(forward, side);
            if (diagonal != null && isEnemy(board.getPiece(diagonal))) {
                moves.add(diagonal);
            }
        }
        return moves;
    }

    /**
     * Checks whether the pawn is still on its color's starting rank,
     * which means it hasn't moved yet and may move two squares.
     *
     * @return {@code true} if on rank 2 (white) or rank 7 (black)
     */
    private boolean isOnStartingRank() {
        int startRow;
        if (getColor() == PieceColor.WHITE) {
            startRow = WHITE_START_ROW;
        } else {
            startRow = BLACK_START_ROW;
        }
        return getPosition().getRow() == startRow;
    }

    /**
     * Checks whether the pawn has reached the far rank: rank 8 for white,
     * rank 1 for black. {@code Game} then replaces it with a queen.
     *
     * @return {@code true} if the pawn is on its last rank
     */
    @Override
    public boolean isPromotable() {
        int lastRow;
        if (getColor() == PieceColor.WHITE) {
            lastRow = 0;
        } else {
            lastRow = Position.BOARD_SIZE - 1;
        }
        return getPosition().getRow() == lastRow;
    }

    /**
     * Returns the pawn's letter. The spec uses a lowercase p, as in {@code wp}.
     *
     * @return {@code 'p'}
     */
    @Override
    protected char getLetter() {
        return 'p';
    }
}
