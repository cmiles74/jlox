package com.nervestaple.jlox;

import java.io.IOException;

import static java.lang.System.out;

public class Main {

    public static void main(String[] args) {

        if (args.length > 1) {
            out.println("Usage: jlox [script]");
        } else {

            try {

                if (args.length == 1) {
                    Lox.runFile(args[0]);
                } else {
                    Lox.runPrompt();
                }
            } catch (IOException exception) {
                out.println(exception);
            }
        }
    }
}
