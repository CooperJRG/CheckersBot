package com.example.checkersguifinal;

import java.security.SecureRandom;

public class Zorbist {

    private static final int BOARD_SIZE = 32;
    private static final int PIECE_TYPES = 4;
    private static final int PLAYER_TYPES = 2;

    private final long[][][] randomValues;

    public Zorbist() {
        SecureRandom random = new SecureRandom();
        randomValues = new long[BOARD_SIZE][PIECE_TYPES][PLAYER_TYPES];

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < PIECE_TYPES; j++) {
                for (int k = 0; k < PLAYER_TYPES; k++) {
                    randomValues[i][j][k] = random.nextLong();
                }
            }
        }
    }

    public long generateHash(long blackKing, long blackChecker, long redChecker, long redKing, Board.Player player) {
        long hash = 0L;

        for (int i = 0; i < BOARD_SIZE; i++) {
            if (((blackChecker >> i) & 1) == 1) {
                hash ^= randomValues[i][0][0];
            } else if (((blackKing >> i) & 1) == 1) {
                hash ^= randomValues[i][1][0];
            } else if (((redChecker >> i) & 1) == 1) {
                hash ^= randomValues[i][2][0];
            } else if (((redKing >> i) & 1) == 1) {
                hash ^= randomValues[i][3][0];
            }

            if (player == Board.Player.RED) {
                hash ^= randomValues[i][0][1];
            } else {
                hash ^= randomValues[i][0][0];
            }
        }

        return hash;
    }
}
