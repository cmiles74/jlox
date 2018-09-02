package com.nervestaple.jlox.interpreter;

import com.nervestaple.jlox.parser.Stmt;

import java.util.List;

public class LoxFunction implements LoxCallable {

    private final Stmt.Function declaration;

    public LoxFunction(Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {

        Environment environment = new Environment(interpreter.global);
        for (int index = 0; index < declaration.params.size(); index++) {
            environment.define(declaration.params.get(index).lexeme, arguments.get(index));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
