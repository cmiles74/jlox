package com.nervestaple.jlox.parser;

import com.nervestaple.jlox.scanner.Token;

import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }


}
