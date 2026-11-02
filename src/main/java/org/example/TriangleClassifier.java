package org.example;

import java.util.Objects;

/**
 * Classifies a valid {@link Triangle}. Has no state and does no I/O.
 */
public final class TriangleClassifier {

    /**
     * Returns the most specific type of the triangle. An equilateral triangle is reported as
     * {@link TriangleType#EQUILATERAL} only, not also as isosceles.
     *
     * <p>Relies on {@link Triangle} keeping its sides sorted ({@code a <= b <= c}): all three are
     * equal exactly when the shortest equals the longest, and two are equal exactly when a pair of
     * neighbours is equal.
     *
     * @param triangle a valid triangle
     * @return its classification
     * @throws NullPointerException if {@code triangle} is {@code null}
     */
    public TriangleType classify(Triangle triangle) {
        Objects.requireNonNull(triangle, "triangle");

        if (triangle.a().compareTo(triangle.c()) == 0) {
            return TriangleType.EQUILATERAL;
        }
        if (triangle.a().compareTo(triangle.b()) == 0 || triangle.b().compareTo(triangle.c()) == 0) {
            return TriangleType.ISOSCELES;
        }
        return TriangleType.SCALENE;
    }
}
