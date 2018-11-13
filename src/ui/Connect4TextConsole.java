/**
 * This class outputs a Connect 4 game in the text console.
 *
 * @author Kaysi Pilcher
 * @version 1.2
 */

package ui;

// Connect 4 Logic
import core.Connect4;
import core.Connect4ComputerPlayer;

import java.util.Scanner;

public class Connect4TextConsole {

    static Connect4 game = new Connect4();
    static Scanner scan = new Scanner(System.in);

    /**
     * Prints the current state of the board
     *
     * @return the game board as a String
     */
    private static String printBoard() {
        char[][] board = game.getBoard();
        String result = "";
        for (int i = 0; i < board.length; i++){
            for(int j = 0; j < board[i].length; j++) {
                result += "|" + board[i][j];
            }
            result += "|\n";
        }
        return result;
    }

    /**
     * Checks if a string is an integer
     * @param str the string to be checked
     * @return true if the string is an integer, false otherwise
     */
    private static boolean isInteger(String str) {
        try {
            int num = Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    /**
     * Method for twoPlayer connect 4 game control flow
     */
    private static void twoPlayerGame() {
        int gameOver = 0;

        System.out.println("Begin Game.");

        //while there is no winner
        while (gameOver == 0) {

            System.out.println(printBoard() + (game.getTurn() ? "Player X " : "Player O ") +
                    "- your turn. Choose a column number from 1-7");

            String input = scan.nextLine();

            //check if the input is a number
            while (!isInteger(input)) {
                System.out.println("Invalid move - Please try again");
                input = scan.nextLine();
            }

            int column = Integer.parseInt(input);

            // check if valid move
            if (game.validTurn(column)) {
                game.takeTurn(column);
            }
            else {
                System.out.println("Invalid move - Please try again");
            }

            gameOver = game.checkForWin();
        }

        // Print game results
        if (gameOver == -1) { //tie
            System.out.println("Tie Game!");
        }
        else {
            System.out.println(printBoard() + (game.getWinner() == 'X' ? "Player X" : "Player O")
                    + " is the winner!");
        }
    }

    /**
     *  Method for play against computer connect 4 game control flow
     */
    private static void computerPlayerGame() {
        // player X is the person
        // player O is the computer

        int gameOver = 0;

        System.out.println("Begin Game against Computer.");

        //while there is no winner
        while (gameOver == 0) {

            boolean playersTurn = game.getTurn();

            System.out.println(printBoard() + (playersTurn ? "It is your turn. Choose a column number from 1-7" :
                    "It is the computers turn."));

            if (playersTurn) {

                String input = scan.nextLine();

                //check if the input is a number
                while (!isInteger(input)) {
                    System.out.println("Invalid move - Please try again");
                    input = scan.nextLine();
                }

                int column = Integer.parseInt(input);

                // check if valid move
                if (game.validTurn(column)) {
                    game.takeTurn(column);
                }
                else {
                    System.out.println("Invalid move - Please try again");
                }
            }
            else { //computers turn
                Connect4ComputerPlayer.takeTurn(game);
            }

            gameOver = game.checkForWin();
        }

        // Print game results
        if (gameOver == -1) { //tie
            System.out.println("Tie Game!");
        }
        else {
            System.out.println(printBoard() + (game.getWinner() == 'X' ? "You won!" : "You lost!"));
        }
    }

    /**
     * Entry point of the console based game
     */
    public static void Entry() {

        // Decide to play against another player or the computer
        String decision;
        System.out.println("Enter 'P' if you want to play against another player; Enter 'C' to play against computer.");
        decision = scan.nextLine();

        // if invalid input
        while (!decision.equals("P") && !decision.equals("C")) {
            System.out.println("Invalid input.\n" +
                    "Enter 'P' if you want to play against another player; Enter 'C' to play against computer.");
            decision = scan.nextLine();
        }

        if (decision.equals("C")) {
            computerPlayerGame();
        } else { // Play another player
            twoPlayerGame();
        }
    }
}