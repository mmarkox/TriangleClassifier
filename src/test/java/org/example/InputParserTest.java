package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import org.example.exception.InvalidInputException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class InputParserTest {

    private final InputParser parser = new InputParser();

    static Stream<Arguments> validLines() {
        return Stream.of(
                Arguments.of("3 4 5", List.of("3", "4", "5")),
                Arguments.of("3,4,5", List.of("3", "4", "5")),
                Arguments.of("3, 4, 5", List.of("3", "4", "5")),
                Arguments.of("3 ,4 ,5", List.of("3", "4", "5")),
                Arguments.of("3 , 4,5", List.of("3", "4", "5")),
                Arguments.of("  3   4\t5  ", List.of("3", "4", "5")),
                Arguments.of("2 2.0 2.00", List.of("2", "2.0", "2.00")),
                Arguments.of("007 7 7", List.of("007", "7", "7")),
                Arguments.of("0.125 1.5 2", List.of("0.125", "1.5", "2")),
                Arguments.of("9007199254740993 9007199254740992 5",
                        List.of("9007199254740993", "9007199254740992", "5")));
    }

    @ParameterizedTest
    @MethodSource("validLines")
    void parsesValidLines(String line, List<String> expected) {
        List<BigDecimal> expectedValues = expected.stream().map(BigDecimal::new).toList();
        // BigDecimal.equals is intended here: it checks that the exact typed value (incl. scale) is kept.
        assertEquals(expectedValues, parser.parse(line));
    }

    @Test
    void rejectsLineLongerThanLimit() {
        String line = "3 4 5" + " ".repeat(InputParser.MAX_LINE_LENGTH);
        assertMessage(line, "Input is too long (maximum 1000 characters).");
    }

    @Test
    void acceptsLineExactlyAtLimit() {
        String line = "3 4 5" + " ".repeat(InputParser.MAX_LINE_LENGTH - 5);
        assertEquals(3, parser.parse(line).size());
    }

    @Test
    void rejectsHugeExponentWithoutExpensiveArithmetic() {
        assertMessage("1e10000000 1 1", InputParser.FORMAT_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t"})
    void rejectsEmptyInput(String line) {
        assertMessage(line, "No input given. Please enter three side lengths.");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "abc 4 5", "NaN 4 5", "Infinity 4 5", "1e3 4 5", "0x1p3 4 5", "٣ 4 5",
            "+3 4 5", "-3 4 5", "3. 4 5", ".5 4 5",
            "3,,4", ",3,4,5", "3,4,5,", "3;4;5", "3 4 five", "3 4 5 abc"})
    void rejectsBadFormatWithSingleMessage(String line) {
        assertMessage(line, "All numbers must follow the format x or x.xx and be separated by whitespace or comma.");
    }

    static Stream<Arguments> wrongCounts() {
        return Stream.of(
                Arguments.of("3", 1),
                Arguments.of("3 4", 2),
                Arguments.of("3 4 5 6", 4),
                Arguments.of("2,5 3 4", 4));
    }

    @ParameterizedTest
    @MethodSource("wrongCounts")
    void rejectsWrongNumberOfValues(String line, int received) {
        assertMessage(line, "Expected 3 side lengths but received " + received + ".");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0 4 5", "0.0 4 5", "000 4 5", "4 5 0", "3 0.000 4"})
    void rejectsZero(String line) {
        assertMessage(line, "Side lengths must be greater than zero.");
    }

    private void assertMessage(String line, String expectedMessage) {
        InvalidInputException e = assertThrows(InvalidInputException.class, () -> parser.parse(line));
        assertEquals(expectedMessage, e.getMessage());
    }
}
