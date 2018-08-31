package com.nervestaple.jlox;

import com.nervestaple.jlox.interpreter.Interpreter;
import com.nervestaple.jlox.interpreter.RuntimeError;
import com.nervestaple.jlox.parser.Expr;
import com.nervestaple.jlox.parser.Parser;
import com.nervestaple.jlox.parser.Stmt;
import com.nervestaple.jlox.scanner.Scanner;
import com.nervestaple.jlox.scanner.Token;
import com.nervestaple.jlox.scanner.TokenType;

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

    private static boolean hadRuntimeError = false;

    private static final Interpreter interpreter = new Interpreter();

    public static void runtimeError(RuntimeError error) {

        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    public static void error(Token token, String message) {

        if(token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    private static void report(int line, String where, String message) {
        err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    public static void runFile(String path) throws IOException {

        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // indicate that we've encountered an error
        if (hadError) {
            System.exit(65);
        }

        if(hadRuntimeError) {
            System.exit(70);
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
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // stop if there was an error
        if (hadError) {
            return;
        }

        interpreter.interpret(statements);
    }
}
