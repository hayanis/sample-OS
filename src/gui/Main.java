/*
 * Shared class (Leader).
 * Starts the program by opening the main menu.
 */
package gui;

import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        try {
            // Use the normal look of the computer's operating system.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // If that fails, Java's default look is used.
        }
        new MainMenu().setVisible(true);
    }
}
