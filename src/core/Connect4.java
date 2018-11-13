/**
 * This class provides the logic for a Connect 4 game
 *
 * @author Kaysi Pilcher
 * @version 1.0
 */

package core;

public class Connect4 {

    private char[][] board;
    private boolean playerXTurn;
    private char winner;

    // constants for game board
    private final char BLANK = ' ';
    private final char PLAYER_X = 'X';
    private final char PLAYER_O = 'O';

    /**
     * Constructor for a blank board of a new game
     */
    public Connect4() {
        board = new char[6][7];
        for (int i = 0; i < board.length; i++){
            for(int j = 0; j < board[i].length; j++) {
                board[i][j] = BLANK;
            }
        }
        playerXTurn = true;
        winner = BLANK;
    }

    /**
     * Getter for the game board
     * @return char[][] board, the current state of the board
     */
    public char[][] getBoard() {
        return board;
    }

    /**
     * Getter for whose turn it is
     * @return true if it is Player X's turn, false otherwise
     */
    public boolean getTurn() {
        return playerXTurn;
    }


    public char getWinner() {
        return winner;
    }


    /**
     * Determines if the column specified is a valid move
     * @param col the column the user selected
     * @return true if the column is within the bounds of the array and
     *         has available spaces.
     */
    public boolean validTurn(int col) {
        // adjust column for array indicies
        col -= 1;
        boolean result = false;
        if (col >= 0 && col <= board.length) {
            for (int i = board.length - 1; i >= 0; i--) {
                if (board[i][col] == BLANK) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Updates the board with the most recent turn (if valid) then switches
     * turns
     * @param col the column the user selected
     */
    public void takeTurn(int col) {
        if (validTurn(col)) {
            // adjust column for array indicies
            col -= 1;
            // decide which token to use
            char token = playerXTurn ? PLAYER_X : PLAYER_O;
            for (int i = board.length - 1; i >= 0; i--) {
                if (board[i][col] == BLANK) {
                    board[i][col] = token;
                    break;
                }
            }

            // switch turns
            playerXTurn = !playerXTurn;
        }
    }

    /**
     * Checks if there is a win
     *
     * @return 1 if someone won, 0 if no winner yet, -1 if tie
     */
    public int checkForWin() {

        // check for horizontal win
        for (int i = 0; i < board.length; i++) {
            // board.length - 3 to avoid index out of bounds
            for (int j = 0; j < board[i].length - 3; j++)

                // check for taken spaces in board
                if (board[i][j] != BLANK && board[i][j + 1] != BLANK
                        && board[i][j + 2] != BLANK && board[i][j + 3] != BLANK) {
                    if (board[i][j] == board[i][j + 1] && board[i][j] == board[i][j + 2]
                            && board[i][j] == board[i][j + 3]) {
                        winner = board[i][j];
                        return 1;
                    }
                }
        }

        // check for vertical win
        // board.length - 3 to avoid index out of bounds
        for (int i = 0; i < board.length - 3; i++) {
            for (int j = 0; j < board[i].length; j++) {

                // check for taken spaces in board
                if (board[i][j] != BLANK && board[i + 1][j] != BLANK
                        && board[i + 2][j] != BLANK && board[i + 3][j] != BLANK) {
                    if (board[i][j] == board[i + 1][j] && board[i][j] == board[i + 2][j]
                            && board[i][j] == board[i + 3][j]) {
                        winner = board[i][j];
                        return 1;
                    }
                }
            }
        }

        // check for right diagonal (\) win
        // board.length - 3 to avoid index out of bounds
        for (int i = 0; i < board.length - 3; i++) {
            for (int j = 0; j < board[i].length - 3; j++) {

                // check for taken spaces in board
                if (board[i][j] != BLANK && board[i + 1][j + 1] != BLANK
                        && board[i + 2][j + 2] != BLANK && board[i + 3][j + 3] != BLANK) {
                    if (board[i][j] == board[i + 1][j + 1] && board[i][j] == board[i + 2][j + 2]
                            && board[i][j] == board[i + 3][j + 3]) {
                        winner = board[i][j];
                        return 1;
                    }
                }
            }
        }

        // check for left diagonal (/) win
        // board.length - 3 to avoid index out of bounds
        for (int i = 0; i < board.length - 3; i++) {
            // starting at j = 3 to avoid index out of bounds
            for (int j = 3; j < board[i].length; j++) {

                // check for taken spaces in board
                if (board[i][j] != BLANK && board[i + 1][j - 1] != BLANK
                        && board[i + 2][j - 2] != BLANK && board[i + 3][j - 3] != BLANK) {
                    if (board[i][j] == board[i + 1][j - 1] && board[i][j] == board[i + 2][j - 2]
                            && board[i][j] == board[i + 3][j - 3]) {
                        winner = board[i][j];
                        return 1;
                    }
                }
            }
        }

        // Check if any more available moves
        for (int i =0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == BLANK) {
                    return 0;
                }
            }
        }

        // no winner && no available moves so must be tie
        return -1;
    }
}
