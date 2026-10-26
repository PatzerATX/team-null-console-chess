# Console Chess

**CS3354 Object-Oriented Programming – Project Phase 1**
**Team:** Ryan Ashworth

A two-player chess game played in the terminal. This is a **simplified chess prototype**, built as a foundation for an entry-level AI chess program and for a graphical version in Phase 2. The game logic is kept separate from console input and output, so the same classes can drive a GUI later.

---

## Requirements

- **Java 17 or newer** (developed and tested with OpenJDK 26)
- No external libraries, database or network access

## Entry point

```
chess.console.ChessApp
```

## How to run

### In IntelliJ IDEA

1. Open the `console-chess` folder as a project.
2. Open `src/chess/console/ChessApp.java`.
3. Click the green run button next to `main`.
4. Play in the Run window at the bottom of the screen.

### From the command line

From the project folder:

```bash
javac -d out $(find src -name "*.java")
java -cp out chess.console.ChessApp
```

## How to play

1. Enter a name for each player, or press Enter to skip.
2. White moves first. Enter a move as **`FROM TO`**, for example `E2 E4`.
   - Lowercase works (`e2 e4`), as do `E2-E4` and `E2E4`.
3. To capture, move onto a square holding an opponent's piece.
4. The game ends when a king is captured.

### Board display

Files **A–H** run across the top and ranks **8–1** down the side. Each piece is shown as its color (`w` or `b`) followed by its letter: `p` pawn, `R` rook, `N` knight, `B` bishop, `Q` queen, `K` king. Empty squares are shown as `##`.

```
     A   B   C   D   E   F   G   H
 8  bR  bN  bB  bQ  bK  bB  bN  bR  8
 7  bp  bp  bp  bp  bp  bp  bp  bp  7
 6  ##  ##  ##  ##  ##  ##  ##  ##  6
 5  ##  ##  ##  ##  ##  ##  ##  ##  5
 4  ##  ##  ##  ##  ##  ##  ##  ##  4
 3  ##  ##  ##  ##  ##  ##  ##  ##  3
 2  wp  wp  wp  wp  wp  wp  wp  wp  2
 1  wR  wN  wB  wQ  wK  wB  wN  wR  1
     A   B   C   D   E   F   G   H
```

### Commands

| Command | What it does |
|---|---|
| `help` | Shows the move format and the list of commands |
| `history` | Lists every move made so far |
| `captured` | Shows each side's captured pieces |
| `restart` | Starts a new game with the same players |
| `quit` | Exits the program |

## Completed features

- 8 × 8 board display with file and rank labels and the required piece symbols
- All six piece types with correct basic movement and captures:
  - **Pawn:** one square forward, two from its starting rank, diagonal captures only
  - **Rook / Bishop / Queen:** straight or diagonal lines, blocked by pieces in the way
  - **Knight:** L-shaped jumps over other pieces
  - **King:** one square in any direction
- Alternating turns, starting with White
- Captured-piece tracking, shown under the board and with the `captured` command
- **Pawn promotion:** a pawn reaching the far rank automatically becomes a queen
- **Check announcement:** "Black is in check!" after a move that attacks the king
- Move history with the `history` command
- Restarting a game with the `restart` command
- Input validation with clear messages. An invalid move or command never crashes the program and never changes the board or the turn. Rejected cases include:
  - Invalid squares or format (`Z9 E4`, `E2`, `hello`)
  - Moving from an empty square
  - Moving the opponent's piece
  - Moving onto your own piece
  - A piece "moving" to the same square
  - A move the piece can't make (`E2 E5`)
  - A blocked path (`A1 A3` at the start)
- Clean exit with `quit` (or end of input)

## Known limitations

This is a simplified prototype. The following rules are **not** implemented:

- **Check is announced but not enforced.** A player may make a move that leaves their own king in check.
- **No checkmate or stalemate detection.** The game ends when a king is captured instead.
- **No castling** and **no en passant**.
- **Promotion is always to a queen.** The player can't choose another piece.
- No draw rules (threefold repetition, fifty-move rule).
- No saving or loading games, and no computer opponent.

## Sample session

```
=== Console Chess (simplified prototype) ===
Enter moves like E2 E4. Type help for commands, or quit to exit.

Name for White (press Enter to skip): Ryan
Name for Black (press Enter to skip):

Ryan (White) to move: e2 e4
White pawn moves E2 to E4.
Black to move: f7 f6
Black pawn moves F7 to F6.
Ryan (White) to move: d1 h5
White queen moves D1 to H5. Black is in check!
Black to move: e8 e9
Invalid input: "E9" is not on the board. Use a letter A-H and a number 1-8, like E2.
Black to move: g7 g6
Black pawn moves G7 to G6.
Ryan (White) to move: Z9 E4
Invalid input: "Z9" is not on the board. Use a letter A-H and a number 1-8, like E2.
Ryan (White) to move: a1 a3
Invalid move: The White rook's path from A1 to A3 is blocked.
Ryan (White) to move: history
Moves so far:
  1. White pawn E2 - E4
  2. Black pawn F7 - F6
  3. White queen D1 - H5
  4. Black pawn G7 - G6
Ryan (White) to move: quit
Thanks for playing!
```

(The board is printed after every successful move; it is left out above to save space.)

### Suggested moves to try

| Input | Expected result |
|---|---|
| `E2 E4`, then `E7 E5` | Both accepted; White moves next |
| `E2 E4`, `D7 D5`, `E4 D5` | White pawn captures the black pawn |
| `A1 A3` (at the start) | Rejected: the rook's path is blocked |
| `G1 F3` | Accepted: the knight jumps over the pawns |
| `E2 E5` / `E7 E5` (White's turn) / `E2 E1` / `E3 E4` / `E2 E2` | Each rejected with a different message |
| `Z9 E4`, `E2`, `quit` | Invalid input handled; `quit` exits cleanly |
| `E2 E4`, `F7 F6`, `D1 H5` | "Black is in check!" |

## Project structure

```
src/chess/
├── model/      Board, Player, Position, PieceColor   – the game's data
├── pieces/     Piece (abstract) and Pawn, Rook, Knight,
│               Bishop, Queen, King                    – movement rules
├── service/    Game, MoveResult                       – turn handling and rule checks
└── console/    ChessApp, ConsoleUI, InputParser,
                Command                                – keyboard input and screen output
```

- **Inheritance and polymorphism:** `Piece` is an abstract superclass with six subclasses. Each subclass overrides `possibleMoves(Board)`, and `Game` calls it through `Piece` references without knowing the piece type. Shared movement code (`slide()` for rook, bishop and queen; `step()` for knight and king) lives in `Piece`.
- **Separation of logic and I/O:** only the `console` package reads input or prints output. `Game` returns a `MoveResult` that the interface displays, so a Phase 2 GUI can reuse `Game`, `Board` and the pieces unchanged.

## Documentation

- **Javadoc:** open `docs/index.html` in a browser.
- **UML class diagram:** `uml/class-diagram.png` (source: `uml/class-diagram.mmd`).
