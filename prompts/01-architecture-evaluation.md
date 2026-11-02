# Prompt 01 — Architecture evaluation

**Purpose:** First step of the assignment: evaluate possible implementations before writing any code.
**Inputs:** `task.md`, `pom.xml`
**Expected output:** `solutions.md` in the project root

---

## Prompt

You are acting as a **senior software architect**. Your job in this step is to **evaluate and document implementation options**. Do **not** write production code yet.

### Context

- Read `task.md` in the project root. It describes the assignment: a production-ready, executable Java application that classifies a triangle as `Equilateral`, `Isosceles` or `Scalene` from its three side lengths.
- The project is a Maven project (`pom.xml`) targeting **Java 21**, with JUnit 5 for tests, packaged as an executable jar. The main class is `org.example.Main`.
- This is a job assignment. The resulting document will be read by reviewers, so the reasoning must be clear, precise and verifiable.

### Goals (in priority order)

1. **Accuracy.** Every valid input gets the correct classification, and every invalid input is rejected.
2. **Runtime efficiency.**

When the two goals conflict, accuracy wins. Say so explicitly wherever a trade-off is made.

### Fixed requirements (already decided; do not re-open)

**Interaction**
- The application is interactive and reads from the console (standard input).
- On start it prints a welcome message and instructions, for example:
  ```
  Welcome to Triangle Classifier!
  Please input triangle sides.
  Triangle Classifier accepts positive decimal values separated by whitespace or comma.
  ```

**Input format**
- Exactly **three** values on one line.
- Values are **positive decimal numbers** (for example `3`, `2.5`, `0.75`). Use `.` as the decimal point.
- Separators are whitespace, a comma, or both, for example `3 4 5`, `3,4,5` and `3, 4, 5`. The comma is **only** a separator, never a decimal point.

**Validation.** Reject the input with a **meaningful, specific error message** in each of these cases:
- The number of values is not exactly 3. The message states how many values were expected and how many were received.
- A value is not a valid decimal number, for example text, `NaN`, `Infinity`, hex or other unsupported formats.
- A value is zero or negative.
- The sides cannot form a triangle because they violate the triangle inequality. This includes degenerate cases where `a + b = c`.
- The input is empty or blank.

**Output.** For valid input, print exactly one of `Equilateral`, `Isosceles` or `Scalene`.

### What to evaluate

For each decision below, describe **at least two realistic options**. For each option give its pros and cons, and assess its impact on **accuracy** and on **runtime efficiency**. End each decision with a clear recommendation and the reasoning behind it.

1. **Numeric representation.** Consider `long`, `double` (with and without epsilon comparison), `BigDecimal`, and a hybrid approach. Give concrete inputs that each option gets wrong, if any. Cover pitfalls such as floating-point rounding, precision loss on large values, `BigDecimal.equals` versus `compareTo`, and extreme exponents (for example `1e999999999`) as a denial-of-service risk.
2. **Parsing and tokenizing input.** Consider `Scanner` methods (`nextDouble`, `nextLine`), a regex split on whitespace and commas, and manual tokenizing. Cover locale sensitivity, empty tokens (for example `3,,4`), and leading or trailing separators.
3. **Validation strategy and error reporting.** Consider a custom exception hierarchy, `IllegalArgumentException`, and a result or sealed type. Explain how each produces specific, user-friendly messages, and where validation should live (parser or domain object).
4. **Classification algorithm.** Consider sorting the sides first, direct pairwise comparison, and counting distinct values. Assess the number of comparisons, risk of bugs, and order independence.
5. **Interaction flow on invalid input.** Consider exiting with a non-zero exit code, re-prompting until the input is valid, and offering an option to quit. Explain the impact on usability and on automated testing.
6. **Code structure.** Consider a single class, a small layered design (for example parser, domain record, enum, classifier, console UI), and a framework such as Spring or Picocli. Assess testability, readability, and startup cost.
7. **Testing strategy.** Cover the unit, boundary, invalid-input and console I/O tests needed to *prove* accuracy, and list concrete edge-case inputs.

### Constraints

- Keep runtime dependencies at zero unless an option justifies one. Test dependencies are fine.
- Prefer standard Java 21 features where they help, such as records, enums, sealed types and pattern matching.
- Claims must be correct. Where a claim is surprising (for example a floating-point failure), give the exact input that demonstrates it.
- Do not invent requirements. If something is still unspecified, list it as an open question with a proposed default.

### Output format

Write the result to **`solutions.md`** in the project root, in Markdown, with this structure:

1. **Title and goals**: one short paragraph.
2. **Requirements summary**: the fixed requirements above, restated concisely.
3. **One section per decision (1–7)**: options, a comparison table (accuracy, efficiency, complexity), and the recommendation.
4. **Recommended solution**: a numbered summary of the chosen design, including the proposed package and class structure.
5. **Open questions**: anything still unspecified, each with a proposed default.

Keep it concise and skimmable. Use tables for comparisons and short bullets for pros and cons. Avoid filler.
