package org.example.exception;

/**
 * Thrown when three side lengths are syntactically valid numbers but cannot form a triangle.
 */
public final class InvalidTriangleException extends TriangleClassifierException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a message that is meant to be shown to the user.
     *
     * @param message user-facing description of the problem
     */
    public InvalidTriangleException(String message) {
        super(message);
    }
}
