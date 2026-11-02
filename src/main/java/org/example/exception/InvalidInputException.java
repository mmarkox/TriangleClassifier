package org.example.exception;

/**
 * Thrown when an input line cannot be turned into three side lengths: it is too long, empty,
 * badly formatted, has the wrong number of values, or contains a zero.
 */
public final class InvalidInputException extends TriangleClassifierException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a message that is meant to be shown to the user.
     *
     * @param message user-facing description of the problem
     */
    public InvalidInputException(String message) {
        super(message);
    }
}
