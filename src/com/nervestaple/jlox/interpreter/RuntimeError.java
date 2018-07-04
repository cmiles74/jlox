package com.nervestaple.jlox.interpreter;

import com.nervestaple.jlox.scanner.Token;

public class RuntimeError extends RuntimeException {

    public final Token token;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
