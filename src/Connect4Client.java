import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Connect4Client extends Application implements Connect4Constants {

    private Scanner scan = new Scanner(System.in);

    private Label prompt = new Label();
    private GridPane grid;
    private boolean twoPlayerGame;
    private Label title = new Label();
    private char myToken;
    private char otherToken;
    private boolean myTurn;

    private int selectedColumn;

    private DataInputStream fromServer;
    private DataOutputStream toServer;

    // Continue to play?
    private boolean continueToPlay = true;

    // Wait for the player to mark a cell
    private boolean waiting = true;

    // Host name or ip
    private String host = "localhost";
    private int port = 8000;

    public static void main(String args[]) {
        //TODO: add option for UI or Console
        launch(args);
      }

    /**
     * The start method initializes all components of the page and
     * starts the application
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        pickNumPlayers();
        // add correct styling to each gamespace in board
        grid = new GridPane();
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                grid.add(new GameSpace(j), j, i);
            }
        }

        //initialize title

        if (twoPlayerGame) {
            title.setText("You are playing Connect4 - 2 player");
        }
        else {
            title.setText("You are playing Connect4 - Play against computer");
        }
        title.setStyle("-fx-font: 36 arial");

        prompt.setStyle("-fx-font: 24 arial");

        // setting title of Stage
        primaryStage.setTitle("Connect4 Client");

        //Positioning all elements
        VBox root = new VBox();
        root.setPadding(new Insets(10, 50, 50, 50));
        root.setSpacing(10);

        root.getChildren().add(title);
        root.getChildren().add(prompt);
        root.getChildren().add(grid);
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 300, 250);
        primaryStage.setScene(scene);

        primaryStage.show();


        connectToServer();

    }

    private void pickNumPlayers() {
        String decision;
        System.out.println("Enter 'P' if you want to play against another player; " +
                "Enter 'C' to play against computer.");
        decision = scan.nextLine();

        // if invalid input
        while (!decision.equals("P") && !decision.equals("C")) {
            System.out.println("Invalid input.\n" +
                    "Enter 'P' if you want to play against another player; " +
                    "Enter 'C' to play against computer.");
            decision = scan.nextLine();
        }

        if (decision.equals("C")) {
            twoPlayerGame = false;
        } else { // Play another player
            twoPlayerGame = true;
        }
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(host, 8000);

            // create input and output streams to communicate with server
            fromServer = new DataInputStream(socket.getInputStream());
            toServer = new DataOutputStream(socket.getOutputStream());

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // control game on thread
        new Thread(() -> {
           try {
               myToken = fromServer.readChar();

               if (myToken == PLAYER_1) {
                   otherToken = PLAYER_2;
                   Platform.runLater(() -> {
                       title.setText("You are Player 1 with the red token.");
                       if (twoPlayerGame) {
                           prompt.setText("Waiting for player 2 to join...");
                       }
                   });

//                   if (twoPlayerGame) {
//                       toServer.writeChar(TWO_PLAYER_GAME);
//                   }
//                   else {
//                       toServer.writeChar(ONE_PLAYER_GAME);
//                   }

                   // Receive startup notification from the server
                   fromServer.readInt(); // Whatever read is ignored

                   // The other player has joined
                   Platform.runLater(() ->
                           prompt.setText("Player 2 has joined. Please click a " +
                                   "column to make a move"));

                   // It is my turn
                   myTurn = true;
               }
               else if(myToken == PLAYER_2) {
                   otherToken = PLAYER_1;
                   Platform.runLater(() -> {
                       title.setText("You are Player 2 with the yellow token.");
                       prompt.setText("Waiting for player 1 to make a move");
                   });
               }

               while (continueToPlay) {
                   if (myToken == PLAYER_1) {
                       waitForPlayerAction(); // Wait for player 1 to move
                       sendMove(); // Send the move to the server
                       receiveInfoFromServer(); // Receive info from the server
                   }
                   else if (myToken == PLAYER_2) {
                       receiveInfoFromServer(); // Receive info from the server
                       waitForPlayerAction(); // Wait for player 2 to move
                       sendMove(); // Send player 2's move to the server
                   }
               }

           } catch (Exception ex) {
               ex.printStackTrace();
           }
        }).start();
    }

    /** Wait for the player to mark a cell */
    private void waitForPlayerAction() throws InterruptedException {
        while (waiting) {
            Thread.sleep(100);
        }

        waiting = true;
    }

    /**
     * Sends move to server
     * @throws IOException
     */
    private void sendMove() throws IOException {
        toServer.writeInt(selectedColumn);
        int valid = fromServer.readInt();

        if (valid == INVALID_TURN) {
            Platform.runLater(() -> {
                prompt.setText("Invalid Move - Please try again.");
            });
            waiting = true; //new
            try {
                waitForPlayerAction();
                sendMove();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            return;
        }

        // turn is valid
        Platform.runLater(() -> {
            prompt.setText("Waiting for " + (myToken == PLAYER_1? "player 2 " : "player 1 ") +
                    "to make a move...");
        });

        receiveInfoFromServer(); // status after my turn
        receiveMove(myToken);
        myTurn = false;
    }

    private void receiveInfoFromServer(){
        try {
            int status = fromServer.readInt();
            // need to receive last move no matter what
            if (!myTurn) {
                receiveMove(otherToken);
            }
//            receiveMove(otherToken);
            if (status == P1_WINNER) {
                continueToPlay = false;
                if (myToken == PLAYER_1) {
                    Platform.runLater(() -> prompt.setText("You won!"));
                } else {
                    Platform.runLater(() -> prompt.setText("You lost!"));
                }
            } else if (status == P2_WINNER) {
                continueToPlay = false;
                if (myToken == PLAYER_1) {
                    Platform.runLater(() -> prompt.setText("You lost!"));
                } else {
                    Platform.runLater(() -> prompt.setText("You won!"));
                }
            } else if (status == TIE) {
                continueToPlay = false;
                Platform.runLater(() -> prompt.setText("Tie game!"));
            } else { //continue
                if (!myTurn) {
                    myTurn = true;
                    Platform.runLater(() -> {
                        prompt.setText("Your turn - please click on a column" +
                                " to make a move.");
                    });
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Receive other players move
     * @throws IOException
     */
    private void receiveMove(char token) throws IOException {
        int row = fromServer.readInt();
        int column = fromServer.readInt();
        GameSpace temp = (GameSpace) getNodeFromGridPane(column, row);
        Platform.runLater(() -> {
            temp.setValue(token);
        });

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
            if (value == PLAYER_1) {
                chip.setStroke(Color.RED);
                chip.setFill(Color.RED);
                Platform.runLater(() -> {
                    getChildren().add(chip);
                });
            }
            else if (value == PLAYER_2) {
                chip.setStroke(Color.YELLOW);
                chip.setFill(Color.YELLOW);
                Platform.runLater(() -> {
                    getChildren().add(chip);
                });
            }
            else {
                Platform.runLater(() -> {
                    getChildren().clear();
                });
            }
        }

        /**
         * Handles clicks on the gameboard
         */
        private void handleMouseClick() {
            if (myTurn) {
                selectedColumn = column;
                waiting = false;
            }
        }

        /**
         * Checks for a win when playing against the computer
         * @param gameOver
         */
        private void checkForWinPlayAgainstComputer(int gameOver) {

        }
    }
}
