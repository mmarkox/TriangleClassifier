package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TriangleClassifierTest {

    private final TriangleClassifier classifier = new TriangleClassifier();

    static Stream<Arguments> triangles() {
        return Stream.of(
                // Each type, every order
                Arguments.of("3", "3", "3", TriangleType.EQUILATERAL),
                Arguments.of("3", "4", "4", TriangleType.ISOSCELES),
                Arguments.of("4", "3", "4", TriangleType.ISOSCELES),
                Arguments.of("4", "4", "3", TriangleType.ISOSCELES),
                Arguments.of("4", "3", "3", TriangleType.ISOSCELES),
                Arguments.of("3", "4", "5", TriangleType.SCALENE),
                Arguments.of("3", "5", "4", TriangleType.SCALENE),
                Arguments.of("4", "3", "5", TriangleType.SCALENE),
                Arguments.of("4", "5", "3", TriangleType.SCALENE),
                Arguments.of("5", "3", "4", TriangleType.SCALENE),
                Arguments.of("5", "4", "3", TriangleType.SCALENE),
                // Scale differences: equal values written differently
                Arguments.of("2", "2.0", "2.00", TriangleType.EQUILATERAL),
                Arguments.of("1.5", "1.50", "2", TriangleType.ISOSCELES),
                // Leading zeros
                Arguments.of("007", "7", "7", TriangleType.EQUILATERAL),
                // Precision: a double would treat the first two as equal
                Arguments.of("9007199254740993", "9007199254740992", "5", TriangleType.SCALENE),
                // Very large and very small values
                Arguments.of("1000000000000000000000", "1000000000000000000000", "1", TriangleType.ISOSCELES),
                Arguments.of("0.0000001", "0.0000001", "0.0000001", TriangleType.EQUILATERAL),
                // Barely valid: sum of the shorter sides only just exceeds the longest
                Arguments.of("0.1", "0.2", "0.29999999999999999999", TriangleType.SCALENE));
    }

    @ParameterizedTest
    @MethodSource("triangles")
    void classifies(String a, String b, String c, TriangleType expected) {
        assertEquals(expected, classifier.classify(TriangleTest.triangle(a, b, c)));
    }

    @Test
    void labelsMatchRequiredOutput() {
        assertEquals("Equilateral", TriangleType.EQUILATERAL.label());
        assertEquals("Isosceles", TriangleType.ISOSCELES.label());
        assertEquals("Scalene", TriangleType.SCALENE.label());
    }

    @Test
    void rejectsNull() {
        assertThrows(NullPointerException.class, () -> classifier.classify(null));
    }
}
