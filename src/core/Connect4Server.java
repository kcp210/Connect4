package core;

/**
 * @version 1.0
 * @author Kaysi Pilcher
 *
 * The core.Connect4Server class is the server side to a Connect4 game.
 * 1 and 2 Player game play is supported with a Graphic User Interface
 *
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;


public class Connect4Server extends Application implements Connect4Constants{

    // keeps track of how many sessions are running
    private static int sessionNum = 1;

    /**
     * Start method for server
     * @param primaryStage stage for server
     */
    public void start(Stage primaryStage) {
        TextArea serverLog = new TextArea();

        // Create a scene and place it in the stage
        Scene scene = new Scene(new ScrollPane(serverLog), 450, 200);
        // set title
        primaryStage.setTitle("Connect4 Server");
        primaryStage.setScene(scene);
        primaryStage.show();


        new Thread( () -> {
           try {
               ServerSocket serverSocket = new ServerSocket(8000);
               Platform.runLater(() -> serverLog.appendText(new Date() +
                       ": Server started at port 8000\n"));

               // Ready to create new session
               while (true) {
                   Platform.runLater(() -> serverLog.appendText(new Date() +
                           ": Waiting for players to join session " + sessionNum + '\n'));

                   // Connect to player 1
                   Socket player1 = serverSocket.accept();

                   Platform.runLater(() -> {
                       serverLog.appendText(new Date() + ": Player 1 joined session "
                               + sessionNum + '\n');
                       serverLog.appendText("Player 1's IP address" +
                               player1.getInetAddress().getHostAddress() + '\n');
                   });

                   // Notify player of their token
                   new DataOutputStream(
                           player1.getOutputStream()).writeChar(PLAYER_1);

                   // determine if player wants one or two person game
                   int numPlayers = new DataInputStream(player1.getInputStream()).readInt();
                   System.out.println(numPlayers);

                   if (numPlayers == TWO_PLAYER_GAME) {

                       // Connect to player 2
                       Socket player2 = serverSocket.accept();

                       Platform.runLater(() -> {
                           serverLog.appendText(new Date() +
                                   ": Player 2 joined session " + sessionNum + '\n');
                           serverLog.appendText("Player 2's IP address" +
                                   player2.getInetAddress().getHostAddress() + '\n');
                       });

                       // Notify player of their token
                       new DataOutputStream(
                               player2.getOutputStream()).writeChar(PLAYER_2);

                       // Display session and increment sessionNum
                       Platform.runLater(() ->
                               serverLog.appendText(new Date() +
                                       ": Start a thread for 2 player session " + sessionNum++ + '\n'));

                       // Launch new thread for two player GUI session
                       new Thread(new HandleGUISession(player1, player2)).start();
                   }
                   else { //one player game
                       Platform.runLater(() ->
                               serverLog.appendText(new Date() +
                                       ": Start a thread for 1 player session " + sessionNum++ + '\n'));
                       new Thread(new HandleGUISession(player1)).start();
                   }
               }
           } catch (IOException ex) {
               ex.printStackTrace();
           }
        }).start();
    }


    /**
     * Nested Inner Task class to manage a particular Connect4 game session
     */
    private class HandleGUISession implements Runnable, Connect4Constants {

        private Socket player1;
        private Socket player2;

        private DataInputStream fromP1;
        private DataOutputStream toP1;
        private DataInputStream fromP2;
        private DataOutputStream toP2;

        private char[][] board;
        private boolean player1Turn;
        private char winner;
        private int lastTurnRow;
        private int lastTurnCol;

        private boolean twoPlayerGame;


        /**
         * Constructor for a thread to handle the GUI Connect4 game
         * with two players
         * @param p1 client socket for player 1
         * @param p2 client socket for player 2
         */
        private HandleGUISession(Socket p1, Socket p2) {
            // assign sockets
            player1 = p1;
            player2 = p2;

            twoPlayerGame = true;

            // initialize new Connect4 game
            board = new char[6][7];
            for (int i = 0; i < board.length; i++){
                for(int j = 0; j < board[i].length; j++) {
                    board[i][j] = BLANK;
                }
            }
            player1Turn = true;
            winner = BLANK;
        }

        /**
         * Constructor for a thread to handle the GUI Connect4 game
         * with one player
         * @param p1 client socket for player 1
         */
        private HandleGUISession(Socket p1) {
            player1 = p1;
            twoPlayerGame = false;
            board = new char[6][7];
            for (int i = 0; i < board.length; i++){
                for(int j = 0; j < board[i].length; j++) {
                    board[i][j] = BLANK;
                }
            }
            player1Turn = true;
            winner = BLANK;
        }

        /**
         * The control method for the task
         */
        @Override
        public void run() {

            try {
                if (twoPlayerGame) {
                    fromP1 = new DataInputStream(player1.getInputStream());
                    toP1 = new DataOutputStream(player1.getOutputStream());
                    fromP2 = new DataInputStream(player2.getInputStream());
                    toP2 = new DataOutputStream(player2.getOutputStream());
                    int column;

                    // give player 1 ok to start game
                    toP1.writeInt(1);

                    while (true) {

                        // receive move from player 1 & ensure it is a valid move
                        column = fromP1.readInt();
                        while (takeTurn(column) == INVALID_TURN) {
                            toP1.writeInt(INVALID_TURN);
                            column = fromP1.readInt();
                        }
                        // tell p1 that move was valid
                        toP1.writeInt(VALID_TURN);

                        // determine if p1 made winning move, else next player takes turn
                        int status = checkForWin();

                        if (sendStatus2Player(status, P1_WINNER)) break; //game over

                        // receive move from p2

                        column = fromP2.readInt();

                        while (takeTurn(column) == INVALID_TURN) {
                            toP2.writeInt(INVALID_TURN);
                            column = fromP2.readInt();
                        }
                        // tell p2 that move was valid
                        toP2.writeInt(VALID_TURN);

                        // check if p2 made winning move
                        status = checkForWin();

                        if (sendStatus2Player(status, P2_WINNER)) break;
                    }
                }
                else { //one player game

                    fromP1 = new DataInputStream(player1.getInputStream());
                    toP1 = new DataOutputStream(player1.getOutputStream());
                    int column;

                    // give player 1 ok to start game
                    toP1.writeInt(1);

                    while (true) {
                        // receive move from player 1 & ensure it is a valid move
                        column = fromP1.readInt();
                        while (takeTurn(column) == INVALID_TURN) {
                            toP1.writeInt(INVALID_TURN);
                            column = fromP1.readInt();
                        }
                        // tell p1 that move was valid
                        toP1.writeInt(VALID_TURN);

                        // determine if p1 made winning move, else next player takes turn
                        int status = checkForWin();

                        if (sendStatusCompPlay(status, P1_WINNER)) break; //game over

                        takeComputerTurn();

                        status = checkForWin();

                        if (sendStatusCompPlay(status, P2_WINNER)) break;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

        /**
         * Method that sends the status of a one player game to the player. The method
         * is called after each turn
         * @param status status of game
         * @param winner which player just went and may have won
         * @return true if the game is over due to tie or win
         * @throws IOException due to server communication
         */
        private boolean sendStatusCompPlay(int status, int winner) throws IOException {
            if (status == winner) {
                toP1.writeInt(winner);
                sendMove(toP1);
                return true;
            } else if (status == TIE) {
                toP1.writeInt(TIE);
                sendMove(toP1);
                return true;
            } else if (status == CONTINUE) {
                toP1.writeInt(CONTINUE); //new
                sendMove(toP1);
            }
            return false;
        }

        /**
         * Method that sends the status of a two player game to the players. The method
         * is called after each turn.
         * @param status status of game
         * @param winner which player just went and may have won
         * @return true if game is over dur to tie or win
         * @throws IOException due to server communication
         */
        private boolean sendStatus2Player(int status, int winner) throws IOException {
            if (status == winner) {
                toP1.writeInt(winner);
                toP2.writeInt(winner);
                sendMove(toP1);
                sendMove(toP2);
                return true;
            } else if (status == TIE) {
                toP1.writeInt(TIE);
                toP2.writeInt(TIE);
                sendMove(toP1);
                sendMove(toP2);
                return true;
            } else if (status == CONTINUE) {
                toP2.writeInt(CONTINUE);
                toP1.writeInt(CONTINUE); //new
                sendMove(toP1);
                sendMove(toP2);
            }
            return false;
        }

        /**
         * Sends last move to other player
         * @param out
         * @throws IOException
         */
        private void sendMove(DataOutputStream out) throws IOException {
            out.writeInt(lastTurnRow);
            out.writeInt(lastTurnCol);
        }


        /**
         * Determines if the column specified is a valid move
         * @param col the column the user selected
         * @return true if the column is within the bounds of the array and
         *         has available spaces.
         */
        private boolean validTurn(int col) {
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
        private int takeTurn(int col) {
            int result = CONTINUE;
            if (validTurn(col)) {
                // adjust column for array indicies
                col -= 1;
                // decide which token to use
                char token = player1Turn ? PLAYER_1 : PLAYER_2;
                for (int i = board.length - 1; i >= 0; i--) {
                    if (board[i][col] == BLANK) {
                        board[i][col] = token;
                        lastTurnRow = i;
                        lastTurnCol = col;
                        break;
                    }
                }

                // switch turns
                player1Turn = !player1Turn;
            }
            else {
                result = INVALID_TURN;
            }
            return result;
        }

        private void takeComputerTurn() {
            int column = (int)(Math.random()* 7) + 1;
            while (!validTurn(column)) {
                column = (int)(Math.random()* 7) + 1;
            }
            takeTurn(column);
        }


        /**
         * Checks if there is a win
         *
         * @return 1 if player 1 won, 2 if player 2 won, 0 if no
         * winner yet, -1 if tie
         */
        private int checkForWin() {

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
                            return winner == PLAYER_1 ? P1_WINNER: P2_WINNER;
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
                            return winner == PLAYER_1 ? P1_WINNER: P2_WINNER;
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
                            return winner == PLAYER_1 ? P1_WINNER: P2_WINNER;
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
                            return winner == PLAYER_1 ? P1_WINNER: P2_WINNER;
                        }
                    }
                }
            }

            // Check if any more available moves
            for (int i =0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if (board[i][j] == BLANK) {
                        return CONTINUE;
                    }
                }
            }

            // no winner && no available moves so must be tie
            return TIE;
        }
    }
}