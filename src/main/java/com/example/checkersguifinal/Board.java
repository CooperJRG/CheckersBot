package com.example.checkersguifinal;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedList;

public class Board {
    // Create several bitboards that will store the positional data for the
    // different types of pieces.
    private long redCheckers = 0L;
    private long redKings = 0L;
    private long blackCheckers = 0L;
    private long blackKings = 0L;

    private int blackCheckerCount = 0;

    private int redCheckerCount = 0;

    private int blackKingCount = 0;

    private int redKingCount = 0;

    private int drawCount = 0;

    private SecureRandom randomNum = new SecureRandom();

    private Zorbist zorbist;

    // We can ignore half of the squares as they are not playable, meaning only
    // 32 bits are needed.
    private final int NUM_SQUARES = 32;

    private final int DRAW_THRESHOLD = 80;

    public Board clone() {
        return new Board(redCheckers, blackCheckers, redKings, blackKings, redCheckerCount,
                redKingCount, blackKingCount, blackCheckerCount, drawCount, zorbist, currentPlayer, computerPlayer, movesMade);
    }

    int getNumMoves(){
        return movesMade;
    }

    // Returns the resulting index of a piece.
    public int indexInDirection(int currentPieceIndex, Direction direction) {
        Move testMove = new Move(currentPieceIndex, direction, false);
        if(pieceCanJump(currentPieceIndex, currentPlayer, createTempBitboards(currentPlayer)).size() > 0){
            testMove = new Move(currentPieceIndex, direction, true);
        }
        Board testBoard = this.clone();
        return testBoard.move(testMove);
    }

    public enum Player {
        RED,
        BLACK,

        NONE;

        public Player opponent() {
            if(this == RED){
                return BLACK;
            } else return RED;
        }
    }

    public enum Direction {
        FORWARD_LEFT,
        FORWARD_RIGHT,
        BACKWARD_LEFT,
        BACKWARD_RIGHT
    }

    public enum State {
        WIN,
        LOSS,
        DRAW,
        ONGOING
    }

    private Player currentPlayer;

    private int movesMade = 0;

    private Player computerPlayer;

    private final int[] edgePieces = new int[]{0, 1, 2, 3, 7, 8, 15, 16, 23, 24, 28, 29, 30, 31};

    private final int[] cornerPieces = new int[]{3, 7, 27, 24};

    private final int[] blackKingPieces = new int[]{28, 29, 30, 31};

    private final int[] redKingPieces = new int[]{0, 1, 2, 3};

    public boolean isComputerPlayer(){
        return computerPlayer == currentPlayer;
    }
    public Board(Player computerPlayer) {
        // Board as an array for visual purposes and debugging, will be converted to a
        // bitboard for optimization
        currentPlayer = Player.BLACK;
        this.computerPlayer = computerPlayer;
        zorbist = new Zorbist();
        String[][] arrayBoard =
                        {{" ", "r", " ", "r", " ", "r", " ", "r"},
                        {"r", " ", "r", " ", "r", " ", "r", " "},
                        {" ", "r", " ", "r", " ", "r", " ", "r"},
                        {" ", " ", " ", " ", " ", " ", " ", " "},
                        {" ", " ", " ", " ", " ", " ", " ", " "},
                        {"b", " ", "b", " ", "b", " ", "b", " "},
                        {" ", "b", " ", "b", " ", "b", " ", "b"},
                        {"b", " ", "b", " ", "b", " ", "b", " "}};
        arrayToBitboard(arrayBoard);
    }

    public Board(long redC, long blackC, long redK, long blackK, int redCC, int redKC, int blackKC, int blackCC, int drawC, Zorbist z, Player currentP, Player computerP, int movesM) {
        currentPlayer = currentP;
        movesMade = movesM;
        computerPlayer = computerP;
        redCheckers = redC;
        blackCheckers = blackC;
        redKings = redK;
        blackKings = blackK;
        redCheckerCount = redCC;
        redKingCount = redKC;
        blackKingCount = blackKC;
        blackCheckerCount = blackCC;
        drawCount = drawC;
        zorbist = z;
    }

    public long generateHash() {
        return zorbist.generateHash(blackKings, blackCheckers, redCheckers, redKings, currentPlayer);
    }


    public String[][] bitboardsToArray() {
        String[][] arrayBoard = new String[8][8];
        boolean activeSquare;
        int index = 32;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i - 1) % 2 == 0 && j % 2 == 0) {
                    activeSquare = false;
                } else {
                    activeSquare = !((i - 1) % 2 != 0 && j % 2 != 0);
                }
                if (activeSquare) {
                    index--;
                    if (((blackCheckers >> index) & 1) == 1) {
                        arrayBoard[i][j] = ("  b  ");
                    } else if (((blackKings >> index) & 1) == 1) {
                        arrayBoard[i][j] = ("  B  ");
                    } else if (((redCheckers >> index) & 1) == 1) {
                        arrayBoard[i][j] = ("  r  ");
                    } else if (((redKings >> index) & 1) == 1) {
                        arrayBoard[i][j] = ("  R  ");
                    } else {
                        arrayBoard[i][j] = ("     ");
                    }
                } else {
                    arrayBoard[i][j] = ("  -  ");
                }
            }
        }
        String[][] arrayBoardTwo = new String[8][8];
        for (int i = 0; i < arrayBoard.length; i++) {
            for (int j = 0; j < arrayBoard.length; j++) {
                arrayBoardTwo[i][j] = arrayBoard[i][7 - j];
            }
        }
        return arrayBoardTwo;
    }

    // Converts the bitboards into a graphical representation for the console.
    public String toString() {
        String[][] arrayBoard = bitboardsToArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                sb.append(arrayBoard[i][j]);
                if (j != 7) {
                    sb.append("|");
                }
                if (j % 7 == 0 && j != 0) {
                    sb.append("\n-----------------------------------------------\n");
                }
            }
        }
        return sb.toString();
    }

    // Convert the visual representation of the board into the necessary bitboards.
    private void arrayToBitboard(String[][] arrrayBoard) {
        String binary;
        int count = 0;
        boolean activeSquare;
        for (int i = arrrayBoard.length; i > 0; i--) {
            for (int j = 0; j < arrrayBoard[0].length; j++) {
                if ((i - 1) % 2 == 0 && j % 2 == 0) {
                    activeSquare = false;
                } else {
                    activeSquare = !((i - 1) % 2 != 0 && j % 2 != 0);
                }
                if (activeSquare) {
                    binary = "00000000000000000000000000000000";
                    binary = binary.substring(count + 1) + "1" + binary.substring(0, count);
                    switch (arrrayBoard[i - 1][j]) {
                        case "r": {
                            redCheckers += stringToBitboard(binary);
                            redCheckerCount++;
                            break;
                        }
                        case "R": {
                            redKings += stringToBitboard(binary);
                            redKingCount++;
                            break;
                        }
                        case "b": {
                            blackCheckers += stringToBitboard(binary);
                            blackCheckerCount++;
                            break;
                        }
                        case "B": {
                            blackKings += stringToBitboard(binary);
                            blackKingCount++;
                            break;
                        }
                    }
                    count++;
                }
            }
        }
    }

    // Converts a string to a long for use in bitboards.
    private long stringToBitboard(String binary) {
        // Will be a positive number
        if (binary.charAt(0) == '0') {
            return Long.parseLong(binary, 2);
        }
        return Long.parseLong("1" + binary.substring(2), 2) * 2;
    }

    public long getBitboard(){
        return blackCheckers | blackKings | redCheckers | redKings;
    }


    public LinkedList<Move> getLegalMoves() {
        // Generate empty hashmap
        LinkedList<Move> possibleMoves = new LinkedList<>();
        int maxJump = -1;
        long[] bitboards = createTempBitboards(currentPlayer);
        ArrayList<Integer> jumpPieces = new ArrayList<>();
        // For each piece
        for (int i = 0; i < NUM_SQUARES; i++) {
            // If no pieces so far can jump, add piece to list
            if (maxJump == -1) {
                if (pieceMoves(i, currentPlayer, bitboards).size() > 0) {
                    jumpPieces.add(i);
                }
            }
            // Set temp max to num jumps piece can make.
            int tempMax = maxJumpForPiece(i, currentPlayer, bitboards);
            if (tempMax > maxJump && tempMax != 0) {
                maxJump = tempMax;
                jumpPieces = new ArrayList<>();
                jumpPieces.add(i);
            } else if (tempMax == maxJump) {
                jumpPieces.add(i);
            }
        }
        if (maxJump > 0) {
            for (int piece : jumpPieces) {
                ArrayList<Move> sequences = allJumpSequencesForPiece(piece, currentPlayer, bitboards, -1, new ArrayList<>(), this);
                possibleMoves.addAll(sequences);
            }
        } else {
            for (int piece : jumpPieces) {
                possibleMoves.addAll(pieceMoves(piece, currentPlayer, bitboards));
            }
        }
        // Orders the moves
        moveList(possibleMoves);
        return cleanAllJumpSequences(possibleMoves);
    }

    // Returns the max jump amount of jumps for one individual piece.
    public int maxJumpForPiece(int piece, Player player, long[] bitboards) {
        int max = 0;
        // Get directions piece can jump in.
        ArrayList<Move> moves = pieceCanJump(piece, player, bitboards);
        // If piece can jump.
        if (moves.size() > 0) {
            // Increment max since it can jump.
            max++;
            // Check if the move can jump in new direction.
            // Checks if piece became king
            for (Move move : moves) {
                Board tempBoard = this.clone();
                boolean startsChecker = tempBoard.pieceIsChecker(piece, bitboards);
                int newPiece = tempBoard.tempMove(move);
                long[] tempBitboard = tempBoard.createTempBitboards(player);
                boolean finishChecker = tempBoard.pieceIsChecker(newPiece, tempBitboard);
                if(startsChecker && finishChecker) {
                    int tempMax = tempBoard.maxJumpForPiece(newPiece, player, tempBoard.createTempBitboards(player));
                    max = Math.max(max, tempMax + 1);
                }
            }
        }
        return max;
    }

    // Returns all Moves that can be made, including multi jumps.
    public ArrayList<Move> allJumpSequencesForPiece(int piece, Player player, long[] bitboards, int index, ArrayList<Move> result, Board board) {
        // First run of all jump Sequences
        ArrayList<Move> moves = board.pieceCanJump(piece, player, bitboards);
        if(index == -1){
            // If piece can jump
            int newIndex = 0;
            if(moves.size() > 0){
                for (Move move : moves) {
                    result.add(move);
                    // Run again with index value
                    Board tempBoard = board.clone();
                    int newPiece = tempBoard.tempMove(move);
                    allJumpSequencesForPiece(newPiece, player, tempBoard.createTempBitboards(player), newIndex, result, tempBoard);
                    index++;
                }
            }
            // at least the second pass through.
        } else{
            // Need to check if the move is already in result, if so duplicate it with additional jump
            int newIndex = result.size();
            // For each new move that can be made duplicate the current index
            if(moves.size() > 0){
                for (Move move : moves) {
                    Move duplicate = result.get(index).clone();
                    duplicate.addDirection(move.getDirection().get(0));
                    result.add(duplicate);
                    // Log piece state
                    boolean startsChecker = board.pieceIsChecker(piece, bitboards);
                    // Run again with index value
                    Board tempBoard = board.clone();
                    int newPiece = tempBoard.tempMove(move);
                    long[] tempBitboard = tempBoard.createTempBitboards(player);
                    boolean finishChecker = tempBoard.pieceIsChecker(newPiece, tempBitboard);
                    // Checks if piece did not promote
                    if(startsChecker && finishChecker || !startsChecker && !finishChecker) {
                        allJumpSequencesForPiece(newPiece, player, tempBitboard, newIndex, result, tempBoard);
                    }
                    newIndex++;
                }
            }
        }
        return result;
    }

    public LinkedList<Move> cleanAllJumpSequences(LinkedList<Move> input) {
        LinkedList<Move> result = new LinkedList<>();
        int maxJump = 0;
        for (Move move : input) {
            int moveSize = move.getDirection().size();
            if (moveSize > maxJump) {
                maxJump = moveSize;
                result.clear();
                result.add(move);
            } else if (moveSize == maxJump) {
                result.add(move);
            }
        }
        return result;
    }



    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    // Arrange moves, if moving towards edge put first, away put last.
    public void moveList(LinkedList<Move> possibleMoves) {
        int kingCount = 0;
        boolean endGame = blackCheckerCount + blackKingCount < 4 || redCheckerCount + redKingCount < 4;
        if(possibleMoves.size() > 0) {
            for (int i = possibleMoves.size() - 1; i > 0; i--) {
                if (becomesKing(possibleMoves.get(i))){
                    if (becomesKing(possibleMoves.get(i))) {
                        Move temp = possibleMoves.get(i);
                        possibleMoves.remove(i);
                        possibleMoves.addFirst(temp);
                        kingCount++;
                    }
                }else if (movesToEdge(possibleMoves.get(i)) && !endGame) {
                    Move temp = possibleMoves.get(i);
                    possibleMoves.remove(i);
                    possibleMoves.add(kingCount, temp);

                }
                if (movesFromEdge(possibleMoves.get(i))) {
                    Move temp = possibleMoves.get(i);
                    possibleMoves.remove(i);
                    possibleMoves.addLast(temp);
                }
            }
        }
    }

    private boolean becomesKing(Move move) {
        int index = this.clone().move(move);
        if(currentPlayer == Player.BLACK) {
            for (int i : blackKingPieces) {
                if (i == index) {
                    return true;
                }
            }
        } else{
            for (int i : redKingPieces) {
                if (i == index) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean movesFromEdge(Move move) {
        for (int i : edgePieces) {
            if (i == move.getPiece()) {
                return true;
            }
        }
        return false;
    }

    private boolean movesToEdge(Move move) {
        int index = this.clone().move(move);
        for (int i : edgePieces) {
            if (i == index) {
                return true;
            }
        }
        return false;
    }

    // The four methods below move a piece in the direction as indicated by the name
    // for temporary bitboards
    public void updatePlayer() {
        if (currentPlayer == Board.Player.RED) {
            currentPlayer = Board.Player.BLACK;
        } else {
            currentPlayer = Board.Player.RED;
        }
    }

    public int move(Move move) {
        int piece = move.getPiece();
        boolean checker = pieceIsChecker(piece, createTempBitboards(currentPlayer));
        movesMade++;
        for(Direction direction : move.getDirection()) {
            if (direction == Direction.BACKWARD_LEFT) {
                piece = moveBackwardLeft(piece, currentPlayer);
            } else if (direction == Direction.FORWARD_LEFT) {
                piece = moveForwardLeft(piece, currentPlayer);
            } else if (direction == Direction.BACKWARD_RIGHT) {
                piece = moveBackwardRight(piece, currentPlayer);
            } else if (direction == Direction.FORWARD_RIGHT) {
                piece = moveForwardRight(piece, currentPlayer);
            }
        }
        updatePlayer();
        return piece;
    }

    public int tempMove(Move move) {
        int piece = move.getPiece();
        boolean checker = pieceIsChecker(piece, createTempBitboards(currentPlayer));
        movesMade++;
        for (Direction direction : move.getDirection()) {
            if (direction == Direction.BACKWARD_LEFT) {
                piece = moveBackwardLeft(piece, currentPlayer);
            } else if (direction == Direction.FORWARD_LEFT) {
                piece = moveForwardLeft(piece, currentPlayer);
            } else if (direction == Direction.BACKWARD_RIGHT) {
                piece = moveBackwardRight(piece, currentPlayer);
            } else if (direction == Direction.FORWARD_RIGHT) {
                piece = moveForwardRight(piece, currentPlayer);
            }
        }
        return piece;
    }

    private int moveBackwardRight(int piece, Player player) {
        int[] offsets = createOffsets(piece);

        long[] bitboards = createTempBitboards(player);
        boolean isChecker = pieceIsChecker(piece, bitboards);
        // Since jumps are forced, if the piece can jump at all, that means it can
        // jump backward to the right.
        if (pieceCanJump(piece, player, bitboards).size() > 0) {
            if (isChecker) {
                if (bitboards[0] == redCheckers) {
                    // Erase piece
                    redCheckers = redCheckers ^ (1L << piece);
                    // Place piece
                    if (piece - offsets[5] < 4) {
                        redKings = redKings ^ (1L << piece - offsets[5]);
                        redCheckerCount--;
                        redKingCount++;
                    } else {
                        redCheckers = redCheckers ^ (1L << piece - offsets[5]);
                    }
                } else {
                    blackCheckers = blackCheckers ^ (1L << piece);
                    blackCheckers = blackCheckers ^ (1L << piece - offsets[5]);
                    if (piece - offsets[5] < 4) {
                        blackKings = blackKings ^ (1L << piece - offsets[5]);
                        blackCheckerCount--;
                        blackKingCount++;
                    } else {
                        blackCheckers = blackCheckers ^ (1L << piece - offsets[5]);
                    }
                }
            } else {
                if (bitboards[1] == redKings) {
                    redKings = redKings ^ (1L << piece);
                    redKings = redKings ^ (1L << piece - offsets[5]);
                } else {
                    blackKings = blackKings ^ (1L << piece);
                    blackKings = blackKings ^ (1L << piece - offsets[5]);
                }
            }
            eraseJumpedPiece(bitboards, piece, offsets, Direction.BACKWARD_RIGHT, player);
            return piece - offsets[5];
        } else {
            drawCount++;
            if (isChecker) {
                if (bitboards[0] == redCheckers) {
                    redCheckers = redCheckers ^ (1L << piece);
                    if (piece - offsets[3] < 4) {
                        redKings = redKings ^ (1L << piece - offsets[3]);
                        redCheckerCount--;
                        redKingCount++;
                    } else {
                        redCheckers = redCheckers ^ (1L << piece - offsets[3]);
                    }
                } else {
                    blackCheckers = blackCheckers ^ (1L << piece);
                    if (piece - offsets[3] < 4) {
                        blackKings = blackKings ^ (1L << piece - offsets[3]);
                        blackCheckerCount--;
                        blackKingCount++;
                    } else {
                        blackCheckers = blackCheckers ^ (1L << piece - offsets[3]);
                    }
                }
            } else {
                if (bitboards[1] == redKings) {
                    redKings = redKings ^ (1L << piece);
                    redKings = redKings ^ (1L << piece - offsets[3]);
                } else {
                    blackKings = blackKings ^ (1L << piece);
                    blackKings = blackKings ^ (1L << piece - offsets[3]);
                }
            }
        }
        return piece - offsets[3];
    }

    private int moveBackwardLeft(int piece, Player player) {
        int[] offsets = createOffsets(piece);

        long[] bitboards = createTempBitboards(player);
        boolean isChecker = pieceIsChecker(piece, bitboards);
        // Since jumps are forced, if the piece can jump at all, that means it can
        // jump backward to the left.
        if (pieceCanJump(piece, player, bitboards).size() > 0) {
            if (isChecker) {
                if (bitboards[0] == redCheckers) {
                    // Erase piece
                    redCheckers = redCheckers ^ (1L << piece);
                    // Place piece
                    if (piece - offsets[4] < 4) {
                        redKings = redKings ^ (1L << piece - offsets[4]);
                        redCheckerCount--;
                        redKingCount++;
                    } else {
                        redCheckers = redCheckers ^ (1L << piece - offsets[4]);
                    }
                } else {
                    blackCheckers = blackCheckers ^ (1L << piece);
                    if (piece - offsets[4] < 4) {
                        blackKings = blackKings ^ (1L << piece - offsets[4]);
                        blackCheckerCount--;
                        blackKingCount++;
                    } else {
                        blackCheckers = blackCheckers ^ (1L << piece - offsets[4]);
                    }
                }
            } else {
                if (bitboards[1] == redKings) {
                    redKings = redKings ^ (1L << piece);
                    redKings = redKings ^ (1L << piece - offsets[4]);
                } else {
                    blackKings = blackKings ^ (1L << piece);
                    blackKings = blackKings ^ (1L << piece - offsets[4]);
                }
            }
            eraseJumpedPiece(bitboards, piece, offsets, Direction.BACKWARD_LEFT, player);
            return piece - offsets[4];
        } else {
            drawCount++;
            if (isChecker) {
                if (bitboards[0] == redCheckers) {
                    redCheckers = redCheckers ^ (1L << piece);
                    if (piece - offsets[2] < 4) {
                        redKings = redKings ^ (1L << piece - offsets[2]);
                        redCheckerCount--;
                        redKingCount++;
                    } else {
                        redCheckers = redCheckers ^ (1L << piece - offsets[2]);
                    }
                } else {
                    blackCheckers = blackCheckers ^ (1L << piece);
                    if (piece - offsets[2] < 4) {
                        blackKings = blackKings ^ (1L << piece - offsets[2]);
                        blackCheckerCount--;
                        blackKingCount++;
                    } else {
                        blackCheckers = blackCheckers ^ (1L << piece - offsets[2]);
                    }
                }
            } else {
                if (bitboards[1] == redKings) {
                    redKings = redKings ^ (1L << piece);
                    redKings = redKings ^ (1L << piece - offsets[2]);
                } else {
                    blackKings = blackKings ^ (1L << piece);
                    blackKings = blackKings ^ (1L << piece - offsets[2]);
                }
            }
        }
        return piece - offsets[2];
    }

    private int moveForwardRight(int piece, Player player) {
        int[] offsets = createOffsets(piece);

        long[] bitboards = createTempBitboards(player);
        boolean isChecker = pieceIsChecker(piece, bitboards);
        // Since jumps are forced, if the piece can jump at all, that means it can
        // jump forward to the right.
        if (pieceCanJump(piece, player, bitboards).size() > 0) {
            if (isChecker) {
                if (bitboards[0] == redCheckers) {
                    // Erase piece
                    redCheckers = redCheckers ^ (1L << piece);
                    // Place piece
                    if (piece + offsets[4] < 27) {
                        redKings = redKings ^ (1L << piece + offsets[4]);
                        redCheckerCount--;
                        redKingCount++;
                    } else {
                        redCheckers = redCheckers ^ (1L << piece + offsets[4]);
                    }
                } else {
                    blackCheckers = blackCheckers ^ (1L << piece);
                    if (piece + offsets[4] > 27) {
                        blackKings = blackKings ^ (1L << piece + offsets[4]);
                        blackCheckerCount--;
                        blackKingCount++;
                    } else {
                        blackCheckers = blackCheckers ^ (1L << piece + offsets[4]);
                    }
                }
            } else {
                if (bitboards[1] == redKings) {
                    redKings = redKings ^ (1L << piece);
                    redKings = redKings ^ (1L << piece + offsets[4]);
                } else {
                    blackKings = blackKings ^ (1L << piece);
                    blackKings = blackKings ^ (1L << piece + offsets[4]);
                }
            }
            eraseJumpedPiece(bitboards, piece, offsets, Direction.FORWARD_RIGHT, player);
            return piece + offsets[4];
        } else {
            drawCount++;
            if (isChecker) {
                if (bitboards[0] == redCheckers) {
                    redCheckers = redCheckers ^ (1L << piece);
                    if (piece + offsets[1] > 27) {
                        redKings = redKings ^ (1L << piece + offsets[1]);
                        redCheckerCount--;
                        redKingCount++;
                    } else {
                        redCheckers = redCheckers ^ (1L << piece + offsets[1]);
                    }
                } else {
                    blackCheckers = blackCheckers ^ (1L << piece);
                    if (piece + offsets[1] > 27) {
                        blackKings = blackKings ^ (1L << piece + offsets[1]);
                        blackCheckerCount--;
                        blackKingCount++;
                    } else {
                        blackCheckers = blackCheckers ^ (1L << piece + offsets[1]);
                    }
                }
            } else {
                if (bitboards[1] == redKings) {
                    redKings = redKings ^ (1L << piece);
                    redKings = redKings ^ (1L << piece + offsets[1]);
                } else {
                    blackKings = blackKings ^ (1L << piece);
                    blackKings = blackKings ^ (1L << piece + offsets[1]);
                }
            }
        }
        return piece + offsets[1];
    }

    private int moveForwardLeft(int piece, Player player) {
        int[] offsets = createOffsets(piece);
        // Create temporary bitboards to utilize the method later.
        long[] bitboards = createTempBitboards(player);
        boolean isChecker = pieceIsChecker(piece, bitboards);
        // Since jumps are forced, if the piece can jump at all, that means it can
        // jump forward to the left.
        if (pieceCanJump(piece, player, bitboards).size() > 0) {
            if (isChecker) {
                if (bitboards[0] == redCheckers) {
                    // Erase piece
                    redCheckers = redCheckers ^ (1L << piece);
                    // Place piece
                    if (piece + offsets[5] > 27) {
                        redKings = redKings ^ (1L << piece + offsets[5]);
                        redCheckerCount--;
                        redKingCount++;
                    } else {
                        redCheckers = redCheckers ^ (1L << piece + offsets[5]);
                    }
                } else {
                    blackCheckers = blackCheckers ^ (1L << piece);
                    if (piece + offsets[5] > 27) {
                        blackKings = blackKings ^ (1L << piece + offsets[5]);
                        blackCheckerCount--;
                        blackKingCount++;
                    } else {
                        blackCheckers = blackCheckers ^ (1L << piece + offsets[5]);
                    }
                }
            } else {
                if (bitboards[1] == redKings) {
                    redKings = redKings ^ (1L << piece);
                    redKings = redKings ^ (1L << piece + offsets[5]);
                } else {
                    blackKings = blackKings ^ (1L << piece);
                    blackKings = blackKings ^ (1L << piece + offsets[5]);
                }
            }
            eraseJumpedPiece(bitboards, piece, offsets, Direction.FORWARD_LEFT, player);
            return piece + offsets[5];
        } else {
            drawCount++;
            if (isChecker) {
                if (bitboards[0] == redCheckers) {
                    redCheckers = redCheckers ^ (1L << piece);
                    if (piece + offsets[0] > 27) {
                        redKings = redKings ^ (1L << piece + offsets[0]);
                        redCheckerCount--;
                        redKingCount++;
                    } else {
                        redCheckers = redCheckers ^ (1L << piece + offsets[0]);
                    }
                } else {
                    blackCheckers = blackCheckers ^ (1L << piece);
                    if (piece + offsets[0] > 27) {
                        blackKings = blackKings ^ (1L << piece + offsets[0]);
                        blackCheckerCount--;
                        blackKingCount++;
                    } else {
                        blackCheckers = blackCheckers ^ (1L << piece + offsets[0]);
                    }
                }
            } else {
                if (bitboards[1] == redKings) {
                    // Erase piece
                    redKings = redKings ^ (1L << piece);
                    // Place piece
                    redKings = redKings ^ (1L << piece + offsets[0]);
                } else {
                    blackKings = blackKings ^ (1L << piece);
                    // Place piece
                    blackKings = blackKings ^ (1L << piece + offsets[0]);
                }
            }
        }
        return piece + offsets[0];
    }

    private boolean pieceIsChecker(int piece, long[] bitboards) {
        return (((bitboards[0] >> piece) & 1) == 1);
    }

    private boolean oppIsChecker(int piece, long[] bitboards) {
        return (((bitboards[2] >> piece) & 1) == 1);
    }

    private boolean pieceIsKing(int piece, long[] bitboards) {
        return (((bitboards[1] >> piece) & 1) == 1);
    }

    private void eraseJumpedPiece(long[] bitboards, int piece, int[] offsets, Direction direction, Player player) {
        int offset = 0;
        if (direction == Direction.FORWARD_LEFT) {
            offset = offsets[0];
        } else if (direction == Direction.FORWARD_RIGHT) {
            offset = offsets[1];
        } else if (direction == Direction.BACKWARD_LEFT) {
            offset = -1 * offsets[2];
        } else if (direction == Direction.BACKWARD_RIGHT) {
            offset = -1 * offsets[3];
        }
        boolean oppIsChecker = oppIsChecker(piece + offset, bitboards);
        if (oppIsChecker) {
            if (player == Player.RED) {
                // Erase piece
                blackCheckers = blackCheckers ^ (1L << piece + offset);
                blackCheckerCount--;
            } else {
                redCheckers = redCheckers ^ (1L << piece + offset);
                redCheckerCount--;
            }
        } else {
            if (player == Player.RED) {
                blackKings = blackKings ^ (1L << piece + offset);
                blackKingCount--;
            } else {
                redKings = redKings ^ (1L << piece + offset);
                redKingCount--;
            }
        }
        drawCount = 0;
    }


    public ArrayList<Move> pieceMoves(int piece, Player player, long[] bitboards) {
        if (pieceIsChecker(piece, bitboards)) {
            if (player == Player.BLACK) {
                return pieceCanMoveForward(piece, bitboards);
            }
            return pieceCanMoveBackward(piece, bitboards);
        } else if (pieceIsKing(piece, bitboards)) {
            ArrayList<Move> result = pieceCanMoveForward(piece, bitboards);
            result.addAll(pieceCanMoveBackward(piece, bitboards));
            return result;
        }
        return new ArrayList<>();
    }

    private ArrayList<Move> pieceCanMoveBackward(int piece, long[] bitboards) {
        ArrayList<Move> moves = new ArrayList<>();
        int[] offsets = createOffsets(piece);
        if ((((bitboards[0] >> piece) & 1) == 1 || ((bitboards[1] >> piece) & 1) == 1)) {
            boolean blocked = false;
            for (long bitBoard : bitboards) {
                if (((bitBoard >> piece - offsets[3]) & 1) == 1 || piece - offsets[3] < 0) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked && (piece + 1) % 8 != 0) {
                moves.add(new Move(piece, Direction.BACKWARD_RIGHT, false));
            }
            blocked = false;
            for (long bitBoard : bitboards) {
                if (((bitBoard >> piece - offsets[2]) & 1) == 1 || piece - offsets[2] < 0) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked && piece % 8 != 0) {
                moves.add((new Move(piece,Direction.BACKWARD_LEFT, false)));
            }
        }
        return moves;
    }

    private ArrayList<Move> pieceCanMoveForward(int piece, long[] bitboards) {
        ArrayList<Move> moves = new ArrayList<>();
        int[] offsets = createOffsets(piece);

        if ((((bitboards[0] >> piece) & 1) == 1 || ((bitboards[1] >> piece) & 1) == 1)) {
            boolean blocked = false;
            for (long bitBoard : bitboards) {
                if (((bitBoard >> piece + offsets[1]) & 1) == 1 || piece + offsets[1] > 31) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked && (piece + 1) % 8 != 0) {
                moves.add((new Move(piece,Direction.FORWARD_RIGHT, false)));
            }
            blocked = false;
            for (long bitBoard : bitboards) {
                if (((bitBoard >> piece + offsets[0]) & 1) == 1 || piece + offsets[0] > 31) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked && piece % 8 != 0) {
                moves.add((new Move(piece,Direction.FORWARD_LEFT, false)));
            }
        }
        return moves;
    }

    // Returns an array list with all the different directions a piece can jump in.
    public ArrayList<Move> pieceCanJump(int piece, Player player, long[] bitboards) {
        if (pieceIsChecker(piece, bitboards)) {
            if (player == Player.BLACK) {
                return pieceCanJumpForward(piece, bitboards);
            }
            return pieceCanJumpBackward(piece, bitboards);
        } else if (pieceIsKing(piece, bitboards)) {
            ArrayList<Move> result = pieceCanJumpForward(piece, bitboards);
            result.addAll(pieceCanJumpBackward(piece, bitboards));
            return result;
        }
        return new ArrayList<>();
    }

    // Checks if one specific piece can jump forward.
    // Returns an array list of its possible jumps, double jumps and more are saved as one move.
    private ArrayList<Move> pieceCanJumpForward(int piece, long[] bitboards) {
        ArrayList<Move> moves = new ArrayList<>();
        int[] offsets = createOffsets(piece);
        // If the piece is the right color
        if (((bitboards[0] >> piece) & 1) == 1 || ((bitboards[1] >> piece) & 1) == 1) {
            // Checks if an opponent piece is to the right.
            if ((((((bitboards[2] >> piece + offsets[1]) & 1) == 1) || (((bitboards[3] >> piece + offsets[1]) & 1) == 1))
                    && (piece + 1) % 4 != 0)) {
                boolean blocked = false;
                for (long bitBoard : bitboards) {
                    if (((bitBoard >> piece + offsets[4]) & 1) == 1 || piece + offsets[4] > 31) {
                        blocked = true;
                        break;
                    }
                }
                if (!blocked) {
                    moves.add(new Move(piece, Direction.FORWARD_RIGHT, true));
                }
            }
            // Checks if an opponent piece is to the left.
            if ((((bitboards[2] >> piece + offsets[0]) & 1) == 1 || ((bitboards[3] >> piece + offsets[0]) & 1) == 1) && piece % 4 != 0) {
                boolean blocked = false;
                for (long bitBoard : bitboards) {
                    if (((bitBoard >> piece + offsets[5]) & 1) == 1 || piece + offsets[5] > 31) {
                        blocked = true;
                        break;
                    }
                }
                if (!blocked) {
                    moves.add(new Move(piece, Direction.FORWARD_LEFT, true));
                }
            }
        }
        return moves;
    }

    // Checks if one specific piece can jump for red.
    private ArrayList<Move> pieceCanJumpBackward(int piece, long[] bitboards) {
        ArrayList<Move> moves = new ArrayList<>();
        boolean test = piece == 28;
        int[] offsets = createOffsets(piece);
        if (((bitboards[0] >> piece) & 1) == 1 || ((bitboards[1] >> piece) & 1) == 1) {
            // Checks if an opponent piece is to the right.
            if (((((bitboards[2] >> piece - offsets[3]) & 1) == 1) || (((bitboards[3] >> piece - offsets[3]) & 1) == 1))
                    && (piece + 1) % 4 != 0) {
                boolean blocked = false;
                for (long bitBoard : bitboards) {
                    if (((bitBoard >> piece - offsets[5]) & 1) == 1 || piece - offsets[5] < 0) {
                        blocked = true;
                        break;
                    }
                }
                if (!blocked) {
                    moves.add(new Move(piece, Direction.BACKWARD_RIGHT, true));
                }
            }
            // Checks if an opponent piece is to the left.
            if (((((bitboards[2] >> piece - offsets[2]) & 1) == 1) || (((bitboards[3] >> piece - offsets[2]) & 1) == 1)) && piece % 4 != 0) {
                boolean blocked = false;
                for (long bitBoard : bitboards) {
                    if (((bitBoard >> piece - offsets[4]) & 1) == 1 || piece - offsets[4] < 0) {
                        blocked = true;
                        break;
                    }
                }
                if (!blocked) {
                    moves.add(new Move(piece, Direction.BACKWARD_LEFT, true));
                }
            }
        }
        return moves;
    }

    // Generates the proper offsets, 0 is top left, 1 is top right, 2 is bottom left, 3 is bottom right.
    // 4 is always 9 for positive diagonal twice, 5 is always 7 for negative diagonal.
    public int[] createOffsets(int piece) {
        int[] result;
        if ((piece / 4) % 2 == 0) {
            result = new int[]{3, 4, 5, 4, 9, 7};
        } else {
            result = new int[]{4, 5, 4, 3, 9, 7};
        }
        return result;
    }

    public long[] createTempBitboards(Player player) {
        boolean black = player == Player.BLACK;
        long[] result = new long[4];
        result[0] = (black ? blackCheckers : redCheckers);
        result[1] = black ? blackKings : redKings;
        result[2] = black ? redCheckers : blackCheckers;
        result[3] = black ? redKings : blackKings;
        return result;
    }

    public int evaluateGame() {
        State state = gameState();
        int base = 1;
        if (state == State.DRAW) {
            return 0;
        } else if (state == State.WIN || state == State.LOSS) {
            base = 2;
        }
        boolean endGame = blackCheckerCount + blackKingCount <= 5 || redCheckerCount + redKingCount <= 5;

        int kingDiff;
        int checkerDiff;
        int edgePieces;
        int oppEdgePieces;
        int avgRowDiff;
        // Evaluate Position
        if (computerPlayer == Player.BLACK) {
            kingDiff = blackKingCount - redKingCount;
            checkerDiff = blackCheckerCount - redCheckerCount;
            edgePieces = countEdgePieces(Player.BLACK);
            oppEdgePieces = countEdgePieces(Player.RED);
            avgRowDiff = (int) ((blackAvgRow() - redAvgRow()) * 10);
        } else {
            kingDiff = redKingCount - blackKingCount;
            checkerDiff = redCheckerCount - blackCheckerCount;
            edgePieces = countEdgePieces(Player.RED);
            oppEdgePieces = countEdgePieces(Player.BLACK);
            avgRowDiff = (int) ((redAvgRow() - blackAvgRow()) * 10);
        }

        int bias = 1;
        if (endGame) {
            // adjust weights based on game state
            if (checkerDiff > 0 && kingDiff > 0) {
                edgePieces = 0;
                oppEdgePieces *= 4;
                // Favor when the pieces are closer together #4c564f
                avgRowDiff = 70 - (int) ((blackAvgRow() -  (9 - redAvgRow()) * 100));
                kingDiff += getProximityScore();
                bias = 1000;
            } else if (checkerDiff < 0 && kingDiff < 0) {
                edgePieces *= 2;
                oppEdgePieces = 0;
                // Favor when the pieces are further apart
                avgRowDiff = (int) ((blackAvgRow() -  (9 - redAvgRow()) * 100));
                bias = 50;
            }
        }
        // game is close, use original weights

        int opponentCornerPenalty = countOpponentCornerPieces(computerPlayer);
        int cornerPenalty = countCornerPieces(computerPlayer.opponent());

        int pieceScore = base * (checkerDiff * 3 + kingDiff * 5 + edgePieces - oppEdgePieces) * 100;
        int random = (int) (randomNum.nextDouble() * 100);

        return pieceScore + avgRowDiff + random - cornerPenalty * bias - opponentCornerPenalty * 200;

    }

    public long getProximityScore() {
        long blackPieces = blackCheckers | blackKings;
        long redPieces = redCheckers | redKings;
        long proximityScore = 0;

        for (int i = 0; i < NUM_SQUARES; i++) {
            if (((1L << i) & blackPieces) != 0) {
                for (int j = 0; j < NUM_SQUARES; j++) {
                    if (((1L << j) & redPieces) != 0) {
                        int distance = Math.max(Math.abs(i / 4 - j / 4), Math.abs(i % 4 - j % 4));
                        proximityScore += NUM_SQUARES - distance;
                    }
                }
            }
        }

        return proximityScore;
    }





    private double redAvgRow() {
        int result = 0;
        for(int i = 0; i < NUM_SQUARES; i++){
            if(((redCheckers >> i) & 1) == 1){
                result += 8 - (i / 4);
            }
        }
        return (double) result / (redCheckerCount);
    }

    private double blackAvgRow() {
        int result = 0;
        for(int i = 0; i < NUM_SQUARES; i++){
            if((((blackCheckers >> i) & 1) == 1)){
                result += (i / 4);
            }
        }
        return (double) result / (blackCheckerCount);
    }

    // Counts up all the pieces safe on the edge of the board
    private int countEdgePieces(Player player) {
        int count = 0;
        if (player == Player.RED) {
            for (int square : edgePieces) {
                if (((redCheckers >> square) & 1) == 1) {
                    count++;
                } else if (((redKings >> square) & 1) == 1) {
                    count++;
                }
            }
        }
        if (player == Player.BLACK) {
            for (int square : edgePieces) {
                if (((blackCheckers >> square) & 1) == 1) {
                    count++;
                } else if (((blackKings >> square) & 1) == 1) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countOpponentCornerPieces(Player player) {
        int[] cornerSquares = {24, 28};
        int count = 0;
        if (player == Player.RED) {
            for (int square : cornerSquares) {
                if (((blackKings >> square) & 1) == 1) {
                    count++;
                }
            }
        }
        if (player == Player.BLACK) {
            for (int square : cornerSquares) {
                if (((redKings >> square) & 1) == 1) {
                    count++;
                }
            }
        }
        return count;
    }


    private int countCornerPieces(Player player) {
        int count = 0;
        if (player == Player.RED) {
            for (int square : cornerPieces) {
                if (((redKings >> square) & 1) == 1) {
                    count++;
                }
            }
        }
        if (player == Player.BLACK) {
            for (int square : cornerPieces) {
                if (((blackKings >> square) & 1) == 1) {
                    count++;
                }
            }
        }
        return count;
    }

    // Returns the state of the game with reference to the input player
    public State gameState() {
        if (currentPlayer != computerPlayer) {
            if (drawCount == DRAW_THRESHOLD) {
                return State.DRAW;
            }
            if (getLegalMoves().size() == 0) {
                return State.WIN;
            } else if (getLegalMoves().size() == 0) {
                return State.LOSS;
            }
            return State.ONGOING;
        } else {
            if (drawCount == DRAW_THRESHOLD) {
                return State.DRAW;
            }
            if (getLegalMoves().size() == 0) {
                return State.LOSS;
            } else if (getLegalMoves().size() == 0) {
                return State.DRAW;
            }
            return State.ONGOING;
        }
    }
}


