# Triangle Classifier — Architecture Evaluation & Implementation Specification

Evaluation of implementation options for the task in [`task.md`](task.md): an executable Java 21 console application that classifies a triangle as `Equilateral`, `Isosceles` or `Scalene` from three side lengths. The goals, in priority order, are **(1) accuracy** and **(2) runtime efficiency**. Where they conflict, accuracy wins.

This document is the **final output of step 1**. It is the specification the implementation step must follow. Section 9 is binding. Sections 2–8 explain why each choice was made.

> Prompt used for this step: [`prompts/01-architecture-evaluation.md`](prompts/01-architecture-evaluation.md).
> Every surprising claim below was checked on a JDK and comes with the input that reproduces it.

---

## 1. Requirements summary

| Area | Requirement |
|---|---|
| Interaction | Interactive console app. On start it prints a welcome message and instructions, then reads the sides from standard input. It keeps classifying one line after another; **invalid input gets an error message and the session continues**. Typing `exit` ends the session. |
| Input | Exactly **3** positive decimal values on one line, each in the form `x` or `x.xx`: digits, optionally followed by `.` and more digits. Separators are whitespace, a comma, or both (`3 4 5`, `3,4,5`, `3, 4, 5`). The comma is never a decimal point. |
| Reject | Wrong number of values (message says expected 3 and how many were received), any value not in the `x` / `x.xx` format (text, `NaN`, `Infinity`, hex, exponents, signs, `3.`, `.5`), zero values, impossible triangles including degenerate ones (`a + b = c`), and empty input. Every rejection gets a meaningful message. |
| Output | For valid input, exactly one of `Equilateral`, `Isosceles`, `Scalene`. **All output, including errors, goes to normal output (stdout).** |
| Platform | Maven, Java 21, JUnit 5, executable jar, `org.example.Main`. |

---

## 2. Decision 1 — Numeric representation

| Option | Accuracy | Efficiency | Complexity | Verdict |
|---|---|---|---|---|
| `long` | Exact | Fastest | Low | ❌ Cannot represent decimals, which the requirements demand |
| `double` (exact `==`) | ❌ Wrong results (see below) | Fastest | Low | ❌ |
| `double` + epsilon | ❌ Different wrong results | Fast | Medium | ❌ |
| **`BigDecimal`** | ✅ Exact for all decimal input | Microseconds per call | Low | ✅ **Chosen** |
| Hybrid (`long` fast path, `BigDecimal` fallback) | ✅ Exact | Fastest on whole numbers | High (two code paths) | ❌ Not justified here |

**Why not `double`**
- **The triangle check fails at the boundary.** `0.1 + 0.2` evaluates to `0.30000000000000004`, so `0.1 0.2 0.3`, a flat line, would be accepted and printed as `Scalene`.
- **It loses precision.** `9007199254740993` and `9007199254740992` parse to the same `double`, so `9007199254740993 9007199254740992 5` would come out as `Isosceles`. Inputs with more than about 15–17 significant digits behave the same way.
- **Epsilon comparison doesn't fix it.** It replaces those errors with others, because sides that really are different but closer than ε count as equal, and no single ε works at every scale.

**`BigDecimal` pitfalls and their mitigations**
- **Compare with `compareTo`, never `equals`.** `new BigDecimal("2.0").equals(new BigDecimal("2.00"))` is `false`, so `2 2.0 2.00` would not be classified `Equilateral`.
- **Huge exponents are a denial-of-service risk.** `new BigDecimal("1e10000000").add(BigDecimal.ONE)` took about **3.8 s** of CPU and produced a 10,000,001-digit number from an 11-character input. `1e999999999 + 1` throws `ArithmeticException` instead. Mitigation: the strict `x` / `x.xx` format allows no exponent, and the length of the input line is capped (Decision 2).
- **Non-ASCII digits get through.** The `BigDecimal` constructor accepts Unicode digits: `"٣"` (Arabic-Indic three) parses as `3`. The strict `[0-9]` format check rejects them.

**Efficiency.** With the input restricted to plain decimals of bounded length, `BigDecimal` addition and comparison run in time proportional to the number of digits, which means microseconds here. That cost can't be seen next to JVM startup or a human typing.

---

## 3. Decision 2 — Parsing and tokenizing input

| Option | Accuracy | Error-message quality | Verdict |
|---|---|---|---|
| `Scanner.nextDouble()` ×3 | ❌ Depends on the locale and returns `double` | Poor: it can't tell "2 values" apart from "waiting for more" | ❌ |
| `nextLine()` + split on `[\s,]+` | ❌ Treats `3,,4,5` as 3 valid values | Good | ❌ |
| **Read one line + split on `\s*,\s*\|\s+` with limit `-1`, then a strict check of each token** | ✅ | ✅ Precise | ✅ **Chosen** |
| Hand-written character tokenizer | ✅ | ✅ | ⚠️ More code to get the same result |

**Problems with `Scanner.nextDouble()`**
- **The decimal point depends on the locale.** With `Locale.GERMANY` (the default on many Austrian and German machines), `nextDouble()` **rejects `2.5`** (`InputMismatchException`) and **accepts `2,5` as 2.5**. That directly contradicts the requirement that the comma is only a separator.
- **It can't count values.** It reads tokens across lines, so with two values it just waits for a third instead of reporting "expected 3, received 2".

**The chosen pipeline** (applied to each line the user enters)
1. Read **one line** with `BufferedReader.readLine()`. It returns `null` at end of input, whereas `Scanner.nextLine()` throws.
2. Reject lines longer than **1,000 characters** before doing anything else.
3. `strip()` the line. Reject it if it is empty.
4. Split with `split("\\s*,\\s*|\\s+", -1)`. The limit `-1` keeps empty tokens, so these are caught as errors (checked on a JDK):
   - `3,,4` gives `[3, "", 4]`, so the empty value is reported.
   - `3,4,5,` gives `[3, 4, 5, ""]`, so the trailing separator is reported. Without `-1` this would silently become 3 valid values.
   - `3 , 4,5` gives `[3, 4, 5]`, as expected.
5. Check the **format** of every token against `[0-9]+(\.[0-9]+)?`. If **any** token fails, including an empty token, report the **single format message** (section 9.3). This rejects `abc`, `NaN`, `Infinity`, `1e3`, `0x1p3`, `٣`, `+3`, `-3`, `3.` and `.5`.
6. Check the **count**: exactly 3 tokens.
7. Convert with `new BigDecimal(token)`. Reject any value that `compareTo(BigDecimal.ZERO) == 0`, such as `0`, `0.0` or `000`. These match the format, so they need their own message.

Format is checked before count so that a line like `3,,4` gets the format message (it has a missing value) rather than a confusing "received 3".

---

## 4. Decision 3 — Validation strategy and error reporting

| Option | Pros | Cons | Verdict |
|---|---|---|---|
| `IllegalArgumentException` everywhere | Standard, no extra classes | Can't tell input errors from triangle errors or from programming bugs | ❌ |
| **Small custom hierarchy of unchecked exceptions** | Clear categories; meaningful messages; easy to assert in tests | Two or three extra small classes | ✅ **Chosen** |
| Sealed result type (`Valid` / `Invalid`) | Exhaustive `switch`; no exceptions | More plumbing; less idiomatic for validation failures | ⚠️ Good, but heavier than needed |

**Design**
- `TriangleClassifierException extends RuntimeException` is the common base. The console loop catches this one type, prints its message and continues with the next line.
  - `InvalidInputException` covers line length, empty input, format, count and zero values. It is thrown by `InputParser`.
  - `InvalidTriangleException` covers violations of the triangle inequality. It is thrown by `Triangle`.
- **Validation happens in two places:**
  - The **parser** handles syntax.
  - The **`Triangle` record** checks its own invariants in its constructor: every side `> 0` and `a + b > c` after sorting. A `Triangle` that exists is therefore always valid, even when it's built directly in tests rather than through the parser.
- The exact message texts are fixed in section 9.3.

---

## 5. Decision 4 — Classification algorithm

| Option | Comparisons | Bug risk | Order-independent | Verdict |
|---|---|---|---|---|
| **Sort, then compare** | 3 to sort + 1 inequality + up to 3 equality | Low | ✅ By construction | ✅ **Chosen** |
| Direct pairwise checks | 3 inequalities + up to 4 equality checks | Medium: easy to miss one permutation | ✅ Only if all checks are present | ⚠️ |
| Count distinct values (`Set`) | Hashing + allocation | High: `HashSet` uses `equals`, so `2.0` and `2.00` count as distinct | ✅ | ❌ |

**Logic.** Sort with `compareTo` (three compare-and-swaps, no collections) so that `a ≤ b ≤ c`. Then:
- The triangle is valid if and only if `a + b > c`. One check replaces three.
- `a.compareTo(c) == 0` means **Equilateral**.
- `a.compareTo(b) == 0 || b.compareTo(c) == 0` means **Isosceles**.
- Anything else is **Scalene**.

An equilateral triangle is technically also isosceles, but the program prints only `Equilateral`.

---

## 6. Decision 5 — Interaction flow

| Option | Usability | Automated testing | Verdict |
|---|---|---|---|
| Single attempt: print the result or the error, then exit | Simple, but one typo ends the program | Easiest | ❌ Not chosen |
| Ask again until the input is valid, then print the result and exit | A typo doesn't end the program, but only one triangle per run | Easy | ❌ Not chosen (superseded) |
| **Continuous session: classify line after line, keep going after errors, end on `exit`** | ✅ Best for a user who checks several triangles | ✅ With injected streams, a test feeds several lines and checks the full transcript | ✅ **Chosen** |

**Ending the session.**
- Typing **`exit`** prints `Goodbye!` and ends with exit code `0`. The command is matched ignoring case and surrounding whitespace (`EXIT`, ` exit `). Text that merely contains it, such as `exit now`, is invalid input.
- **End of input** also ends the session: `readLine()` returns `null` after Ctrl+Z then Enter on Windows, Ctrl+D on Linux and macOS, or at the end of piped input. It prints `Input ended. Exiting.` and exits with code `0`, because running out of piped lines is a normal way to finish. Without this check, closed or piped input would loop forever.
- Only a failure to **read** the input exits with code `1`.

This decision was revised after the first implementation: originally the app stopped after the first valid classification. See section 10.

**All text goes to stdout**, including error messages.

---

## 7. Decision 6 — Code structure

| Option | Testability | Readability | Startup cost | Verdict |
|---|---|---|---|---|
| Single `Main` class | Poor (I/O mixed with logic) | OK at first, degrades quickly | Minimal | ❌ Not production-ready |
| **Small layered design, no dependencies** | ✅ Pure core, injectable I/O | ✅ | Minimal | ✅ **Chosen** |
| Spring Boot | Good | Heavy for the problem | Seconds of startup, many MB of dependencies | ❌ Works against the efficiency goal |
| Picocli | Good | Good | Small | ❌ Built for command-line arguments, while this app reads interactive lines |

The structure is fixed in section 9.1.

---

## 8. Decision 7 — Testing strategy

| Layer | Tooling | What it proves |
|---|---|---|
| `TriangleClassifier` / `Triangle` | JUnit 5 `@ParameterizedTest` | Correct type for every order of the sides; the invariants hold |
| `InputParser` | JUnit 5 `@ParameterizedTest` | Every accepted and rejected format, each with the expected message |
| `ConsoleApp` | In-memory streams (`StringReader`, `ByteArrayOutputStream`) | Welcome text, continuing after results and errors, the `exit` command, end-of-input handling, exit code |
| Optional | Property-based testing (for example jqwik) | The result doesn't change when the sides are reordered or written with a different scale |

**Edge-case inputs to cover**

| Category | Inputs | Expected |
|---|---|---|
| Each type, every order | `3 3 3`; `3 4 4`, `4 3 4`, `4 4 3`; `3 4 5`, `5 4 3` | Equilateral / Isosceles / Scalene |
| Separators | `3,4,5`, `3, 4, 5`, `3 ,4 ,5`, `  3   4\t5  ` | Scalene |
| Scale differences | `2 2.0 2.00`, `1.5 1.50 2` | Equilateral, Isosceles |
| Leading zeros | `007 7 7` | Equilateral |
| Degenerate / impossible | `1 2 3`, `0.1 0.2 0.3`, `1 2 10` | Triangle message |
| Precision | `9007199254740993 9007199254740992 5` | Scalene (a `double` would say Isosceles) |
| Very large or small values | `1000000000000000000000 1000000000000000000000 1`, `0.0000001 0.0000001 0.0000001` | Isosceles, Equilateral |
| Count | `3`, `3 4`, `3 4 5 6` | Count message with the number received |
| Empty input | empty line, spaces only | Empty-input message |
| Zero | `0 4 5`, `0.0 4 5`, `000 4 5` | Zero message |
| Format (single message) | `abc 4 5`, `NaN 4 5`, `Infinity 4 5`, `1e3 4 5`, `0x1p3 4 5`, `٣ 4 5`, `+3 4 5`, `-3 4 5`, `3. 4 5`, `.5 4 5`, `3,,4`, `,3,4,5`, `3,4,5,`, `3;4;5` | Format message |
| Comma is not a decimal point | `2,5 3 4` | Count message: 4 values received |
| Abuse | Line longer than 1,000 characters; `1e10000000 1 1` | Rejected immediately, no CPU spike |
| Continuous session | `3 4 5`, `3 3 3`, `3 3 4`, `exit` | `Scalene`, `Equilateral`, `Isosceles`, `Goodbye!`; exit code `0` |
| Errors don't end the session | `3 4`, `abc`, `1 2 3`, `2 2 2`, `exit` | Three error messages, then `Equilateral`, then `Goodbye!` |
| `exit` command | `exit`, `EXIT`, `Exit`, `  exit  ` | `Goodbye!`; exit code `0` |
| Not the `exit` command | `exit now` | Format message; session continues |
| End of input | No line at all; or input ends after some lines | `Input ended. Exiting.`; exit code `0` |
| Read failure | Reader throws `IOException` | `Error: Could not read input. Exiting.`; exit code `1` |

---

## 9. Implementation specification (binding)

### 9.1 Structure

Java 21, package `org.example`, **zero runtime dependencies** (JUnit 5 for tests only). Packaged as the executable jar already configured in `pom.xml`.

```
org.example
├── Main                         // entry point: wires System.in/System.out into ConsoleApp, then System.exit(code)
├── ConsoleApp                   // welcome text, read–classify loop until 'exit' or end of input, returns exit code
│                                //   constructor takes BufferedReader in, PrintStream out (testable)
├── InputParser                  // String line → List<BigDecimal> (exactly 3): length, empty, format, count, zero
├── Triangle (record)            // a, b, c as BigDecimal; compact constructor sorts and validates invariants
├── TriangleType (enum)          // EQUILATERAL("Equilateral"), ISOSCELES("Isosceles"), SCALENE("Scalene")
├── TriangleClassifier           // pure: Triangle → TriangleType (no I/O)
└── exception
    ├── TriangleClassifierException   // extends RuntimeException
    ├── InvalidInputException         // extends TriangleClassifierException
    └── InvalidTriangleException      // extends TriangleClassifierException
```

Replace the IntelliJ template code in `Main.java`.

### 9.2 Console behaviour

On start, print:
```
Welcome to Triangle Classifier!
Triangle Classifier accepts positive decimal values separated by whitespace or comma.
Type 'exit' to quit.
```
Then loop:
1. Print `Please input triangle sides:`.
2. Read one line.
3. If the line is `null` (end of input), print `Input ended. Exiting.` and return exit code `0`.
4. If the stripped line equals `exit`, ignoring case, print `Goodbye!` and return exit code `0`.
5. Parse, build the `Triangle` and classify it. On success, print exactly one line: `Equilateral`, `Isosceles` or `Scalene`.
6. On `TriangleClassifierException`, print `Error: <message>`.
7. Go back to step 1.

If reading fails with an `IOException`, print `Error: Could not read input. Exiting.` and return exit code `1`. Everything is printed to stdout (`out`).

Example session:
```
Welcome to Triangle Classifier!
Triangle Classifier accepts positive decimal values separated by whitespace or comma.
Type 'exit' to quit.
Please input triangle sides:
3 4
Error: Expected 3 side lengths but received 2.
Please input triangle sides:
3, 4, 5
Scalene
Please input triangle sides:
2 2 2
Equilateral
Please input triangle sides:
exit
Goodbye!
```

### 9.3 Validation order and messages

Checks run in this order on each line. The first failing check determines the message.

| # | Check | Exception | Message |
|---|---|---|---|
| 1 | Line length ≤ 1,000 characters | `InvalidInputException` | `Input is too long (maximum 1000 characters).` |
| 2 | Line not blank after `strip()` | `InvalidInputException` | `No input given. Please enter three side lengths.` |
| 3 | Every token matches `[0-9]+(\.[0-9]+)?` | `InvalidInputException` | `All numbers must follow the format x or x.xx and be separated by whitespace or comma.` |
| 4 | Exactly 3 tokens | `InvalidInputException` | `Expected 3 side lengths but received <n>.` |
| 5 | No side equals zero | `InvalidInputException` | `Side lengths must be greater than zero.` |
| 6 | After sorting, `a + b > c` | `InvalidTriangleException` | `Sides <a>, <b> and <c> cannot form a triangle: the sum of the two shorter sides must be greater than the longest side.` |

- Check 3 uses **one message for every format error**, including signs (`+3`, `-3`), shorthand (`3.`, `.5`), text, exponents and empty tokens.
- In check 6, `<a>`, `<b>` and `<c>` are the sorted values as the user typed them (`BigDecimal.toPlainString()`).
- The `Triangle` constructor also rejects sides `≤ 0` with an `InvalidTriangleException`, as a safeguard for code that builds a `Triangle` without the parser.

### 9.4 Numeric and classification rules

- All sides are `BigDecimal`. They are compared only with `compareTo`, never `equals`, `double` or `float`.
- Sort so `a ≤ b ≤ c`. `a == c` is Equilateral, `a == b || b == c` is Isosceles, and anything else is Scalene.
- The output text comes only from `TriangleType`.

### 9.5 Tests

- JUnit 5 parameterized tests for `TriangleClassifier`, `Triangle` and `InputParser` that cover every row of the edge-case table in section 8, asserting both the exception type and the exact message.
- `ConsoleApp` tests with in-memory streams that check the full transcript and the exit code for: several classifications in one session, errors that don't end the session, the `exit` command (any case, surrounding whitespace), text containing `exit`, end of input, and a read failure.

**Result.** This is exact for every accepted input, rejects invalid input with meaningful messages, and keeps each classification in the microsecond range. The strict format and the line-length cap also close the denial-of-service hole that unrestricted `BigDecimal` parsing would open.

---

## 10. Resolved decisions

| # | Question | Decision |
|---|---|---|
| 1 | After invalid input, should the app ask again or exit? | **Ask again.** Revised after the first implementation: the app also keeps going after a *valid* classification, and the session ends only when the user types `exit` or the input ends. |
| 2 | Accept shorthand forms such as `3.`, `.5`, `+3`? | **No.** Reject them, and any other non-`x` / `x.xx` value, with the single format message. |
| 3 | Maximum input line length? | 1,000 characters. |
| 4 | Should errors go to stderr or stdout? | **stdout** (normal output). |
| 5 | Should Equilateral also count as Isosceles? | No. Print only `Equilateral`. |
| 6 | Should leading zeros (`007`) be accepted? | Yes. They are read as `7`. |
| — | Does `x.xx` limit the number of decimal places? | No. It means digits optionally followed by `.` and digits, so `2.5` and `0.125` are both valid. |
