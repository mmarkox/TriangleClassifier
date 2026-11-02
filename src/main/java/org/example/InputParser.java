package org.example;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.example.exception.InvalidInputException;

/**
 * Turns one line of user input into exactly three positive side lengths.
 *
 * <p>Checks run in a fixed order and the first failing check determines the message:
 * <ol>
 *   <li>line length,</li>
 *   <li>empty input,</li>
 *   <li>format of every value,</li>
 *   <li>number of values,</li>
 *   <li>zero values.</li>
 * </ol>
 *
 * <p>Parsing is deliberately strict and locale-independent. {@code Scanner.nextDouble()} would
 * read {@code 2,5} as 2.5 under a German locale, which conflicts with the comma being a
 * separator. Unrestricted {@link BigDecimal} parsing would accept exponents such as
 * {@code 1e10000000}, which cost seconds of CPU in later arithmetic, as well as non-ASCII digits.
 */
public final class InputParser {

    /** Maximum accepted line length, checked before any other processing. */
    static final int MAX_LINE_LENGTH = 1000;

    /** Number of side lengths a triangle needs. */
    static final int SIDE_COUNT = 3;

    static final String TOO_LONG_MESSAGE =
            "Input is too long (maximum " + MAX_LINE_LENGTH + " characters).";
    static final String EMPTY_MESSAGE = "No input given. Please enter three side lengths.";
    static final String FORMAT_MESSAGE =
            "All numbers must follow the format x or x.xx and be separated by whitespace or comma.";
    static final String COUNT_MESSAGE_TEMPLATE = "Expected " + SIDE_COUNT + " side lengths but received %d.";
    static final String ZERO_MESSAGE = "Side lengths must be greater than zero.";

    /**
     * A comma (with optional whitespace around it) or a run of whitespace. Each comma counts as its
     * own separator, so {@code 3,,4} yields an empty token instead of being silently accepted.
     */
    private static final Pattern SEPARATOR = Pattern.compile("\\s*,\\s*|\\s+");

    /** ASCII digits, optionally followed by a dot and more digits. No sign, exponent or shorthand. */
    private static final Pattern NUMBER = Pattern.compile("[0-9]+(\\.[0-9]+)?");

    /**
     * Parses and validates one input line.
     *
     * @param line the raw line as typed by the user
     * @return exactly three side lengths, each greater than zero, in input order
     * @throws InvalidInputException if any check fails; the message is meant for the user
     * @throws NullPointerException  if {@code line} is {@code null}
     */
    public List<BigDecimal> parse(String line) {
        Objects.requireNonNull(line, "line");

        if (line.length() > MAX_LINE_LENGTH) {
            throw new InvalidInputException(TOO_LONG_MESSAGE);
        }

        String trimmed = line.strip();
        if (trimmed.isEmpty()) {
            throw new InvalidInputException(EMPTY_MESSAGE);
        }

        // Limit -1 keeps trailing empty tokens, so "3,4,5," is rejected rather than read as 3 values.
        String[] tokens = SEPARATOR.split(trimmed, -1);

        for (String token : tokens) {
            if (!NUMBER.matcher(token).matches()) {
                throw new InvalidInputException(FORMAT_MESSAGE);
            }
        }

        if (tokens.length != SIDE_COUNT) {
            throw new InvalidInputException(COUNT_MESSAGE_TEMPLATE.formatted(tokens.length));
        }

        List<BigDecimal> sides = List.of(
                new BigDecimal(tokens[0]),
                new BigDecimal(tokens[1]),
                new BigDecimal(tokens[2]));

        // The format allows no sign, so signum() is either 0 or 1. signum() ignores the scale:
        // "0", "0.0" and "000" are all zero.
        for (BigDecimal side : sides) {
            if (side.signum() == 0) {
                throw new InvalidInputException(ZERO_MESSAGE);
            }
        }

        return sides;
    }
}
