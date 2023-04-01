package com.example.checkersguifinal;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    @FXML
    private Group squarePieces;

    @FXML
    private Spinner secondsSpinner;

    @FXML
    private Group checkerImages;

    @FXML
    private Button redButton;
    @FXML
    private Button blackButton;

    @FXML
    private Button newGameButton;

    @FXML
    private Group computerVision;

    @FXML
    private ProgressBar evalBar;

    @FXML
    private Text evalString;

    @FXML
    private Text buttonText;

    @FXML
    private Text bestMoveString;

    @FXML
    private Text depthString;

    @FXML
    private Text gamesSearchedString;

    @FXML
    private Text timeString;

    @FXML
    private Text gameLost;

    @FXML
    private Text gameWon;

    @FXML
    private Text gameDrawn;

    @FXML
    private Text moveCountString;

    @FXML
    private Text moveCount;

    @FXML
    private Button undoButton;

    private Move[] moves;

    private int computerTimeLimit;

    private final Image blackKing = new Image("Black_King.png");
    private final Image blackChecker = new Image("Black_Checker.png");
    private final Image redKing = new Image("Red_King.png");
    private final Image redChecker = new Image("Red_Checker.png");
    private final Image emptySpace = new Image("Empty_Space.png");

    private int[] pieces = new int[]{-1, -1, -1, -1};
    public Board.Player player = Board.Player.NONE;

    public Board.Player user;

    public Board checkersBoard = new Board(Board.Player.BLACK);;

    public LinkedList<Board> lastBoard = new LinkedList<>();

    public CheckersAI checkersAI;

    // Idly, this method animates the movement of the pieces


    public void pieceSelected(MouseEvent e) {
        boolean redPlayer = user == Board.Player.RED;
        ImageView piece = (ImageView) e.getSource();
        lookAtPiece(e);
        int index = coordsToIndex(piece.getLayoutX(), piece.getLayoutY());
        LinkedList<Move> legalMoves = checkersBoard.getLegalMoves();
        for (Move move : legalMoves) {
            if (move.getPiece() == index) {
                // Disable checkerImages user input.
                checkerImages.setDisable(true);
                int tracker;
                if (redPlayer) {
                    tracker = 31;
                } else {
                    tracker = 0;
                }
                for (Node square : squarePieces.getChildren()) {
                    if (index == tracker) {
                        if (square instanceof Rectangle) {
                            ((Rectangle) square).setFill(Color.GOLD);
                        }
                    }
                    if (redPlayer) {
                        tracker--;
                    } else {
                        tracker++;
                    }
                }
                break;
            }
        }
    }

    public void piecePlaced(MouseEvent e) {
        lastBoard.add(checkersBoard.clone());
        checkPiecePlaced(e, () -> {
            // Disable the checker images while the AI is thinking
            checkerImages.setDisable(true);

            // Run moveAI() in a separate thread
            Thread moveAIThread = new Thread(() -> {
                moveAI();

                // Update the UI on the JavaFX Application thread once moveAI() is finished
                Platform.runLater(() -> {
                    highlightLegalPieces();
                    checkerImages.setDisable(false);
                });
            });
            moveAIThread.start();
        });
    }

    public boolean checkPiecePlaced(MouseEvent e, Runnable callback) {
        Rectangle newLocation = (Rectangle) e.getSource();
        boolean result = false;
        // Checks if piece is yellow.
        if (newLocation.getFill() == Color.GOLDENROD) {
            result = true;
            int index = coordsToIndex(newLocation.getLayoutX(), newLocation.getLayoutY());
            // Find which index matches pieceIndex
            int moveIndex = indexToPiece(index);
            // Code to make move.
            checkersBoard.move(moves[moveIndex]);
            stopLookAtPiece(false);
            drawBoard();
            highlightLastMove(moves[moveIndex]);
            // Checks if game is over
            Board.State testState = checkersBoard.gameState();
            if (testState != Board.State.ONGOING) {
                if (testState == Board.State.DRAW) {
                    gameDrawn.setVisible(true);
                } else if (testState == Board.State.LOSS) {
                    gameWon.setVisible(true);
                } else {
                    gameLost.setVisible(true);
                }
            }
        }
        stopLookAtPiece(!result);
        if(result){
            if (callback != null) {
                // Call the callback to run moveAI
                callback.run();
            }
        }
        // Drop, unhighlight piece and enable images
        return result;
    }

    private void moveAI(){
        // If it is the AI's turn
        if (checkersBoard.isComputerPlayer()) {
            Move move = checkersAI.getBestMove(checkersBoard, computerTimeLimit);
            checkersBoard.move(move);
            drawBoard();
            highlightLastMove(move);
            // Checks if game is over
            Board.State testState = checkersBoard.gameState();
            if (testState != Board.State.ONGOING) {
                if (testState == Board.State.DRAW) {
                    gameDrawn.setVisible(true);
                } else if (testState == Board.State.LOSS) {
                    gameWon.setVisible(true);
                } else {
                    gameLost.setVisible(true);
                }
            }
        }
        moveCountString.setText(checkersBoard.getNumMoves() + (checkersBoard.getNumMoves() == 1 ? " move made" : " moves made"));
    }

    private void highlightLegalPieces() {
        boolean redPlayer = user == Board.Player.RED;
        LinkedList<Move> legalMoves = checkersBoard.getLegalMoves();
        HashSet<Integer> moveablePieces = new HashSet<>();
        for (Move move : legalMoves) {
            moveablePieces.add(move.getPiece());
        }
        for (int index : moveablePieces) {
            int tracker;
            if (redPlayer) {
                tracker = 31;
            } else {
                tracker = 0;
            }
            for (Node square : squarePieces.getChildren()) {
                if (index == tracker) {
                    if (square instanceof Rectangle) {
                        ((Rectangle) square).setFill(Color.PALEVIOLETRED);
                    }
                }
                if (redPlayer) {
                    tracker--;
                } else {
                    tracker++;
                }
            }
        }
    }

    private void highlightLastMove(Move lastMove) {
        Color highlightColor = Color.RED;
        List<Integer> positionsToHighlight = new ArrayList<>();
        positionsToHighlight.add(lastMove.getStart());
        System.out.println(lastMove.getDirection().size());
        System.out.println(lastMove.getIntermediatePositions());
        positionsToHighlight.addAll(lastMove.getIntermediatePositions());
        positionsToHighlight.add(lastMove.getEnd());

        boolean redPlayer = user == Board.Player.RED;

        for (Integer index : positionsToHighlight) {
            int tracker;
            if (redPlayer) {
                tracker = 31;
            } else {
                tracker = 0;
            }
            for (Node square : squarePieces.getChildren()) {
                if (index == tracker) {
                    if (square instanceof Rectangle) {
                        ((Rectangle) square).setFill(highlightColor);
                    }
                }
                if (redPlayer) {
                    tracker--;
                } else {
                    tracker++;
                }
            }
        }
    }


    private int indexToPiece(int index) {
        for(int i = 0; i < pieces.length; i++){
            if(pieces[i] == index){
                return i;
            }
        }
        return -1;
    }


    private void erasePiece(int pieceIndex, Board.Direction direction) {
        // Find index of erased piece
        int erasedPieceIndex = 0;
        boolean oddRow = (pieceIndex / 4) % 2 == 1;
        switch (direction) {
            case FORWARD_LEFT:
                if(oddRow){
                    erasedPieceIndex = pieceIndex + 4;
                } else{
                    erasedPieceIndex = pieceIndex + 3;
                }
                break;
            case FORWARD_RIGHT:
                if(oddRow){
                    erasedPieceIndex = pieceIndex + 5;
                } else{
                    erasedPieceIndex = pieceIndex + 4;
                }
                break;
            case BACKWARD_LEFT:
                if(oddRow){
                    erasedPieceIndex = pieceIndex - 4;
                } else{
                    erasedPieceIndex = pieceIndex - 5;
                }
                break;
            case BACKWARD_RIGHT:
                if(oddRow){
                    erasedPieceIndex = pieceIndex - 3;
                } else{
                    erasedPieceIndex = pieceIndex - 4;
                }
                break;
            default:
                break;
        }
        // Erase piece
        boolean redPlayer = user == Board.Player.RED;
        int tracker;
        if(redPlayer) {
            tracker = 31;
        } else {
            tracker = 0;
        }
        for (Node node : checkerImages.getChildren()) {
            if (erasedPieceIndex == tracker) {
                if(node instanceof ImageView) {
                    ((ImageView) node).setImage(emptySpace);
                    break;
                }
            }
            if(redPlayer) {
                tracker--;
            } else {
                tracker++;
            }
        }
    }



    public void lookAtPiece(MouseEvent e) {
        stopLookAtPiece(false);
        boolean redPlayer = user == Board.Player.RED;
        // Identify the location of the mouse click on board
        ImageView piece = (ImageView) e.getSource();
        int index = coordsToIndex(piece.getLayoutX(), piece.getLayoutY());
        // Find list of all legal moves
        LinkedList<Move> legalMoves = checkersBoard.getLegalMoves();
        // If piece has a legal move, calculate the appropriate offsets.
        pieces = new int[]{-1,-1,-1,-1};
        moves = new Move[4];
        int piecesIndex = 0;
        for (Move move : legalMoves) {
            if (move.getPiece() == index) {
                Board tempBoard = checkersBoard.clone();
                pieces[piecesIndex] = tempBoard.move(move);
                moves[piecesIndex] = move;
                piecesIndex++;
                int tracker;
                if(redPlayer) {
                    tracker = 31;
                } else {
                    tracker = 0;
                }
                for (Node square : squarePieces.getChildren()) {
                    for (int pieceIndex : pieces) {
                        if (pieceIndex == tracker) {
                            if(square instanceof Rectangle) {
                                ((Rectangle) square).setFill(Color.GOLDENROD);
                            }
                        }
                    }
                    if(redPlayer) {
                        tracker--;
                    } else {
                        tracker++;
                    }
                }
            }
        }
    }

    private int coordsToIndex(double dx, double dy){
        int index;
        if(user == Board.Player.RED) {
            index = coordsToIndexRed(dx, dy);
        } else {
            index = coordsToIndexBlack(dx, dy);
        }
        return index;
    }

    // Translates the coordinates of a click into the piece being clicked.
    private int coordsToIndexBlack(double dx, double dy) {
        int x = (int) dx  / 100;
        int y = (int) dy / 100;
        int index = 0;
        for(int i = 0; i < 8; i+=2){
            if(x > i){
                index++;
            }
        }
        if(y % 2 == 0){
            index--;
        }
        return (7 - y) * 4 + index;
    }

    private int coordsToIndexRed(double dx, double dy) {
        int x = (int) dx  / 100;
        int y = (int) dy / 100;
        int index = 0;
        for(int i = 0; i < 8; i+=2){
            if(x > i){
                index++;
            }
        }
        if(y % 2 == 0){
            index--;
        }
        return 31 - ((7 - y) * 4 + index);
    }

    public void stopLookAtPiece(boolean highlight) {
        for (Node square : squarePieces.getChildren()) {
            if( square instanceof Rectangle) {
                ((Rectangle) square).setFill(Color.web("#b5726f"));
            }
        }
        if(highlight){
            checkerImages.setDisable(false);
            highlightLegalPieces();
        }
    }

    // Handles the event when the user chooses to play red
    public void pickRed(ActionEvent e){
        checkersBoard = new Board(Board.Player.BLACK);
        checkersAI = new CheckersAI(Board.Player.BLACK, this, computerTimeLimit);
        player = Board.Player.RED;
        user = player;
        evalBar.setRotate(90);
        checkerImages.setDisable(false);
        drawBoard();
        moveAI();
        setUpNewGame();
        highlightLegalPieces();
    }

    // Handles the event when the user chooses to play black
    public void pickBlack(ActionEvent e){
        checkersBoard = new Board(Board.Player.RED);
        checkersAI = new CheckersAI(Board.Player.RED, this, computerTimeLimit);
        checkerImages.setDisable(false);
        player = Board.Player.BLACK;
        user = player;
        setUpNewGame();
        drawBoardBlack();
        highlightLegalPieces();
    }


    private void drawBoard(){
        if (player == Board.Player.RED) {
            drawBoardRed();
        } else {
            drawBoardBlack();
        }
    }

    // Draws the board according to the checkersBoard object
    private void drawBoardRed() {
        String[][] tempBoard = checkersBoard.bitboardsToArray();
        int pieceIndex = 4;
        int rowCounter = 0;
        for(Node node : checkerImages.getChildren()) {
            if (node instanceof ImageView) {
                rowCounter++;
                pieceIndex--;
                if (rowCounter % 5 == 0) {
                    pieceIndex += 8;
                    rowCounter = 1;
                }
                int index = pieceIndex * 2;
                int secondIndex = index % 8;
                if ((index / 8) % 2 == 0) {
                    secondIndex++;
                }
                switch (tempBoard[index / 8][secondIndex]) {
                    case "  b  ":
                        ((ImageView) node).setImage(blackChecker);
                        break;
                    case "  B  ":
                        ((ImageView) node).setImage(blackKing);
                        break;
                    case "  r  ":
                        ((ImageView) node).setImage(redChecker);
                        break;
                    case "  R  ":
                        ((ImageView) node).setImage(redKing);
                        break;
                    default:
                        ((ImageView) node).setImage(emptySpace);
                        break;
                }
            }
        }
    }

    private void drawBoardBlack() {
        String[][] tempBoard = checkersBoard.bitboardsToArray();
        int pieceIndex = 27;
        int rowCounter = 0;
        for(Node node : checkerImages.getChildren()) {
            if (node instanceof ImageView) {
                rowCounter++;
                pieceIndex++;
                if (rowCounter % 5 == 0) {
                    pieceIndex -= 8;
                    rowCounter = 1;
                }
                int index = pieceIndex * 2;
                int secondIndex = index % 8;
                if ((index / 8) % 2 == 0) {
                    secondIndex++;
                }
                switch (tempBoard[index / 8][secondIndex]) {
                    case "  b  ":
                        ((ImageView) node).setImage(blackChecker);
                        break;
                    case "  B  ":
                        ((ImageView) node).setImage(blackKing);
                        break;
                    case "  r  ":
                        ((ImageView) node).setImage(redChecker);
                        break;
                    case "  R  ":
                        ((ImageView) node).setImage(redKing);
                        break;
                    default:
                        ((ImageView) node).setImage(emptySpace);
                        break;
                }
            }
        }
    }

    // Sets the board up for a game
    private void setUpNewGame() {
        redButton.setVisible(false);
        redButton.setDisable(true);
        blackButton.setVisible(false);
        blackButton.setDisable(true);
        buttonText.setVisible(false);
        computerVision.setVisible(true);
        evalBar.setVisible(true);
        undoButton.setVisible(true);
        newGameButton.setVisible(true);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,60);
        valueFactory.setValue(1);
        secondsSpinner.setValueFactory(valueFactory);
        computerTimeLimit = (int) secondsSpinner.getValue();
        secondsSpinner.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                computerTimeLimit = (int) secondsSpinner.getValue();
            }
        });
    }

    public void undoMove(ActionEvent e){
        checkersBoard = lastBoard.getLast();
        lastBoard.removeLast();
        if(player == Board.Player.RED){
            drawBoardRed();
        } else{
            drawBoardBlack();
        }
    }

    // Restarts the entire method to create a new Game.
    public void newGame(ActionEvent e) throws IOException {
        Parent root = FXMLLoader.load((Objects.requireNonNull(Main.class.getResource("board.fxml"))));
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    public void setUpdateStats(double times, int gamesSearched, int depth, Move bestMoves, int evalL, double evalBar) {
        timeString.setText((times / 1000) + " seconds");
        gamesSearchedString.setText(gamesSearched + "");
        depthString.setText(depth + "");
        bestMoveString.setText(bestMoves.toString());
        double eval = (double) evalL / 100;
        evalString.setText(eval + "");
        if(player == Board.Player.RED) {
            this.evalBar.setProgress(evalBar);
        } else {
            this.evalBar.setProgress(1 - evalBar);
        }
    }
}