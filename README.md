# Triangle Classifier

A console application that classifies triangles by the lengths of their sides.

The application accepts three side lengths on one line and outputs:
- **Equilateral** when all three sides are equal,
- **Isosceles** when exactly two sides are equal,
- **Scalene** when all three sides are different.

It then asks for the next triangle, and keeps going until the user inputs **`exit`**.

The application rejects the following user inputs:
- input that doesn't contain exactly three values
- values that aren't positive decimal numbers, such as text, negative numbers, `1e3` or `.5`
- sides of length zero
- sides that can't form a triangle, such as `1 2 10`, or the flat `1 2 3`

After an error, you can simply enter the next triangle.

## Requirements

- **Java 21** or newer
- **Maven 3.9+**, or the Maven bundled with IntelliJ IDEA

## Build

```bash
mvn package
```

This compiles the code, runs all tests and creates the executable jar `target/TriangleClassifier-1.0-SNAPSHOT.jar`.

## Run

```bash
java -jar target/TriangleClassifier-1.0-SNAPSHOT.jar
```

In IntelliJ IDEA, you can also run `org.example.Main` directly.

## Usage

Enter three side lengths per line. The app classifies each line and keeps running until you type `exit`.

```
Welcome to Triangle Classifier!
Triangle Classifier accepts positive decimal values separated by whitespace or comma.
Type 'exit' to quit.
Please input triangle sides:
3 4 5
Scalene
Please input triangle sides:
2, 2, 2
Equilateral
Please input triangle sides:
2.5 2.5 4
Isosceles
Please input triangle sides:
1 2 3
Error: Sides 1, 2 and 3 cannot form a triangle: the sum of the two shorter sides must be greater than the longest side.
Please input triangle sides:
exit
Goodbye!
```

### Input rules

- **Exactly three** values per line.
- Each value is a **positive decimal** in the form `x` or `x.xx`, using `.` as the decimal point. For example `3`, `2.5` and `0.125` are valid.
- Values are separated by **whitespace, a comma, or both**: `3 4 5`, `3,4,5`, `3, 4, 5`. The comma is never a decimal point, so `2,5` means two values.
- The sides must form a real triangle. The two shorter sides must add up to **more** than the longest side, so a flat triangle such as `1 2 3` is rejected.
- `exit` ends the session. It's matched ignoring upper or lower case.

### Error messages

| Problem | Message |
|---|---|
| Line longer than 1,000 characters | `Input is too long (maximum 1000 characters).` |
| Empty line | `No input given. Please enter three side lengths.` |
| Any value not in the `x` / `x.xx` format (text, `-3`, `+3`, `1e3`, `.5`, `3.`, `3,,4`) | `All numbers must follow the format x or x.xx and be separated by whitespace or comma.` |
| Not exactly three values | `Expected 3 side lengths but received <n>.` |
| A side is zero | `Side lengths must be greater than zero.` |
| The sides can't form a triangle | `Sides <a>, <b> and <c> cannot form a triangle: the sum of the two shorter sides must be greater than the longest side.` |

After an error, the app asks for the next input.

### Exit codes

| Code | Meaning |
|---|---|
| `0` | The user typed `exit`, or the input ended (for example, piped input ran out) |
| `1` | The input could not be read |

You can also pipe input in. This works in Bash and in PowerShell:

```bash
echo "3 4 5" | java -jar target/TriangleClassifier-1.0-SNAPSHOT.jar
```

## Tests

```bash
mvn test
```

The JUnit 5 tests cover each triangle type in every order of the sides, separators, numbers written differently (`2`, `2.0`, `2.00`), precision cases that floating point gets wrong (`0.1 0.2 0.3`, `9007199254740993 9007199254740992 5`), and every rejected input. They also check the full console dialogue, including the `exit` command and end of input.

## Project structure

```
src/main/java/org/example
├── Main                  entry point
├── ConsoleApp            console dialogue: read, classify, print, repeat until 'exit'
├── InputParser           turns one input line into three validated side lengths
├── Triangle              immutable, always-valid triangle (sides stored sorted)
├── TriangleType          Equilateral / Isosceles / Scalene, with the output text
├── TriangleClassifier    classifies a Triangle; no I/O
└── exception             TriangleClassifierException, InvalidInputException, InvalidTriangleException
```

## Design decisions

The options that were considered, their trade-offs, and the final specification are documented in [`solutions.md`](solutions.md). The key points:

- **`BigDecimal` instead of `double`.** `double` gets real inputs wrong. It accepts the flat triangle `0.1 0.2 0.3`, because `0.1 + 0.2` evaluates to `0.30000000000000004`. It also treats `9007199254740993` and `9007199254740992` as equal.
- **`compareTo` instead of `equals`.** With `equals`, `2.0` and `2.00` would count as different lengths.
- **Strict input format.** It is locale-independent, so `2,5` is never read as 2.5. It also rejects exponent notation such as `1e10000000`, which would otherwise make `BigDecimal` arithmetic take seconds.
- **Sort the sides first.** The triangle check becomes a single comparison (`a + b > c`), and the classification is independent of the order the sides are entered in.

## How this project was developed

The work was done step by step with an AI assistant. Each step's prompt is kept in [`prompts/`](prompts):

1. [`01-architecture-evaluation.md`](prompts/01-architecture-evaluation.md): evaluate the implementation options. The result is [`solutions.md`](solutions.md).
2. [`02-implementation.md`](prompts/02-implementation.md): implement the specification, with tests.
3. [`03-continuous-session.md`](prompts/03-continuous-session.md): change request to keep classifying until `exit`.

The original task is in [`task.md`](task.md).
