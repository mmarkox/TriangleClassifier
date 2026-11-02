package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.example.exception.InvalidTriangleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TriangleTest {

    @Test
    void sortsSidesForEveryInputOrder() {
        String[][] orders = {
                {"3", "4", "5"}, {"3", "5", "4"}, {"4", "3", "5"},
                {"4", "5", "3"}, {"5", "3", "4"}, {"5", "4", "3"}};
        for (String[] order : orders) {
            Triangle t = triangle(order[0], order[1], order[2]);
            assertEquals(0, t.a().compareTo(new BigDecimal("3")));
            assertEquals(0, t.b().compareTo(new BigDecimal("4")));
            assertEquals(0, t.c().compareTo(new BigDecimal("5")));
        }
    }

    static Stream<Arguments> impossibleTriangles() {
        return Stream.of(
                Arguments.of("1", "2", "3", "1", "2", "3"),
                Arguments.of("3", "1", "2", "1", "2", "3"),
                Arguments.of("0.1", "0.2", "0.3", "0.1", "0.2", "0.3"),
                Arguments.of("1", "2", "10", "1", "2", "10"),
                Arguments.of("10", "1", "2", "1", "2", "10"));
    }

    @ParameterizedTest
    @MethodSource("impossibleTriangles")
    void rejectsImpossibleAndDegenerateTriangles(String x, String y, String z, String a, String b, String c) {
        InvalidTriangleException e = assertThrows(InvalidTriangleException.class, () -> triangle(x, y, z));
        assertEquals("Sides " + a + ", " + b + " and " + c
                        + " cannot form a triangle: the sum of the two shorter sides must be greater than the longest side.",
                e.getMessage());
    }

    @Test
    void rejectsNonPositiveSidesWhenBuiltDirectly() {
        InvalidTriangleException zero = assertThrows(InvalidTriangleException.class, () -> triangle("0", "4", "5"));
        assertEquals("Side lengths must be greater than zero.", zero.getMessage());

        InvalidTriangleException negative = assertThrows(InvalidTriangleException.class, () -> triangle("3", "-4", "5"));
        assertEquals("Side lengths must be greater than zero.", negative.getMessage());
    }

    @Test
    void rejectsNullSides() {
        assertThrows(NullPointerException.class, () -> new Triangle(null, BigDecimal.ONE, BigDecimal.ONE));
    }

    static Triangle triangle(String a, String b, String c) {
        return new Triangle(new BigDecimal(a), new BigDecimal(b), new BigDecimal(c));
    }
}
