package chess.ai;

import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;
import chess.service.ChessRules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * The Medium and Hard levels: searches ahead with <b>minimax</b> and
 * <b>alpha-beta pruning</b>.
 *
 * <p>Minimax assumes both players play their best. On its own turn the
 * computer picks the move with the highest score; on the opponent's turn it
 * assumes the reply with the lowest score (for the computer). At the search
 * depth, positions are scored by {@link BoardEvaluator}.</p>
 *
 * <p>This version is written as "negamax": a score for one side is the
 * negative of the score for the other, so one method handles both turns.</p>
 *
 * <p>Alpha-beta pruning skips moves that can't change the answer: once one
 * reply refutes a move, the other replies to it don't need checking. Trying
 * captures first makes that happen sooner.</p>
 */
public class MinimaxAI implements MoveStrategy {

    /** Score for delivering checkmate; far above any material score. */
    static final int MATE_SCORE = 100_000;

    /** How many moves (both sides counted) to look ahead. */
    private final int depth;

    /** Shuffles moves so equal choices vary from game to game. */
    private final Random random;

    /**
     * Creates a searching player.
     *
     * @param depth  how many moves ahead to look (at least 1)
     * @param random random number generator for breaking ties
     */
    public MinimaxAI(int depth, Random random) {
        if (depth < 1) {
            throw new IllegalArgumentException("Depth must be at least 1.");
        }
        this.depth = depth;
        this.random = random;
    }

    /**
     * Searches every legal move and returns the best one.
     *
     * @param board    a copy of the board
     * @param side     the side to move
     * @param lastMove the previous move, or {@code null}
     * @return the best move found, or {@code null} if there are none
     */
    @Override
    public Move chooseMove(Board board, PieceColor side, Move lastMove) {
        List<Move> moves = orderedMoves(board, side, lastMove);
        Move best = null;
        int bestScore = Integer.MIN_VALUE;
        int alpha = -MATE_SCORE - 1;
        int beta = MATE_SCORE + 1;
        for (Move move : moves) {
            Board next = board.copy();
            ChessRules.applyMove(next, move);
            int score = -search(next, side.opposite(), move, depth - 1, -beta, -alpha, 1);
            if (score > bestScore) {
                bestScore = score;
                best = move;
            }
            if (score > alpha) {
                alpha = score;
            }
        }
        return best;
    }

    /**
     * Scores a position for the side to move by searching ahead.
     *
     * @param board    the position
     * @param side     the side to move
     * @param lastMove the move that led here
     * @param left     how many more moves to search
     * @param alpha    the score the side to move is already sure of
     * @param beta     the score the opponent won't allow it to beat
     * @param ply      how many moves from the root; finds faster mates first
     * @return the score for {@code side}
     */
    private int search(Board board, PieceColor side, Move lastMove,
                       int left, int alpha, int beta, int ply) {
        List<Move> moves = orderedMoves(board, side, lastMove);
        if (moves.isEmpty()) {
            if (ChessRules.isInCheck(board, side)) {
                return -MATE_SCORE + ply;
            }
            return 0;
        }
        if (left == 0) {
            return BoardEvaluator.evaluate(board, side);
        }
        int best = -MATE_SCORE - 1;
        for (Move move : moves) {
            Board next = board.copy();
            ChessRules.applyMove(next, move);
            int score = -search(next, side.opposite(), move, left - 1, -beta, -alpha, ply + 1);
            if (score > best) {
                best = score;
            }
            if (score > alpha) {
                alpha = score;
            }
            if (alpha >= beta) {
                break;
            }
        }
        return best;
    }

    /**
     * Returns the legal moves in a good order for pruning: shuffled, then
     * captures of valuable pieces first.
     *
     * @param board    the position
     * @param side     the side to move
     * @param lastMove the previous move
     * @return the moves, best guesses first
     */
    private List<Move> orderedMoves(Board board, PieceColor side, Move lastMove) {
        List<Move> moves = new ArrayList<>(ChessRules.legalMoves(board, side, lastMove));
        Collections.shuffle(moves, random);
        moves.sort(new Comparator<Move>() {
            @Override
            public int compare(Move a, Move b) {
                return orderingScore(b) - orderingScore(a);
            }
        });
        return moves;
    }

    /**
     * Guesses how promising a move is before searching it: capture the most
     * valuable piece with the least valuable attacker, and promote.
     *
     * @param move the move
     * @return a higher number for moves to try first
     */
    private static int orderingScore(Move move) {
        int score = 0;
        if (move.isCapture()) {
            score += 10 * move.getCaptured().getValue() - move.getPiece().getValue();
        }
        if (move.isPromotion()) {
            score += 8000;
        }
        return score;
    }

    /**
     * Returns how many moves ahead this player looks.
     *
     * @return the search depth
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Returns the menu name.
     *
     * @return a name such as "Hard (3 moves ahead)"
     */
    @Override
    public String getName() {
        if (depth == 1) {
            return "Medium (1 move ahead)";
        }
        return "Hard (" + depth + " moves ahead)";
    }
}
