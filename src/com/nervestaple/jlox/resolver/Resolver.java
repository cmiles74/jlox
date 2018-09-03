package com.nervestaple.jlox.resolver;

import com.nervestaple.jlox.Lox;
import com.nervestaple.jlox.interpreter.Interpreter;
import com.nervestaple.jlox.parser.Expr;
import com.nervestaple.jlox.parser.Stmt;
import com.nervestaple.jlox.scanner.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

enum FunctionType {
    NONE,
    FUNCTION
}

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public Void visit(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visit(Stmt.Var stmt) {

        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visit(Expr.Variable expr) {

        if (!scopes.isEmpty()
                && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            Lox.error(expr.name, "Cannot read from a local variable in it's own initializer");
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visit(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visit(Stmt.Function stmt) {

        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visit(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visit(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);

        if (stmt.elseBranch != null) {
            resolve(stmt.elseBranch);
        }

        return null;
    }

    @Override
    public Void visit(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visit(Stmt.Return stmt) {

        if(currentFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Cannot return from outside a function");
        }

        if (stmt.value != null) {
            resolve(stmt.value);
        }

        return null;
    }

    @Override
    public Void visit(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visit(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visit(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr argument : expr.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visit(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visit(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visit(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visit(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    public void resolve(List<Stmt> statements) {

        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    public void resolve(Stmt statement) {
        statement.accept(this);
    }

    public void resolve(Expr expr) {
        expr.accept(this);
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) {

        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }

        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    private void resolveLocal(Expr expr, Token name) {

        for (int index = scopes.size() - 1; index >= 0; index--) {
            if (scopes.get(index).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - index);
                return;
            }
        }
    }

    private void declare(Token name) {

        if (scopes.isEmpty()) {
            return;
        }

        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name, "Variable with this name is already declared in scope");
        }

        scope.put(name.lexeme, false);
    }

    private void define(Token name) {

        if (scopes.isEmpty()) {
            return;
        }

        scopes.peek().put(name.lexeme, true);
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopes.pop();
    }
}
