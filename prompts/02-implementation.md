# Prompt 02 — Implementation

**Purpose:** Second step of the assignment: implement the application and its tests exactly as specified in step 1.
**Inputs:** `task.md`, `solutions.md` (output of step 1), `pom.xml`
**Expected output:** production code in `src/main/java`, tests in `src/test/java`, and a passing `mvn package`

---

## Prompt

You are acting as a **senior Java developer**. Implement the Triangle Classifier exactly as specified in `solutions.md`.

### Context

- `task.md` describes the assignment.
- `solutions.md` is the architecture evaluation from step 1.
  - **Section 9 ("Implementation specification") is binding.** Follow it exactly: package and class structure, console texts, validation order, error messages, numeric rules and tests.
  - Sections 1–8 explain the reasons behind each decision. Use them to resolve details, but do not change any decision.
  - Section 10 lists the resolved decisions.
- `pom.xml` is already configured: Java 21, JUnit 5 (test scope only), an executable jar with main class `org.example.Main`. Do not add runtime dependencies.
- `src/main/java/org/example/Main.java` still contains the IntelliJ template. Replace it.

### Requirements

1. **Production code** in `src/main/java/org/example`, with the structure from section 9.1:
   - `Main`, `ConsoleApp`, `InputParser`, `Triangle` (record), `TriangleType` (enum), `TriangleClassifier`.
   - In the sub-package `org.example.exception`: `TriangleClassifierException`, `InvalidInputException`, `InvalidTriangleException`.
2. **Behaviour:**
   - Console texts and the read/ask-again loop exactly as in section 9.2.
   - Validation order and the exact message texts from section 9.3. Copy the messages character for character.
3. **Accuracy rules** (section 9.4):
   - Use only `BigDecimal`, compared with `compareTo`. Never use `double`, `float` or `BigDecimal.equals` for side values.
   - Sort the sides, then classify.
4. **Testability:**
   - `ConsoleApp` takes its input and output streams in its constructor and returns an exit code.
   - Only `Main` calls `System.exit`.
   - Handle an `IOException` while reading input gracefully: print an error message and return exit code `1`. Never show a stack trace to the user.
5. **Tests** in `src/test/java/org/example` (section 9.5):
   - JUnit 5 parameterized tests that cover **every row** of the edge-case table in section 8.
   - Assert the exception type **and** the exact message.
   - `ConsoleApp` tests with in-memory streams that check the full transcript and the exit code. Normalize line endings so the tests pass on Windows and Linux.
6. **Code quality:**
   - Small, final classes; immutable where possible.
   - Javadoc on public types and methods, explaining *why* where it isn't obvious, for example why `compareTo` is used instead of `equals`.
   - Constants for limits, patterns and messages; no magic values.
   - Use only standard Java features: records, enums, `switch` expressions where they help.

### Verification

- Run `mvn package`. All tests must pass and `target/TriangleClassifier-1.0-SNAPSHOT.jar` must be produced.
- Run the jar and try a valid input, an invalid input followed by a valid one, and end of input.

### Output

- Write the files to the project.
- Report the files you created, the test results, and any point where the specification was unclear and how you resolved it. Do not silently deviate from `solutions.md`.
