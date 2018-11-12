/**
 * @version 1.0
 * @author Kaysi Pilcher
 *
 * The Connect4Constants interface keeps track of what certain messages mean
 * between the Connect4Server and the Connect4Client
 *
 */

public interface Connect4Constants {

    int P1_WINNER = 1;
    int P2_WINNER = 2;
    int TIE = -1;
    int CONTINUE = 0;
    int INVALID_TURN = -2;
    int VALID_TURN = 0;

    int TWO_PLAYER_GAME = 2;
    int ONE_PLAYER_GAME = 1;


    char BLANK = ' ';
    char PLAYER_1 = 'X';
    char PLAYER_2 = 'O';

}
