/*
 * Shared class (Testing Lead).
 * Checks a typed value and turns it into a number.
 * If the value is wrong, it throws an error with a clear message for the user,
 * so the program never crashes on bad input.
 */
package gui;

public class InputCheck {

    public static int readNumber(String text, String fieldName, int min, int max) {
        text = text.trim();

        if (text.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }

        int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a whole number. You entered \"" + text + "\".");
        }

        if (value < 0 && min >= 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative. You entered " + value + ".");
        }
        if (value < min || value > max) {
            throw new IllegalArgumentException(fieldName + " must be from " + min + " to " + max
                    + ". You entered " + value + ".");
        }
        return value;
    }
}
