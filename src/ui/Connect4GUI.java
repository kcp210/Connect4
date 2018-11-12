/**
 * This class provides a GUI to play Connect4.
 *
 * @author Kaysi Pilcher
 * @version 1.0
 */

package ui;

import core.Connect4;

import core.Connect4ComputerPlayer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

//TODO: make window more responsive

public class Connect4GUI extends Application {

    // Constants
    private final char BLANK = ' ';
    private final char RED = 'X';
    private final char YELLOW = 'O';

    // attributes of page
    private Connect4 game;
    private Label prompt;
    private GridPane grid;
    private boolean twoPlayerGame;
    private Label title;


    public static void main(String[] args) {
        launch(args);
    }

    /**
     * The start method initializes all components of the page and
     * starts the application
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) {

        //automatically set up 2 player game
        twoPlayerGame = true;

        //connect4 game board
        game = new Connect4();
        char[][] board = game.getBoard();
        grid = new GridPane();

        // add correct styling to each gamespace in board
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                grid.add(new GameSpace(j), j, i);
            }
        }

        //initialize title
        title = new Label("You are playing Connect4 - 2 player");
        title.setStyle("-fx-font: 36 arial");

        // create Buttons
        Button newTwoPlayer = new Button("Start new two player game");
        Button newPlayAgainstComp = new Button("Start new play against computer game");

        // Event Listeners for buttons
        newTwoPlayer.setOnAction(event -> {
            game = new Connect4();
            updateGrid();
            title.setText("You are playing Connect4 - 2 player");
            prompt.setText((game.getTurn() ? "Player red " : "Player yellow ") +
                    "- your turn. Click a column to make a move.");
            twoPlayerGame = true;
        });

        newPlayAgainstComp.setOnAction(event -> {
            game = new Connect4();
            updateGrid();
            title.setText("You are playing Connect4 - Play Against Computer");
            prompt.setText((game.getTurn() ? "It is your turn. Click a column to make a move." :
                    "It is the computer's turn"));
            twoPlayerGame = false;
        });

        HBox buttons = new HBox(newTwoPlayer, newPlayAgainstComp);
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.CENTER);


        // initialize text prompt
        prompt = new Label("Begin Game. " + (game.getTurn() ? "Player red " : "Player yellow ") +
                "- your turn. Click a column to make a move.");
        prompt.setStyle("-fx-font: 24 arial");

        // setting title of Stage
        primaryStage.setTitle("Connect 4");

        //Positioning all elements
        VBox root = new VBox();
        root.setPadding(new Insets(10, 50, 50, 50));
        root.setSpacing(10);

        root.getChildren().add(title);
        root.getChildren().add(buttons);
        root.getChildren().add(prompt);
        root.getChildren().add(grid);
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 300, 250);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    /**
     * Updates the state of the board
     */
    public void updateGrid() {
        char[][] board = game.getBoard();

        for(int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                GameSpace temp = (GameSpace) getNodeFromGridPane(j, i);
                temp.setValue(board[i][j]);
            }
        }
    }

    /**
     * Get the node from the specified index of the global gridpane
     * @param col column of gridpane
     * @param row row of gridpane
     * @return Node in the specified index
     */
    private Node getNodeFromGridPane(int col, int row) {
        for (Node node : grid.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }

    // Inner class that represents a space in the Connect4 game
    public class GameSpace extends Pane {

        // value of the gamespace
        private char value;
        private int column;

        /**
         * Constructor for a GameSpace
         */
        public GameSpace(int col) {
            value = BLANK;
            // adding 1 accounts for takeTurn method
            column = col + 1;
            setStyle("-fx-border-color: black");
            this.setPrefSize(5000, 5000);
//            this.setMaxHeight(200);
//            this.setMinHeight(100);
//            this.setMaxWidth(200);
//            this.setMinWidth(100);
            this.setOnMouseClicked(e -> handleMouseClick());

        }

        /**
         * Setter for value of GameSpace
         * @param v new value
         */
        public void setValue(char v) {
            //set new value
            value = v;

            // create chip object
            Circle chip = new Circle(this.getWidth() / 2, this.getHeight() / 2,
                    .9 * (this.getHeight() / 2));
            chip.centerXProperty().bind(this.widthProperty().divide(2));
            chip.centerYProperty().bind(this.heightProperty().divide(2));
            chip.radiusProperty().bind((this.heightProperty().divide(2)).multiply(.7));

            // set color according to player
            if (value == RED) {
                chip.setStroke(Color.RED);
                chip.setFill(Color.RED);
                getChildren().add(chip);
            }
            else if (value == YELLOW) {
                chip.setStroke(Color.YELLOW);
                chip.setFill(Color.YELLOW);
                getChildren().add(chip);
            }
            else {
                this.getChildren().clear();
            }
        }

        /**
         * Handles clicks on the gameboard
         */
        private void handleMouseClick() {
            // if game is not over
            if (game.checkForWin() == 0) {
                if (twoPlayerGame) {
                    // check that a turn can be made in column clicked
                    if (game.validTurn(column)) {
                        game.takeTurn(column);
                    } else {
                        prompt.setText("Invalid move - Please try again");
                        return;
                    }

                    // update grid with correct game pieces
                    updateGrid();

                    // check for a win
                    int gameOver = game.checkForWin();
                    if (gameOver == -1) { //tie
                        prompt.setText("Tie Game!");
                    } else if (gameOver == 1) {
                        prompt.setText((game.getWinner() == RED ? "Player Red" : "Player Yellow")
                                + " is the winner!");

                    } else {
                        prompt.setText((game.getTurn() ? "Player Red " : "Player Yellow ") +
                                "- your turn. Click a column to make a move.");
                    }
                }
                else { //play against computer
                    //check that turn can be made with specified column
                    if (game.validTurn(column)) {
                        game.takeTurn(column);
                    } else {
                        prompt.setText("Invalid move - Please try again");
                        return;
                    }

                    updateGrid();

                    // check for a win
                    checkForWinPlayAgainstComputer(game.checkForWin());

                    Connect4ComputerPlayer.takeTurn(game);

                    updateGrid();

                    // check for a win
                    checkForWinPlayAgainstComputer(game.checkForWin());

                }
            }
        }

        /**
         * Checks for a win when playing against the computer
         * @param gameOver
         */
        private void checkForWinPlayAgainstComputer(int gameOver) {
            if (gameOver == -1) { //tie
                prompt.setText("Tie Game!");
            } else if (gameOver == 1) {
                prompt.setText((game.getWinner() == RED ? "You won!" : "You lost!"));

            } else {
                prompt.setText((game.getTurn() ? "It is your turn. Click a column to make a move." :
                        "It is the computer's turn"));
            }
        }
    }
}