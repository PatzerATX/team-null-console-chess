package chess.gui;

import chess.ai.MoveStrategy;
import chess.logging.GameLogger;
import chess.model.Move;
import chess.model.PieceColor;
import chess.model.Player;
import chess.model.Position;
import chess.pieces.Piece;
import chess.pieces.PromotionPiece;
import chess.service.Game;
import chess.service.GameListener;
import chess.service.GameStatus;
import chess.service.MoveResult;

import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Connects the Swing view to the game: the "controller" in Model-View-Controller.
 *
 * <p>It turns clicks into moves, asks the computer for its moves, and updates
 * the panels when the game tells it something changed. All the chess rules stay in {@link Game} and
 * {@link chess.service.ChessRules}; this class only decides what to ask them
 * and what to show.</p>
 */
public final class GameController implements GameListener {

    /** Logger for GUI events. */
    private static final Logger LOG = Logger.getLogger("chess.gui");

    /** How long the computer waits before moving, so its move doesn't appear instantly. */
    private static final int COMPUTER_DELAY_MS = 450;

    /** The main window, for centering dialogs. */
    private final Window owner;

    /** Draws the board. */
    private final BoardPanel boardPanel;

    /** Shows turn, status, captures and history. */
    private final SidePanel sidePanel;

    /** Writes events to the log file. */
    private final GameLogger gameLogger = new GameLogger();

    /** Random numbers for the computer. */
    private final Random random = new Random();

    /** The computer's strategy for each side it plays; empty in a two-player game. */
    private final Map<PieceColor, MoveStrategy> computer = new EnumMap<>(PieceColor.class);

    /** The current game. */
    private Game game;

    /** The choices from the New Game dialog. */
    private GameSettings settings = GameSettings.defaults();

    /** The square of the piece the user has picked up, or {@code null}. */
    private Position selected;

    /** {@code true} while the computer is about to move; clicks are ignored then. */
    private boolean thinking;

    /** Waits briefly, then makes the computer's move; {@code null} when not waiting. */
    private Timer computerTimer;

    /**
     * Creates the controller and starts the first game.
     *
     * @param owner      the main window
     * @param boardPanel the board view
     * @param sidePanel  the side view
     */
    public GameController(Window owner, BoardPanel boardPanel, SidePanel sidePanel) {
        this.owner = owner;
        this.boardPanel = boardPanel;
        this.sidePanel = sidePanel;
        boardPanel.setController(this);
        startGame();
    }

    /**
     * Shows the New Game dialog and starts a game with the choices.
     */
    public void newGame() {
        GameSettings chosen = NewGameDialog.ask(owner, settings);
        if (chosen == null) {
            return;
        }
        settings = chosen;
        startGame();
    }

    /** Starts the current game again from the beginning, with the same settings. */
    public void restart() {
        startGame();
    }

    /** Builds a new {@link Game} from the settings and starts it. */
    private void startGame() {
        if (computerTimer != null) {
            computerTimer.stop();
        }
        thinking = false;
        selected = null;
        if (game != null) {
            game.removeListener(this);
            game.removeListener(gameLogger);
        }
        computer.clear();
        for (PieceColor color : PieceColor.values()) {
            if (settings.isComputer(color)) {
                computer.put(color, settings.getDifficulty().createStrategy(random));
            }
        }
        game = new Game(settings.nameFor(PieceColor.WHITE), settings.nameFor(PieceColor.BLACK));
        game.addListener(gameLogger);
        game.addListener(this);
        game.start();
        LOG.info("Settings: " + describe(PieceColor.WHITE) + " vs " + describe(PieceColor.BLACK));
        setStatus("New game. " + game.getCurrentPlayer().getName() + " moves first.");
        computerTurnIfNeeded();
    }

    /**
     * Describes who plays a side, for the side panel and log.
     *
     * @param color the side
     * @return such as "Computer, Hard (3 moves ahead)"
     */
    private String describe(PieceColor color) {
        String name = game.getPlayer(color).getName();
        MoveStrategy strategy = computer.get(color);
        if (strategy != null) {
            return name + ", " + strategy.getName();
        }
        return name;
    }

    /**
     * Handles a click on a square: pick up a piece, or move the picked-up piece there.
     *
     * @param square the clicked square
     */
    public void squareClicked(Position square) {
        if (game.isOver() || thinking || computer.containsKey(game.getCurrentTurn())) {
            return;
        }
        Piece piece = game.getBoard().getPiece(square);
        boolean ownPiece = piece != null && piece.getColor() == game.getCurrentTurn();
        if (selected == null) {
            if (ownPiece) {
                select(square);
            } else if (piece == null) {
                sidePanel.setStatus("That square is empty. Click one of your pieces first.", SidePanel.Tone.ERROR);
            } else {
                sidePanel.setStatus("That's your opponent's piece. Click one of your own pieces.", SidePanel.Tone.ERROR);
            }
            return;
        }
        if (square.equals(selected)) {
            select(null);
            return;
        }
        if (ownPiece && !isCastlingTarget(square)) {
            select(square);
            return;
        }
        PromotionPiece promotion = PromotionPiece.QUEEN;
        if (game.isPromotionMove(selected, square)) {
            promotion = PromotionDialog.ask(owner, game.getCurrentTurn());
        }
        Position from = selected;
        select(null);
        play(game.tryMove(from, square, promotion));
    }

    /**
     * Reports whether a square is a castling destination for the selected king.
     * Castling destinations are empty, so this is just a safeguard.
     *
     * @param square the square
     * @return {@code true} if a castling move ends there
     */
    private boolean isCastlingTarget(Position square) {
        for (Move move : game.getLegalMoves(selected)) {
            if (move.getType().isCastle() && move.getTo().equals(square)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Picks up a piece (or puts it down) and shows where it can go.
     *
     * @param square the piece's square, or {@code null} to clear the selection
     */
    private void select(Position square) {
        selected = square;
        if (square == null) {
            boardPanel.setSelection(null, new ArrayList<>());
            return;
        }
        List<Move> moves = game.getLegalMoves(square);
        boardPanel.setSelection(square, moves);
        Piece piece = game.getBoard().getPiece(square);
        if (moves.isEmpty()) {
            String reason = "That " + piece.getName().toLowerCase() + " has no legal moves.";
            if (game.isInCheck(game.getCurrentTurn())) {
                reason += " Your king is in check.";
            }
            sidePanel.setStatus(reason, SidePanel.Tone.WARNING);
        } else {
            sidePanel.setStatus(piece.getColor().displayName() + " " + piece.getName().toLowerCase()
                    + " on " + square + " selected. Click a highlighted square.", SidePanel.Tone.INFO);
        }
    }

    /**
     * Shows the result of a move attempt and lets the computer reply.
     *
     * @param result the result from {@link Game#tryMove}
     */
    private void play(MoveResult result) {
        if (!result.isSuccess()) {
            sidePanel.setStatus(result.getMessage(), SidePanel.Tone.ERROR);
            return;
        }
        setStatus(result.getMessage());
        if (game.isOver()) {
            JOptionPane.showMessageDialog(owner, result.getMessage() + "\nUse New Game or Restart to play again.",
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        computerTurnIfNeeded();
    }

    /**
     * Shows a message, colored by the game's state.
     *
     * @param message the message
     */
    private void setStatus(String message) {
        SidePanel.Tone tone = SidePanel.Tone.INFO;
        if (game.isOver()) {
            tone = SidePanel.Tone.RESULT;
        } else if (game.getStatus() == GameStatus.CHECK) {
            tone = SidePanel.Tone.WARNING;
        }
        sidePanel.setStatus(message, tone);
    }

    /**
     * If it's the computer's turn, waits briefly and then makes its move.
     *
     * <p>A {@link Timer} runs the action once after the delay, on Swing's
     * event thread, so the player's own move is drawn before the computer
     * starts thinking. Restart stops the timer so an old move can't be played
     * in a new game.</p>
     */
    private void computerTurnIfNeeded() {
        final PieceColor side = game.getCurrentTurn();
        final MoveStrategy strategy = computer.get(side);
        if (game.isOver() || strategy == null) {
            return;
        }
        thinking = true;
        computerTimer = new Timer(COMPUTER_DELAY_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                thinking = false;
                Move move = strategy.chooseMove(game.getBoard().copy(), side, game.getLastMove());
                if (move != null) {
                    play(game.tryMove(move));
                }
            }
        });
        computerTimer.setRepeats(false);
        computerTimer.start();
    }

    /** Redraws everything from the game's current state. */
    private void refresh() {
        boardPanel.setBoard(game.getBoard());
        Position check = null;
        if (game.getStatus() == GameStatus.CHECK || game.getStatus() == GameStatus.CHECKMATE) {
            check = game.getBoard().findKing(game.getCurrentTurn());
        }
        boardPanel.setCheckSquare(check);
        sidePanel.setTurn(game.getCurrentTurn(), game.getCurrentPlayer().getName());
        sidePanel.setPlayers(describe(PieceColor.WHITE), describe(PieceColor.BLACK));
        sidePanel.setCaptured(game.getBoard().getCapturedPieces(PieceColor.BLACK),
                game.getBoard().getCapturedPieces(PieceColor.WHITE));
        sidePanel.setHistory(game.getHistoryLines());
    }

    /** Clears the board highlights and redraws for a new game. */
    @Override
    public void onGameStarted() {
        boardPanel.setSelection(null, new ArrayList<>());
        refresh();
    }

    /**
     * Redraws the board after a move.
     *
     * @param move   the move played
     * @param status the status afterwards
     */
    @Override
    public void onMoveMade(Move move, GameStatus status) {
        refresh();
    }

    /**
     * Shows the result in place of the turn indicator. The message box is
     * shown by {@link #play(MoveResult)} once the board has been updated.
     *
     * @param result checkmate or stalemate
     * @param winner the winner, or {@code null} for a draw
     */
    @Override
    public void onGameOver(GameStatus result, Player winner) {
        if (result == GameStatus.CHECKMATE) {
            sidePanel.setGameOver(winnerText(winner));
        } else {
            sidePanel.setGameOver("Draw by stalemate");
        }
    }

    /**
     * Words the winner, leaving out the color when the name already is the color.
     *
     * @param winner the winning player
     * @return such as "Ryan (White) wins" or "Black wins"
     */
    private static String winnerText(Player winner) {
        String color = winner.getColor().displayName();
        if (winner.getName().equals(color)) {
            return color + " wins";
        }
        return winner.getName() + " (" + color + ") wins";
    }

    /**
     * Returns the current game, for tests and the menus.
     *
     * @return the game
     */
    public Game getGame() {
        return game;
    }
}
