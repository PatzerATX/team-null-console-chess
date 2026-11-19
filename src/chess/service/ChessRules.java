package chess.service;

import chess.model.Board;
import chess.model.Move;
import chess.model.MoveType;
import chess.model.PieceColor;
import chess.model.Position;
import chess.pieces.Piece;
import chess.pieces.PromotionPiece;

import java.util.ArrayList;
import java.util.List;

/**
 * The single source of truth for which moves are legal.
 *
 * <p>Every part of the program that needs legal moves calls this class: move
 * validation in {@link Game}, highlighting in the GUI, checkmate and stalemate
 * detection, and the computer opponent. In Phase 1 that logic was spread over
 * several methods; keeping it in one place means they can never disagree.</p>
 *
 * <p>Legal moves are found in two stages. First, each piece supplies the
 * squares its movement pattern allows ({@link Piece#possibleMoves(Board)}), and
 * this class adds the special moves that depend on game history: castling and
 * en passant. Then each candidate is tried on a copy of the board, and any move
 * that would leave the mover's own king under attack is removed.</p>
 *
 * <p>All methods are static because the rules don't belong to any one game;
 * they only need a board, the side to move and the previous move.</p>
 */
public final class ChessRules {

    /** Column of the king and of each rook at the start of the game. */
    private static final int KING_COL = 4;

    /** Column of the rook on the kingside (H-file). */
    private static final int KINGSIDE_ROOK_COL = 7;

    /** Column of the rook on the queenside (A-file). */
    private static final int QUEENSIDE_ROOK_COL = 0;

    /** Prevents creating objects; this class only holds static rule methods. */
    private ChessRules() {
    }

    /**
     * Returns every legal move for one side.
     *
     * @param board    the current board
     * @param side     the side to move
     * @param lastMove the previous move in the game, or {@code null}; needed for en passant
     * @return all legal moves, empty if the side has none (checkmate or stalemate)
     */
    public static List<Move> legalMoves(Board board, PieceColor side, Move lastMove) {
        List<Move> moves = new ArrayList<>();
        for (Piece piece : board.getPieces(side)) {
            moves.addAll(legalMovesFrom(board, piece.getPosition(), lastMove));
        }
        return moves;
    }

    /**
     * Returns every legal move for the piece on one square.
     *
     * @param board    the current board
     * @param from     the square of the piece to move
     * @param lastMove the previous move in the game, or {@code null}
     * @return the piece's legal moves, empty if the square is empty or the piece is stuck
     */
    public static List<Move> legalMovesFrom(Board board, Position from, Move lastMove) {
        List<Move> legal = new ArrayList<>();
        Piece piece = board.getPiece(from);
        if (piece == null) {
            return legal;
        }
        for (Move candidate : candidateMoves(board, piece, lastMove)) {
            if (!leavesKingInCheck(board, candidate)) {
                legal.add(candidate);
            }
        }
        return legal;
    }

    /**
     * Collects a piece's moves before checking king safety: its normal
     * pattern, plus castling and en passant where they apply.
     *
     * @param board    the current board
     * @param piece    the piece to move
     * @param lastMove the previous move, or {@code null}
     * @return the candidate moves
     */
    private static List<Move> candidateMoves(Board board, Piece piece, Move lastMove) {
        List<Move> moves = new ArrayList<>();
        Position from = piece.getPosition();
        for (Position to : piece.possibleMoves(board)) {
            PromotionPiece promotion = null;
            if (piece.isPawn() && to.getRow() == lastRow(piece.getColor())) {
                promotion = PromotionPiece.QUEEN;
            }
            moves.add(new Move(from, to, piece, board.getPiece(to), MoveType.NORMAL, promotion));
        }
        if (piece.isPawn()) {
            Move enPassant = enPassantMove(board, piece, lastMove);
            if (enPassant != null) {
                moves.add(enPassant);
            }
        }
        if (piece.isKing()) {
            moves.addAll(castlingMoves(board, piece));
        }
        return moves;
    }

    /**
     * Returns the en passant capture available to a pawn, if any. It's only
     * possible immediately after an enemy pawn moves two squares and lands
     * right beside this pawn.
     *
     * @param board    the current board
     * @param pawn     the pawn that might capture
     * @param lastMove the previous move, or {@code null}
     * @return the en passant move, or {@code null} if it isn't available
     */
    private static Move enPassantMove(Board board, Piece pawn, Move lastMove) {
        if (lastMove == null || !lastMove.isPawnDoubleStep()
                || lastMove.getPiece().getColor() == pawn.getColor()) {
            return null;
        }
        Position from = pawn.getPosition();
        Position enemySquare = lastMove.getTo();
        boolean sameRow = enemySquare.getRow() == from.getRow();
        boolean adjacent = Math.abs(enemySquare.getCol() - from.getCol()) == 1;
        if (!sameRow || !adjacent) {
            return null;
        }
        Piece enemyPawn = board.getPiece(enemySquare);
        if (enemyPawn == null || !enemyPawn.isPawn()) {
            return null;
        }
        Position to = new Position(from.getRow() + pawn.getColor().getForwardDirection(),
                enemySquare.getCol());
        return new Move(from, to, pawn, enemyPawn, MoveType.EN_PASSANT, null);
    }

    /**
     * Returns the castling moves available to a king. Castling requires that
     * the king and that rook have never moved, the squares between them are
     * empty, and the king is not in check, does not pass through an attacked
     * square and does not land on one.
     *
     * @param board the current board
     * @param king  the king that might castle
     * @return zero, one or two castling moves
     */
    private static List<Move> castlingMoves(Board board, Piece king) {
        List<Move> moves = new ArrayList<>();
        Position from = king.getPosition();
        PieceColor color = king.getColor();
        PieceColor enemy = color.opposite();
        if (king.hasMoved() || from.getCol() != KING_COL || isInCheck(board, color)) {
            return moves;
        }
        int row = from.getRow();
        if (rookReady(board, row, KINGSIDE_ROOK_COL, color)
                && emptySquares(board, row, 5, 6)
                && !isSquareAttacked(board, new Position(row, 5), enemy)
                && !isSquareAttacked(board, new Position(row, 6), enemy)) {
            moves.add(new Move(from, new Position(row, 6), king, null, MoveType.CASTLE_KINGSIDE, null));
        }
        if (rookReady(board, row, QUEENSIDE_ROOK_COL, color)
                && emptySquares(board, row, 1, 3)
                && !isSquareAttacked(board, new Position(row, 3), enemy)
                && !isSquareAttacked(board, new Position(row, 2), enemy)) {
            moves.add(new Move(from, new Position(row, 2), king, null, MoveType.CASTLE_QUEENSIDE, null));
        }
        return moves;
    }

    /**
     * Checks whether an unmoved rook of the given color stands on a square.
     *
     * @param board the current board
     * @param row   the rook's row
     * @param col   the rook's column
     * @param color the king's color
     * @return {@code true} if a matching rook is there and has never moved
     */
    private static boolean rookReady(Board board, int row, int col, PieceColor color) {
        Piece rook = board.getPiece(new Position(row, col));
        return rook != null && rook.getColor() == color
                && rook.getTypeLetter() == 'R' && !rook.hasMoved();
    }

    /**
     * Checks whether every square in a row between two columns is empty.
     *
     * @param board    the current board
     * @param row      the row to check
     * @param firstCol the first column, inclusive
     * @param lastCol  the last column, inclusive
     * @return {@code true} if all those squares are empty
     */
    private static boolean emptySquares(Board board, int row, int firstCol, int lastCol) {
        for (int col = firstCol; col <= lastCol; col++) {
            if (!board.isEmpty(new Position(row, col))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether a move would leave the moving side's king under attack,
     * by trying it on a copy of the board.
     *
     * @param board the current board (not changed)
     * @param move  the move to test
     * @return {@code true} if the mover's king would be in check afterward
     */
    public static boolean leavesKingInCheck(Board board, Move move) {
        Board trial = board.copy();
        applyMove(trial, move);
        return isInCheck(trial, move.getPiece().getColor());
    }

    /**
     * Checks whether any piece of the attacking side could capture on a square.
     *
     * @param board    the current board
     * @param square   the square to check
     * @param attacker the side that might be attacking
     * @return {@code true} if the square is attacked
     */
    public static boolean isSquareAttacked(Board board, Position square, PieceColor attacker) {
        for (Piece piece : board.getPieces(attacker)) {
            if (piece.attackedSquares(board).contains(square)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether a side's king is under attack.
     *
     * @param board the current board
     * @param color the side whose king to check
     * @return {@code true} if that king is in check, {@code false} if there is no king
     */
    public static boolean isInCheck(Board board, PieceColor color) {
        Position king = board.findKing(color);
        return king != null && isSquareAttacked(board, king, color.opposite());
    }

    /**
     * Carries out a move on a board, including the extra steps of special
     * moves: the rook's jump when castling, removing the passed pawn in en
     * passant, and replacing a promoted pawn. It does not check legality.
     *
     * @param board the board to change
     * @param move  a move that has already been checked
     */
    public static void applyMove(Board board, Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        board.movePiece(from, to);
        int row = from.getRow();
        if (move.getType() == MoveType.CASTLE_KINGSIDE) {
            board.movePiece(new Position(row, KINGSIDE_ROOK_COL), new Position(row, 5));
        } else if (move.getType() == MoveType.CASTLE_QUEENSIDE) {
            board.movePiece(new Position(row, QUEENSIDE_ROOK_COL), new Position(row, 3));
        } else if (move.getType() == MoveType.EN_PASSANT) {
            board.capturePiece(new Position(row, to.getCol()));
        }
        if (move.isPromotion()) {
            PieceColor color = move.getPiece().getColor();
            board.removePiece(to);
            board.placePiece(move.getPromotion().create(color, to));
        }
    }

    /**
     * Works out the game's status for the side about to move.
     *
     * @param board    the current board
     * @param toMove   the side about to move
     * @param lastMove the move just played, or {@code null}
     * @return checkmate, stalemate, check or in progress
     */
    public static GameStatus statusFor(Board board, PieceColor toMove, Move lastMove) {
        boolean inCheck = isInCheck(board, toMove);
        boolean canMove = !legalMoves(board, toMove, lastMove).isEmpty();
        if (!canMove && inCheck) {
            return GameStatus.CHECKMATE;
        }
        if (!canMove) {
            return GameStatus.STALEMATE;
        }
        if (inCheck) {
            return GameStatus.CHECK;
        }
        return GameStatus.IN_PROGRESS;
    }

    /**
     * Returns the row a pawn of the given color promotes on.
     *
     * @param color the pawn's color
     * @return row 0 (rank 8) for white, row 7 (rank 1) for black
     */
    public static int lastRow(PieceColor color) {
        if (color == PieceColor.WHITE) {
            return 0;
        }
        return Position.BOARD_SIZE - 1;
    }
}
