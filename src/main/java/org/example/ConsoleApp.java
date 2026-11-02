package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.example.exception.TriangleClassifierException;

/**
 * The interactive console front end.
 *
 * <p>Prints a welcome text, then classifies one line of input after another until the user types
 * {@code exit} or the input ends. Invalid input is answered with an error message and the user is
 * simply asked again. All text goes to the given output stream. The streams are injected so the
 * whole dialogue can be tested without a real console.
 */
public final class ConsoleApp {

    /** Exit code when the session ended normally (by {@code exit} or end of input). */
    public static final int EXIT_SUCCESS = 0;

    /** Exit code when the input could not be read. */
    public static final int EXIT_FAILURE = 1;

    /** Command that ends the session. Matched ignoring case and surrounding whitespace. */
    static final String EXIT_COMMAND = "exit";

    static final String WELCOME_LINE = "Welcome to Triangle Classifier!";
    static final String INSTRUCTIONS_LINE =
            "Triangle Classifier accepts positive decimal values separated by whitespace or comma.";
    static final String EXIT_HINT_LINE = "Type 'exit' to quit.";
    static final String PROMPT_LINE = "Please input triangle sides:";
    static final String GOODBYE_LINE = "Goodbye!";
    static final String END_OF_INPUT_LINE = "Input ended. Exiting.";
    static final String READ_ERROR_LINE = "Error: Could not read input. Exiting.";
    static final String ERROR_PREFIX = "Error: ";

    private final BufferedReader in;
    private final PrintStream out;
    private final InputParser parser = new InputParser();
    private final TriangleClassifier classifier = new TriangleClassifier();

    /**
     * Creates the app.
     *
     * @param in  source of user input, read line by line
     * @param out destination for all output, including error messages
     */
    public ConsoleApp(BufferedReader in, PrintStream out) {
        this.in = Objects.requireNonNull(in, "in");
        this.out = Objects.requireNonNull(out, "out");
    }

    /**
     * Runs the dialogue until the user types {@code exit}, the input ends, or it cannot be read.
     *
     * @return {@link #EXIT_SUCCESS} after {@code exit} or end of input, or {@link #EXIT_FAILURE} if
     *         the input could not be read
     */
    public int run() {
        out.println(WELCOME_LINE);
        out.println(INSTRUCTIONS_LINE);
        out.println(EXIT_HINT_LINE);

        while (true) {
            out.println(PROMPT_LINE);

            String line;
            try {
                line = in.readLine();
            } catch (IOException e) {
                out.println(READ_ERROR_LINE);
                return EXIT_FAILURE;
            }

            // Without this check, closed or piped input would make the loop run forever.
            if (line == null) {
                out.println(END_OF_INPUT_LINE);
                return EXIT_SUCCESS;
            }

            if (line.strip().equalsIgnoreCase(EXIT_COMMAND)) {
                out.println(GOODBYE_LINE);
                return EXIT_SUCCESS;
            }

            try {
                out.println(classify(line).label());
            } catch (TriangleClassifierException e) {
                out.println(ERROR_PREFIX + e.getMessage());
            }
        }
    }

    private TriangleType classify(String line) {
        List<BigDecimal> sides = parser.parse(line);
        Triangle triangle = new Triangle(sides.get(0), sides.get(1), sides.get(2));
        return classifier.classify(triangle);
    }
}
