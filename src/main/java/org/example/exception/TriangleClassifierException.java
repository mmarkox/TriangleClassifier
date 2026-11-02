package org.example.exception;

/**
 * Base type for every error that is caused by the user's input and can be reported back to them.
 *
 * <p>The console loop catches only this type: its message is shown to the user and the input is
 * requested again. Any other exception indicates a programming error and is not caught there.
 */
public class TriangleClassifierException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a message that is meant to be shown to the user.
     *
     * @param message user-facing description of the problem
     */
    public TriangleClassifierException(String message) {
        super(message);
    }
}
