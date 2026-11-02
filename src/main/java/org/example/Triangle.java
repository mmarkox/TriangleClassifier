package org.example;

import java.math.BigDecimal;
import java.util.Objects;

import org.example.exception.InvalidTriangleException;

/**
 * An immutable, always-valid triangle.
 *
 * <p>The sides are stored sorted, so that {@code a <= b <= c}. Sorting once here keeps both the
 * validity check and the classification simple: the triangle inequality reduces to the single
 * check {@code a + b > c}.
 *
 * <p>All comparisons use {@link BigDecimal#compareTo}, never {@link BigDecimal#equals}, because
 * {@code equals} also compares the scale: {@code 2.0} and {@code 2.00} are not {@code equals},
 * but they are the same length.
 *
 * @param a the shortest side
 * @param b the middle side
 * @param c the longest side
 */
public record Triangle(BigDecimal a, BigDecimal b, BigDecimal c) {

    static final String NON_POSITIVE_SIDE_MESSAGE = "Side lengths must be greater than zero.";

    /**
     * Sorts the sides and validates them.
     *
     * @throws InvalidTriangleException if a side is not greater than zero, or if the sides violate
     *                                  the triangle inequality (including degenerate triangles
     *                                  where {@code a + b = c})
     * @throws NullPointerException     if any side is {@code null}
     */
    public Triangle {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        Objects.requireNonNull(c, "c");

        // Safeguard for callers that bypass InputParser.
        if (a.signum() <= 0 || b.signum() <= 0 || c.signum() <= 0) {
            throw new InvalidTriangleException(NON_POSITIVE_SIDE_MESSAGE);
        }

        // Three compare-and-swaps sort the sides without allocating a collection.
        BigDecimal tmp;
        if (a.compareTo(b) > 0) {
            tmp = a;
            a = b;
            b = tmp;
        }
        if (b.compareTo(c) > 0) {
            tmp = b;
            b = c;
            c = tmp;
        }
        if (a.compareTo(b) > 0) {
            tmp = a;
            a = b;
            b = tmp;
        }

        // BigDecimal addition is exact, so this decides degenerate cases such as 0.1 + 0.2 = 0.3
        // correctly. A double would get 0.30000000000000004 and wrongly accept them.
        if (a.add(b).compareTo(c) <= 0) {
            throw new InvalidTriangleException(
                    "Sides %s, %s and %s cannot form a triangle: the sum of the two shorter sides must be greater than the longest side."
                            .formatted(a.toPlainString(), b.toPlainString(), c.toPlainString()));
        }
    }
}
