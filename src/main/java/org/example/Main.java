package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Entry point. Connects the console to {@link ConsoleApp} and exits with its exit code.
 */
public final class Main {

    private Main() {
    }

    /**
     * Starts the interactive Triangle Classifier.
     *
     * @param args ignored; input is read from the console
     */
    public static void main(String[] args) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int exitCode = new ConsoleApp(in, System.out).run();
        System.out.flush();
        System.exit(exitCode);
    }
}
