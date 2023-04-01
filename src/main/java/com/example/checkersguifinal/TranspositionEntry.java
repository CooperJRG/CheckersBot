package com.example.checkersguifinal;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class TranspositionEntry {
    private Move bestMove;
    public int age;
    private int depth;
    private int evaluation;
    private TreeMap<Integer, Move> moveList;

    private boolean maximizingPlayer;


    public TranspositionEntry(int depth, Integer evaluation, int age, TreeMap<Integer, Move> moveList, boolean maximizingPlayer, Move bestMove) {
        this.depth = depth;
        this.moveList = moveList;
        this.evaluation = evaluation;
        this.age = age;
        this.maximizingPlayer = maximizingPlayer;
        this.bestMove = bestMove;
    }
    public TranspositionEntry(int depth, Integer evaluation, int age, boolean maximizingPlayer, Move bestMove) {
        this.depth = depth;
        this.evaluation = evaluation;
        this.age = age;
        this.maximizingPlayer = maximizingPlayer;
        this.bestMove = bestMove;
    }


    public int getDepth() {
        return depth;
    }

    public int getAge() {
        return age;
    }

    public Move getBestMove() {
        return bestMove;
    }

    public LinkedList<Move> getBestMoveList() {
        LinkedList<Move> result = new LinkedList<>();
        boolean valueFound = false;

        // Add the value to the front of the result LinkedList
        result.add(bestMove);

        // Create a temporary TreeMap with a custom Comparator that sorts keys in ascending order
        TreeMap<Integer, Move> tempMapAsc = new TreeMap<>(new Comparator<Integer>() {
            public int compare(Integer key1, Integer key2) {
                return key1.compareTo(key2);
            }
        });
        tempMapAsc.putAll(moveList);

        // Iterate over the entries in the temporary TreeMap and add them to the result LinkedList
        for (Map.Entry<Integer, Move> entry : tempMapAsc.entrySet()) {
            if (!valueFound && entry.getValue().equals(bestMove)) {
                valueFound = true;
                continue;
            }
            result.add(entry.getValue());
        }

        // If the value was found at the front, reverse the order of the result LinkedList
        if (!valueFound) {
            LinkedList<Move> reversedResult = new LinkedList<>();
            for (Move m : result) {
                reversedResult.addFirst(m);
            }
            result = reversedResult;
        }

        return result;
    }

    public Integer getEvaluation() {
        return evaluation;
    }


    public void setDepth(int depth) {
        this.depth = depth;
    }


    public void setEvaluation(Integer evaluation) {
        this.evaluation = evaluation;
    }


}

