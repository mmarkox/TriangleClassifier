package org.example;

/**
 * The classification result. Each constant carries the exact text that is printed to the user,
 * so the output wording is defined in exactly one place.
 */
public enum TriangleType {

    /** All three sides are equal. */
    EQUILATERAL("Equilateral"),

    /** Exactly two sides are equal. */
    ISOSCELES("Isosceles"),

    /** All three sides are different. */
    SCALENE("Scalene");

    private final String label;

    TriangleType(String label) {
        this.label = label;
    }

    /**
     * Returns the text printed to the user for this type.
     *
     * @return the user-facing label, for example {@code "Equilateral"}
     */
    public String label() {
        return label;
    }
}
