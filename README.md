# CheckersBot
## Java-based Checkers AI using Minimax, Alpha-Beta Pruning, Iterative Deepening, and Transposition Tables
### Overview
CheckersBot is a Java project that aims to create an advanced and efficient Checkers AI using minimax algorithm with alpha-beta pruning, iterative deepening, and transposition tables. The project also includes a graphical user interface (GUI) built with JavaFX, allowing users to play against the AI.

### Class Structure
The project consists of the following classes:

**Board:** Represents the checkers board in bitboard format.
**Move:** Stores a move as an initial position and an ArrayList of direction enums.
**Zobrist:** Creates a unique hash for each board position.
**TranspositionEntry:** Stores the best moves made for a specific position and the age of entries.
**Controller:** Handles the GUI interactions and updates.
**Main:** Runs the CheckersBot application.
**CheckersAI:** Implements the minimax algorithm, a HashMap using the Zobrist hashing technique, and transposition entries representing the transposition table.
### Setup & Installation
Clone the repository to your local machine.
Open the project in your favorite Java IDE (e.g., IntelliJ IDEA, Eclipse).
Ensure you have JavaFX installed and configured in your IDE.
Run the Main class to start the CheckersBot application.
### How to Play
Start the CheckersBot application by running the Main class.
The JavaFX GUI will open, displaying a checkers board.
You can choose to play against the AI, or watch an AI vs. AI game by selecting the appropriate option from the GUI.
If playing against the AI, make your move by clicking on a piece and then clicking on a valid destination square.
The AI will make its move after you've completed yours.
### Contributors
Feel free to contribute to this project by submitting a pull request or opening an issue on the project's GitHub repository.

### License
This project is licensed under the MIT License. See the LICENSE file for details.
