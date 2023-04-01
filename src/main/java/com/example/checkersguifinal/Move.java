package com.example.checkersguifinal;

import java.util.ArrayList;
import java.util.List;

/**
 * The Move class represents a move in a checkers game.
 * It stores the index of the piece being moved and an ordered list of directions to move the piece in.
 */
public class Move {
    private boolean jump;
    private int piece;

    private int start;

    private int end;

    private List<Integer> intermediatePositions;
    private ArrayList<Board.Direction> direction = new ArrayList<>();

    /**
     * Constructs a new Move with the given piece index and initial direction.
     */
    public Move(int piece, Board.Direction direction, boolean jump) {
        this.piece = piece;
        this.direction.add(direction);
        this.jump = jump;
        this.start = createStart();
        this.intermediatePositions = createIntPos();
        this.end = createEnd();
    }

    // These three methods might literally be all I need to get working.

    private List<Integer> createIntPos() {
        ArrayList<Integer> fintermediatePositions = new ArrayList<>();
        if (jump && direction.size() > 1) {
            fintermediatePositions.add(jumpInDirection(piece, direction.get(0)));
            int index = 1;
            while (index < direction.size() - 1) {
                fintermediatePositions.add(jumpInDirection(fintermediatePositions.get(index - 1), direction.get(index)));
                index++;
            }
        }
        return fintermediatePositions;
    }

    private int moveInDirection(int piece, Board.Direction direction) {
        int row = piece / 4;
        int col = (piece % 4) * 2 + (row % 2);
        int newRow, newCol;

        switch (direction) {
            case BACKWARD_LEFT:
                newRow = row - 1;
                newCol = col - 1;
                break;
            case BACKWARD_RIGHT:
                newRow = row - 1;
                newCol = col + 1;
                break;
            case FORWARD_RIGHT:
                newRow = row + 1;
                newCol = col + 1;
                break;
            case FORWARD_LEFT:
                newRow = row + 1;
                newCol = col - 1;
                break;
            default:
                return piece;
        }

        if (newRow < 0 || newRow >= 8 || newCol < 0 || newCol >= 8) {
            return piece;
        }

        return newRow * 4 + newCol / 2;
    }

    private int jumpInDirection(int piece, Board.Direction direction){
        int row = piece / 4;
        int col = (piece % 4) * 2 + (row % 2);
        int newRow, newCol;

        switch (direction) {
            case BACKWARD_LEFT:
                newRow = row - 2;
                newCol = col - 2;
                break;
            case BACKWARD_RIGHT:
                newRow = row - 2;
                newCol = col + 2;
                break;
            case FORWARD_RIGHT:
                newRow = row + 2;
                newCol = col + 2;
                break;
            case FORWARD_LEFT:
                newRow = row + 2;
                newCol = col - 2;
                break;
            default:
                return piece;
        }

        if (newRow < 0 || newRow >= 8 || newCol < 0 || newCol >= 8) {
            return piece;
        }

        return newRow * 4 + newCol / 2;
    }



    private int createEnd() {
        if(jump){
            // Means we need to pull up the last direction and last position in intermediate Pos.
            if(intermediatePositions.size() > 0){
                return jumpInDirection(intermediatePositions.get(intermediatePositions.size() - 1), direction.get(direction.size() - 1));
            }
            // Jump but not a multiple jump.
            return jumpInDirection(piece, direction.get(direction.size() - 1));
        }
        // Not a jump
        return moveInDirection(piece, direction.get(direction.size() - 1));
    }


    private int createStart() {
        return piece;
    }

    private Move(int piece, boolean jump) {
        this.piece = piece;
        this.direction = new ArrayList<>();
        this.jump = jump;
    }

    /**
     * Returns the index of the piece being moved.
     */
    public int getPiece() {
        return piece;
    }

    /**
     * Returns the ordered list of directions to move the piece in.
     */
    public ArrayList<Board.Direction> getDirection() {
        return direction;
    }

    /**
     * Adds a new direction to the ordered list of directions.
     */
    public void addDirection(Board.Direction direction) {
        this.direction.add(direction);
        this.start = createStart();
        this.intermediatePositions = createIntPos();
        this.end = createEnd();

    }

    /**
     * Returns a deep copy of this Move.
     */
    public Move clone() {
        Move result = new Move(piece, jump);
        for (Board.Direction temp : direction) {
            result.addDirection(temp);
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Piece: ");
        sb.append(piece);
        int num = 1;
        for (Board.Direction path : direction) {
            String suffix = "th";
            if (num == 1) {
                suffix = "st";
            } else if (num == 2) {
                suffix = "nd";
            } else if (num == 3) {
                suffix = "rd";
            }
            sb.append("\n").append(num).append(suffix);
            sb.append(" Direction: ").append(direction.get(num - 1));
            num++;
        }
        return sb.toString();
    }

    /**
     * Checks if this Move is equal to another Move.
     */
    public boolean equals(Move other) {
        return piece == other.piece && direction.equals(other.direction);
    }

    /**
     * Returns an Iterable of the directions in this Move.
     */
    public Iterable<Board.Direction> getDirections() {
        return direction;
    }

    // I need to modify this method to additionally check if the piece is jumping.
    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public List<Integer> getIntermediatePositions() {
        return intermediatePositions;
    }
}

