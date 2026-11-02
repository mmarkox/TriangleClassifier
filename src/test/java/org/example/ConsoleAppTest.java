package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConsoleAppTest {

    private static final String HEADER = """
            Welcome to Triangle Classifier!
            Triangle Classifier accepts positive decimal values separated by whitespace or comma.
            Type 'exit' to quit.
            """;

    private static final String PROMPT = "Please input triangle sides:\n";

    @Test
    void classifiesUntilExit() {
        Result result = run("3, 4, 5\n3 3 3\n3 3 4\nexit\n");
        assertEquals(0, result.exitCode());
        assertEquals(HEADER
                + PROMPT + "Scalene\n"
                + PROMPT + "Equilateral\n"
                + PROMPT + "Isosceles\n"
                + PROMPT + "Goodbye!\n", result.output());
    }

    @Test
    void continuesAfterInvalidInput() {
        Result result = run("3 4\nabc\n1 2 3\n2 2 2\nexit\n");
        assertEquals(0, result.exitCode());
        assertEquals(HEADER
                + PROMPT + "Error: Expected 3 side lengths but received 2.\n"
                + PROMPT + "Error: All numbers must follow the format x or x.xx and be separated by whitespace or comma.\n"
                + PROMPT + "Error: Sides 1, 2 and 3 cannot form a triangle: the sum of the two shorter sides must be greater than the longest side.\n"
                + PROMPT + "Equilateral\n"
                + PROMPT + "Goodbye!\n", result.output());
    }

    @ParameterizedTest
    @ValueSource(strings = {"exit", "EXIT", "Exit", "  exit  ", "\texit"})
    void exitCommandIgnoresCaseAndWhitespace(String command) {
        Result result = run(command + "\n3 4 5\n");
        assertEquals(0, result.exitCode());
        assertEquals(HEADER + PROMPT + "Goodbye!\n", result.output());
    }

    @Test
    void exitInsideOtherTextIsInvalidInput() {
        Result result = run("exit now\nexit\n");
        assertEquals(HEADER
                + PROMPT + "Error: All numbers must follow the format x or x.xx and be separated by whitespace or comma.\n"
                + PROMPT + "Goodbye!\n", result.output());
    }

    @Test
    void endsWhenInputEndsImmediately() {
        Result result = run("");
        assertEquals(0, result.exitCode());
        assertEquals(HEADER + PROMPT + "Input ended. Exiting.\n", result.output());
    }

    @Test
    void endsWhenInputEndsAfterClassifications() {
        Result result = run("3 4 5\nabc\n");
        assertEquals(0, result.exitCode());
        assertEquals(HEADER
                + PROMPT + "Scalene\n"
                + PROMPT + "Error: All numbers must follow the format x or x.xx and be separated by whitespace or comma.\n"
                + PROMPT + "Input ended. Exiting.\n", result.output());
    }

    @Test
    void reportsReadErrorWithoutStackTrace() {
        Reader failing = new Reader() {
            @Override
            public int read(char[] buffer, int offset, int length) throws IOException {
                throw new IOException("boom");
            }

            @Override
            public void close() {
            }
        };
        Result result = run(new BufferedReader(failing));
        assertEquals(1, result.exitCode());
        assertEquals(HEADER + PROMPT + "Error: Could not read input. Exiting.\n", result.output());
    }

    private static Result run(String input) {
        return run(new BufferedReader(new StringReader(input)));
    }

    private static Result run(BufferedReader in) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        int exitCode = new ConsoleApp(in, out).run();
        // println uses the platform line separator; normalize so tests pass on Windows and Linux.
        String output = bytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        return new Result(exitCode, output);
    }

    private record Result(int exitCode, String output) {
    }
}
