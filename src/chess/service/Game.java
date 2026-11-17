package chess.service;

import chess.model.Board;
import chess.model.Move;
import chess.model.MoveType;
import chess.model.PieceColor;
import chess.model.Player;
import chess.model.Position;
import chess.pieces.Piece;
import chess.pieces.PromotionPiece;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Runs a game of chess: holds the board and both players, tracks whose turn it
 * is and the move history, checks every move against the rules, and tells its
 * listeners what happened.
 *
 * <p>Game never reads input or prints output. It returns a {@link MoveResult}
 * and notifies {@link GameListener}s (the Observer pattern), so the console,
 * the Swing GUI and the logger all work with the same class. The rules
 * themselves live in {@link ChessRules}.</p>
 *
 * <p>All standard rules are implemented, including king safety, castling, en
 * passant, promotion to a chosen piece, checkmate and stalemate. Draws by
 * repetition, the fifty-move rule and insufficient material are not.</p>
 */
public class Game {

    /** The board with all the pieces. */
    private final Board board;

    /** The player with the white pieces. */
    private final Player whitePlayer;

    /** The player with the black pieces. */
    private final Player blackPlayer;

    /** Every move played so far, in order. */
    private final List<Move> moveHistory;

    /** Everything that wants to hear about changes to this game. */
    private final List<GameListener> listeners;

    /** The color whose turn it is. */
    private PieceColor currentTurn;

    /** The game's status for the side to move. */
    private GameStatus status;

    /** The winner after checkmate, or {@code null} while playing or after stalemate. */
    private Player winner;

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
        listeners = new ArrayList<>();
        start();
    }

    /**
     * Sets up the starting position, gives White the first move, and clears
     * the winner and move history. Also used to restart a game.
     * It is {@code final} because the constructor calls it, so subclasses can't change it.
     */
    public final void start() {
        board.initialize();
        resetState(PieceColor.WHITE);
    }

    /**
     * Replaces the board with a custom position, used by the tests to set up
     * stalemate and special moves quickly. The history is cleared.
     *
     * @param pieces the pieces to place, each on its own position
     * @param toMove the side to move first
     * @throws IllegalArgumentException if either king is missing
     */
    public void loadPosition(List<Piece> pieces, PieceColor toMove) {
        board.clear();
        for (Piece piece : pieces) {
            board.placePiece(piece);
        }
        if (board.findKing(PieceColor.WHITE) == null || board.findKing(PieceColor.BLACK) == null) {
            start();
            throw new IllegalArgumentException("A position needs both kings.");
        }
        resetState(toMove);
    }

    /**
     * Resets the turn, history and status after the board has been set up,
     * then tells the listeners a new game has started.
     *
     * @param toMove the side to move first
     */
    private void resetState(PieceColor toMove) {
        currentTurn = toMove;
        moveHistory.clear();
        winner = null;
        status = ChessRules.statusFor(board, currentTurn, null);
        for (GameListener listener : listeners) {
            listener.onGameStarted();
        }
    }

    /**
     * Returns every legal move for the piece on a square. Only the current
     * player's pieces have moves, and there are none after the game ends.
     * The GUI uses this to highlight where a selected piece can go.
     *
     * @param from the square of the piece
     * @return the legal moves, possibly empty
     */
    public List<Move> getLegalMoves(Position from) {
        Piece piece = board.getPiece(from);
        if (isOver() || piece == null || piece.getColor() != currentTurn) {
            return new ArrayList<>();
        }
        return ChessRules.legalMovesFrom(board, from, getLastMove());
    }

    /**
     * Returns every legal move for the side to move.
     *
     * @return all legal moves, empty after the game ends
     */
    public List<Move> getAllLegalMoves() {
        if (isOver()) {
            return new ArrayList<>();
        }
        return ChessRules.legalMoves(board, currentTurn, getLastMove());
    }

    /**
     * Checks whether moving between two squares is a legal pawn promotion, so
     * the GUI knows to ask which piece the player wants.
     *
     * @param from the pawn's square
     * @param to   the destination on the far rank
     * @return {@code true} if this legal move promotes a pawn
     */
    public boolean isPromotionMove(Position from, Position to) {
        for (Move move : getLegalMoves(from)) {
            if (move.matches(from, to) && move.isPromotion()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tries a move, promoting to a queen if it's a promotion.
     *
     * @param from the square of the piece to move
     * @param to   the destination square
     * @return the result, with a message either way
     */
    public MoveResult tryMove(Position from, Position to) {
        return tryMove(from, to, PromotionPiece.QUEEN);
    }

    /**
     * Tries a move, such as one chosen by the computer opponent.
     *
     * @param move the move to try
     * @return the result, with a message either way
     */
    public MoveResult tryMove(Move move) {
        PromotionPiece choice = move.getPromotion();
        if (choice == null) {
            choice = PromotionPiece.QUEEN;
        }
        return tryMove(move.getFrom(), move.getTo(), choice);
    }

    /**
     * Tries to move the current player's piece from one square to another.
     *
     * <p>The move is accepted only if it is in the piece's list of legal moves
     * from {@link ChessRules}. If it isn't, the reason is worked out for the
     * message, nothing changes, and listeners are told it was rejected.</p>
     *
     * @param from      the square of the piece to move
     * @param to        the destination square
     * @param promotion the piece to promote to if this is a promotion
     * @return a successful result describing the move, or a failure explaining why not
     */
    public MoveResult tryMove(Position from, Position to, PromotionPiece promotion) {
        String problem = findProblem(from, to);
        if (problem != null) {
            for (GameListener listener : listeners) {
                listener.onMoveRejected(from, to, problem);
            }
            return MoveResult.failure(problem);
        }
        Move move = findLegalMove(from, to);
        if (move.isPromotion() && promotion != null) {
            move = move.withPromotion(promotion);
        }
        return play(move);
    }

    /**
     * Returns why a move is not allowed, or {@code null} if it is legal.
     *
     * @param from the starting square
     * @param to   the destination square
     * @return a message for the player, or {@code null}
     */
    private String findProblem(Position from, Position to) {
        if (isOver()) {
            return "The game is over. Start a new game to keep playing.";
        }
        if (from.equals(to)) {
            return "A piece must move to a different square.";
        }
        Piece piece = board.getPiece(from);
        if (piece == null) {
            return "There is no piece on " + from + ".";
        }
        if (piece.getColor() != currentTurn) {
            return "The " + describe(piece) + " on " + from + " isn't yours. It's "
                    + currentTurn.displayName() + "'s turn.";
        }
        Piece target = board.getPiece(to);
        if (target != null && target.getColor() == currentTurn) {
            return "You can't move onto your own " + target.getName().toLowerCase() + " on " + to + ".";
        }
        if (findLegalMove(from, to) != null) {
            return null;
        }
        return explainIllegal(piece, from, to);
    }

    /**
     * Explains why a move by the current player's piece is illegal.
     *
     * @param piece the piece being moved
     * @param from  the starting square
     * @param to    the destination square
     * @return a message for the player
     */
    private String explainIllegal(Piece piece, Position from, Position to) {
        boolean kingTriesToCastle = piece.isKing() && from.getRow() == to.getRow()
                && Math.abs(to.getCol() - from.getCol()) == 2;
        if (kingTriesToCastle) {
            return "You can't castle now. The king and rook must not have moved, the squares between "
                    + "them must be empty, and the king can't be in, pass through or land in check.";
        }
        if (piece.canMoveTo(board, to)) {
            if (status == GameStatus.CHECK) {
                return "Your king is in check, and that move doesn't get it out of check.";
            }
            return "That move would leave your king in check.";
        }
        if (!board.isPathClear(from, to)) {
            return "The " + describe(piece) + "'s path from " + from + " to " + to + " is blocked.";
        }
        return "A " + piece.getName().toLowerCase() + " can't move from " + from + " to " + to + ".";
    }

    /**
     * Finds the legal move between two squares, if there is one.
     *
     * @param from the starting square
     * @param to   the destination square
     * @return the matching legal move, or {@code null}
     */
    private Move findLegalMove(Position from, Position to) {
        for (Move move : getLegalMoves(from)) {
            if (move.matches(from, to)) {
                return move;
            }
        }
        return null;
    }

    /**
     * Carries out a legal move, updates the turn, status and history, and
     * notifies the listeners.
     *
     * @param move a move that has passed every check
     * @return a successful result describing what happened
     */
    private MoveResult play(Move move) {
        PieceColor mover = currentTurn;
        ChessRules.applyMove(board, move);
        currentTurn = mover.opposite();
        status = ChessRules.statusFor(board, currentTurn, move);

        String suffix = "";
        if (status == GameStatus.CHECKMATE) {
            suffix = "#";
            winner = getPlayer(mover);
        } else if (status == GameStatus.CHECK) {
            suffix = "+";
        }
        Move recorded = move.withSuffix(suffix);
        moveHistory.add(recorded);

        String message = describeMove(recorded) + statusMessage();
        for (GameListener listener : listeners) {
            listener.onMoveMade(recorded, status);
        }
        if (status.isGameOver()) {
            for (GameListener listener : listeners) {
                listener.onGameOver(status, winner);
            }
        }
        return MoveResult.success(message, recorded);
    }

    /**
     * Describes a move in a sentence, such as "White knight moves G1 to F3."
     *
     * @param move the move to describe
     * @return the description, ending with a period
     */
    private String describeMove(Move move) {
        Piece piece = move.getPiece();
        String side = piece.getColor().displayName();
        if (move.getType() == MoveType.CASTLE_KINGSIDE) {
            return side + " castles kingside.";
        }
        if (move.getType() == MoveType.CASTLE_QUEENSIDE) {
            return side + " castles queenside.";
        }
        String text = describe(piece) + " moves " + move.getFrom() + " to " + move.getTo();
        if (move.isCapture()) {
            text += " and captures " + describe(move.getCaptured());
            if (move.getType() == MoveType.EN_PASSANT) {
                text += " en passant";
            }
        }
        if (move.isPromotion()) {
            text += " and is promoted to a " + move.getPromotion().getDisplayName().toLowerCase();
        }
        return text + ".";
    }

    /**
     * Returns a message about check, checkmate or stalemate after a move.
     *
     * @return the text to add, or an empty string
     */
    private String statusMessage() {
        if (status == GameStatus.CHECKMATE) {
            return " Checkmate! " + winner.getName() + " wins.";
        }
        if (status == GameStatus.STALEMATE) {
            return " Stalemate! " + currentTurn.displayName() + " has no legal moves. The game is a draw.";
        }
        if (status == GameStatus.CHECK) {
            return " " + currentTurn.displayName() + " is in check!";
        }
        return "";
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
     * Registers a listener to be told about changes to this game.
     *
     * @param listener the listener to add
     */
    public void addListener(GameListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Stops telling a listener about changes.
     *
     * @param listener the listener to remove
     */
    public void removeListener(GameListener listener) {
        listeners.remove(listener);
    }

    /**
     * Checks whether a side's king is in check right now.
     *
     * @param color the side to check
     * @return {@code true} if that king is under attack
     */
    public boolean isInCheck(PieceColor color) {
        return ChessRules.isInCheck(board, color);
    }

    /**
     * Returns the game's status for the side to move.
     *
     * @return in progress, check, checkmate or stalemate
     */
    public GameStatus getStatus() {
        return status;
    }

    /**
     * Returns whether the game has ended by checkmate or stalemate.
     *
     * @return {@code true} if no more moves can be made
     */
    public boolean isOver() {
        return status.isGameOver();
    }

    /**
     * Returns the winner.
     *
     * @return the winning player after checkmate, or {@code null}
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * Returns the most recent move.
     *
     * @return the last move played, or {@code null} at the start
     */
    public Move getLastMove() {
        if (moveHistory.isEmpty()) {
            return null;
        }
        return moveHistory.get(moveHistory.size() - 1);
    }

    /**
     * Returns the moves played so far. The list can be read but not changed.
     *
     * @return the moves in the order they were played
     */
    public List<Move> getMoveHistory() {
        return Collections.unmodifiableList(moveHistory);
    }

    /**
     * Returns the move history as numbered lines in the Phase 1 style, such
     * as "1. White pawn E2 - E4". The console's history command and the GUI's
     * move list both show these lines.
     *
     * @return one line per move, oldest first
     */
    public List<String> getHistoryLines() {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < moveHistory.size(); i++) {
            lines.add((i + 1) + ". " + moveHistory.get(i).toDescription());
        }
        return lines;
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
