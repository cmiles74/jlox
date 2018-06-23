package com.nervestaple.jlox;

import com.nervestaple.jlox.scanner.Scanner;
import com.nervestaple.jlox.scanner.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.System.*;

public class Lox {

    private static boolean hadError = false;

    public static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        err.println("[line " + line + "] Error " + where + ": " + message);
        hadError = true;
    }

    public static void runFile(String path) throws IOException {

        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // indicate that we've encountered an error
        if (hadError) {
            System.exit(0);
        }
    }

    public static void runPrompt() throws IOException {

        InputStreamReader input = new InputStreamReader(in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {

            out.print("> ");
            run(reader.readLine());
            hadError = false;
        }
    }

    private static void run(String source) {

        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        for (Token token : tokens) {
            out.println(token);
        }
    }
}
