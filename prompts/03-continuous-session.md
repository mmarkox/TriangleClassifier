# Prompt 03 — Continuous session with `exit` command

**Purpose:** Change request after the first implementation: the app should keep classifying until the user types `exit`, instead of ending after one classification.
**Inputs:** `solutions.md`, the current code in `src/`
**Expected output:** updated `ConsoleApp`, updated `ConsoleAppTest`, updated `solutions.md`

---

## Prompt

You are acting as a **senior Java developer**. Implement the following change request.

### Change request

Currently the application ends after the first valid classification. Change it so that:

1. After printing a result (`Equilateral`, `Isosceles` or `Scalene`), the app asks for the next triangle instead of exiting.
2. After an error message, the app also continues, as before.
3. The session ends when the user types **`exit`**:
   - Match it ignoring case and surrounding whitespace.
   - Print `Goodbye!` and return exit code `0`.
   - A line that only *contains* the word, such as `exit now`, is not the command. Treat it as normal (invalid) input.
4. Tell the user how to quit: add the line `Type 'exit' to quit.` to the welcome text, after the instructions.
5. Print `Please input triangle sides:` before **every** input line.
6. End of input (`readLine()` returns `null`) must still end the loop, so that piped input can't loop forever:
   - Print `Input ended. Exiting.` and return exit code `0`. Running out of piped lines is now a normal way to finish.
7. A read failure (`IOException`) still prints `Error: Could not read input. Exiting.` and returns exit code `1`.

### Constraints

- Change only the console layer (`ConsoleApp`) and its tests. Parsing, validation, the error messages and classification must stay exactly as they are.
- Keep `ConsoleApp` testable: injected streams, returns an exit code, and no `System.exit` outside `Main`.
- Update `solutions.md` so it stays the binding specification:
  - Section 6 (interaction flow): record the change and its reasons.
  - Section 8 (edge-case table): add rows for the new behaviour.
  - Section 9.2 (console behaviour and example session) and section 9.5 (tests).
  - Section 10 (resolved decisions).

### Tests

Update `ConsoleAppTest` to check the full transcript and the exit code for:
- several classifications in one session, ending with `exit`
- errors that don't end the session
- the `exit` command in different cases and with surrounding whitespace
- text that contains `exit` but isn't the command
- end of input right away, and end of input after some lines
- a read failure

### Verification

- All tests pass.
- Run the jar with several inputs followed by `exit`, and check the transcript.
