/**
 * This is the entry class for the Connect4 game.
 *
 * @author Kaysi Pilcher
 * @version 1.0
 */
package ui;

import java.util.Scanner;

public class Entry {
    public static void main(String args[]) {

        Scanner scan = new Scanner(System.in);
        System.out.println("Welcome to Connect4. Press 'C' for the console based game " +
                "or 'G' for the GUI base game");
        String input = scan.nextLine();

        while (!input.equals("C") && !input.equals("G")) {
            System.out.println("Invalid input. Press 'C' for the console based game " +
                    "or 'G' for the GUI base game");
            input = scan.nextLine();
        }

        if (input.equals("C")) {
            Connect4TextConsole.Entry();
        }
        else {
            Connect4GUI.main(args);
        }
    }
}