/**
 * This class provides a module to play against computer.
 *
 * @author Kaysi Pilcher
 * @version 1.0
 */
package core;

public class Connect4ComputerPlayer {

    /**
     * This method completes the computers turn.
     * @param game the game being played
     */
    public static void takeTurn(Connect4 game) {
        int column = (int)(Math.random()* 7) + 1;
        while (!game.validTurn(column)) {
            column = (int)(Math.random()* 7) + 1;
        }
        game.takeTurn(column);
    }
}
