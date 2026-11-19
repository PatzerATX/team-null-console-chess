package chess.ai;

import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;

/**
 * A way of choosing a move for the computer. This is the <b>Strategy
 * pattern</b>: the GUI asks "what move?" without knowing which strategy it is
 * talking to, so a new difficulty level can be added without changing the GUI
 * or the game.
 */
public interface MoveStrategy {

    /**
     * Chooses a move.
     *
     * @param board    a copy of the current board; the strategy may change it freely
     * @param side     the side to move
     * @param lastMove the previous move, or {@code null}; needed for en passant
     * @return a legal move, or {@code null} if there are none
     */
    Move chooseMove(Board board, PieceColor side, Move lastMove);

    /**
     * Returns a short name for menus and logs.
     *
     * @return the strategy's name, such as "Hard (3 moves ahead)"
     */
    String getName();
}
