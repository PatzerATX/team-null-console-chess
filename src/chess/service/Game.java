package chess.service;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Player;
import chess.model.Position;
import chess.pieces.King;
import chess.pieces.Piece;
import chess.pieces.Queen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Runs a game of chess: holds the board and both players, tracks whose turn
 * it is, and checks every move against the rules before carrying it out.
 *
 * <p>Game never reads input or prints output. It returns a {@link MoveResult}
 * that the user interface displays, so the same class works for the console
 * now and a GUI later.</p>
 *
 * <p>This is a simplified prototype. Pawns promote to a queen automatically,
 * and the game announces when a king is in check. Checkmate, stalemate,
 * castling and en passant are not implemented, and a player may make a move
 * that leaves their own king in check. The game ends when a king is captured.</p>
 */
public class Game {

    /** The board with all the pieces. */
    private final Board board;

    /** The player with the white pieces. */
    private final Player whitePlayer;

    /** The player with the black pieces. */
    private final Player blackPlayer;

    /** The color whose turn it is. */
    private PieceColor currentTurn;

    /** The winner once a king is captured, or {@code null} while the game is in progress. */
    private Player winner;

    /** Every move made this game, in order, such as "1. White pawn E2 - E4". */
    private final List<String> moveHistory;

    /**
     * Creates a game between two players and sets up the starting position.
     *
     * @param whiteName name for the white player, or blank for "White"
     * @param blackName name for the black player, or blank for "Black"
     */
    public Game(String whiteName, String blackName) {
        board = new Board();
        whitePlayer = new Player(whiteName, PieceColor.WHITE);
        blackPlayer = new Player(blackName, PieceColor.BLACK);
        moveHistory = new ArrayList<>();
        start();
    }

    /**
     * Sets up the starting position, gives White the first move, and clears
     * any winner and the move history. Also used to restart a game.
     * It is {@code final} because the constructor calls it, so subclasses can't change it.
     */
    public final void start() {
        board.initialize();
        currentTurn = PieceColor.WHITE;
        winner = null;
        moveHistory.clear();
    }

    /**
     * Tries to move the current player's piece from one square to another.
     *
     * <p>The move is checked in this order: game not over, the piece moves,
     * there is a piece to move, it belongs to the current player, the
     * destination isn't a friendly piece, the path is clear, and the piece's
     * movement rules allow it. If any check fails, nothing changes and the
     * result explains why.</p>
     *
     * <p>After a legal move, a pawn on the far rank is promoted to a queen,
     * the move is added to the history, and the turn passes to the other
     * player, who is warned if their king is in check.</p>
     *
     * @param from the square of the piece to move
     * @param to   the destination square
     * @return a successful result with a description of the move,
     *         or a failed result explaining why the move was rejected
     */
    public MoveResult tryMove(Position from, Position to) {
        if (isOver()) {
            return MoveResult.failure("The game is over. " + winner.getName() + " won.");
        }
        if (from.equals(to)) {
            return MoveResult.failure("A piece must move to a different square.");
        }

        Piece piece = board.getPiece(from);
        if (piece == null) {
            return MoveResult.failure("There is no piece on " + from + ".");
        }
        if (piece.getColor() != currentTurn) {
            return MoveResult.failure("The " + describe(piece) + " on " + from + " isn't yours. It's "
                    + currentTurn.displayName() + "'s turn.");
        }

        Piece target = board.getPiece(to);
        if (target != null && target.getColor() == currentTurn) {
            return MoveResult.failure("You can't move onto your own " + target.getName().toLowerCase()
                    + " on " + to + ".");
        }
        if (!board.isPathClear(from, to)) {
            return MoveResult.failure("The " + describe(piece) + "'s path from " + from + " to " + to
                    + " is blocked.");
        }
        if (!piece.canMoveTo(board, to)) {
            return MoveResult.failure("A " + piece.getName().toLowerCase() + " can't move from "
                    + from + " to " + to + ".");
        }

        return applyMove(piece, from, to);
    }

    /**
     * Carries out a move that has already passed every check: moves the piece,
     * handles capture and promotion, records the move, and switches turns.
     *
     * @param piece the piece being moved
     * @param from  its starting square
     * @param to    its destination
     * @return a successful result describing everything that happened
     */
    private MoveResult applyMove(Piece piece, Position from, Position to) {
        Piece captured = board.movePiece(from, to);
        String message = describe(piece) + " moves " + from + " to " + to;
        if (captured != null) {
            message += " and captures " + describe(captured);
        }

        boolean promoted = piece.isPromotable();
        if (promoted) {
            board.placePiece(new Queen(piece.getColor(), to));
            message += " and is promoted to a queen";
        }
        recordMove(piece, from, to, captured, promoted);

        if (captured instanceof King) {
            winner = getCurrentPlayer();
            return MoveResult.success(message + ". " + winner.getName() + " wins!");
        }

        currentTurn = currentTurn.opposite();
        message += ".";
        if (isInCheck(currentTurn)) {
            message += " " + currentTurn.displayName() + " is in check!";
        }
        return MoveResult.success(message);
    }

    /**
     * Adds a move to the history in a short readable form, such as
     * "3. White knight F3 x E5" for a capture or "5. White pawn A7 - A8 =Q"
     * for a promotion.
     *
     * @param piece    the piece that moved
     * @param from     its starting square
     * @param to       its destination
     * @param captured the piece it took, or {@code null}
     * @param promoted whether it was promoted to a queen
     */
    private void recordMove(Piece piece, Position from, Position to, Piece captured, boolean promoted) {
        String separator;
        if (captured != null) {
            separator = " x ";
        } else {
            separator = " - ";
        }
        String entry = (moveHistory.size() + 1) + ". " + describe(piece) + " " + from + separator + to;
        if (promoted) {
            entry += " =Q";
        }
        moveHistory.add(entry);
    }

    /**
     * Checks whether a side's king could be captured by an enemy piece on the next move.
     * Each enemy piece is asked for its possible moves, so this reuses every
     * piece's own movement rules.
     *
     * @param color the side whose king to check
     * @return {@code true} if any enemy piece can reach that king
     */
    public boolean isInCheck(PieceColor color) {
        Position kingSquare = board.findKing(color);
        if (kingSquare == null) {
            return false;
        }
        for (Piece enemy : board.getPieces(color.opposite())) {
            if (enemy.canMoveTo(board, kingSquare)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the moves made so far. The list can be read but not changed.
     *
     * @return move descriptions in the order they were played
     */
    public List<String> getMoveHistory() {
        return Collections.unmodifiableList(moveHistory);
    }

    /**
     * Describes a piece in words, such as "White pawn".
     *
     * @param piece the piece to describe
     * @return its color and type
     */
    private String describe(Piece piece) {
        return piece.getColor().displayName() + " " + piece.getName().toLowerCase();
    }

    /**
     * Returns whether the game has ended because a king was captured.
     *
     * @return {@code true} if there is a winner
     */
    public boolean isOver() {
        return winner != null;
    }

    /**
     * Returns the winner.
     *
     * @return the winning player, or {@code null} if the game is still going
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * Returns the board, so the user interface can display it.
     *
     * @return the game board
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Returns the color whose turn it is.
     *
     * @return {@code WHITE} or {@code BLACK}
     */
    public PieceColor getCurrentTurn() {
        return currentTurn;
    }

    /**
     * Returns the player whose turn it is.
     *
     * @return the white or black player
     */
    public Player getCurrentPlayer() {
        return getPlayer(currentTurn);
    }

    /**
     * Returns the player for a color.
     *
     * @param color the side to look up
     * @return the player controlling that side
     */
    public Player getPlayer(PieceColor color) {
        if (color == PieceColor.WHITE) {
            return whitePlayer;
        }
        return blackPlayer;
    }
}
