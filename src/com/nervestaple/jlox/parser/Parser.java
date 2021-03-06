package com.nervestaple.jlox.parser;

import com.nervestaple.jlox.Lox;
import com.nervestaple.jlox.scanner.Token;
import com.nervestaple.jlox.scanner.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import static com.nervestaple.jlox.scanner.TokenType.*;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {

        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    public static class ParseError extends RuntimeException {

    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private Stmt declaration() {

        try {

            if (match(CLASS)) {
                return classDeclaration();
            }

            if (match(FUN)) {
                return function("function");
            }

            if (match(VAR)) {
                return varDeclaration();
            }

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {

        Token name = consume(IDENTIFIER, "Expecting a class name");

        Expr.Variable superclass = null;
        if (match(LESS)) {
            consume(IDENTIFIER, "Expecting a superclass name");
            superclass = new Expr.Variable(previous());
        }

        consume(LEFT_BRACE, "Expecting '{' before the class body");

        List<Stmt.Function> methods = new ArrayList<>();
        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("method"));
        }

        consume(RIGHT_BRACE, "Expecting '}' after the class body");

        return new Stmt.Class(name, superclass, methods);
    }

    private Stmt varDeclaration() {

        Token name = consume(IDENTIFIER, "Expecting a variable name");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expecting \";\" after variable declaration");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {

        if (match(FOR)) {
            return forStatement();
        }

        if(match(IF)) {
            return ifStatement();
        }

        if (match(PRINT)) {
            return printStatement();
        }

        if (match(RETURN)) {
            return returnStatement();
        }

        if (match(WHILE)) {
            return whileStatement();
        }

        if (match(LEFT_BRACE)) {
            return new Stmt.Block(block());
        }

        return expressionStatement();
    }

    private Stmt forStatement() {

        consume(LEFT_PAREN, "Expected '(' after 'for'");

        Stmt initializer = null;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if(!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expcted ';' after loop condition");

        Expr increment = null;
        if(!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expected ')' after for clause");

        Stmt body = statement();

        // increment
        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(
                    body,
                    new Stmt.Expression(increment)
            ));
        }

        // condition
        if (condition == null) {
            condition = new Expr.Literal(true);
        }
        body = new Stmt.While(condition, body);

        // initializer
        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Stmt whileStatement() {

        consume(LEFT_PAREN, "Expected '(' after 'while'");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after condition");
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private Stmt ifStatement() {

        consume(LEFT_PAREN, "Expecting '(' after 'if'");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expecting ')' after if condition");

        Stmt thenBranch = statement();

        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {

        Expr value = expression();
        consume(SEMICOLON, "Expecting \";\" after value");
        return new Stmt.Print(value);
    }

    private Stmt returnStatement() {

        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expectinng ';' after return value");
        return new Stmt.Return(keyword, value);
    }

    private Stmt expressionStatement() {

        Expr expr = expression();
        consume(SEMICOLON, "Expecting \";\" after expression");
        return new Stmt.Expression(expr);
    }

    private Stmt.Function function(String kind) {

        Token name = consume(IDENTIFIER, "Expecting " + kind + " name");

        consume(LEFT_PAREN, "Expecting '(' after " + kind + " name");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 8) {
                    error(peek(), "Cannot have more than 8 parameters");
                }

                parameters.add(consume(IDENTIFIER, "Expeccting parameter name"));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expeccting ')' after parameters");

        consume(LEFT_BRACE, "Expecting '{' before " + kind + " body");
        List<Stmt> body = block();

        return new Stmt.Function(name, parameters, body);
    }

    private List<Stmt> block() {

        List<Stmt> statements = new ArrayList<>();

        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expected \"}\" after block");
        return statements;
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {

        Expr expr = or();

        if (match(EQUAL)) {

            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object, get.name, value);
            }

            error(equals, "Invalid assignment target");
        }

        return expr;
    }

    private Expr or() {

        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {

        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {

        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {

            Token operator = previous();

            try {

                Expr right = comparison();
                expr = new Expr.Binary(expr, operator, right);
            } catch (ParseError error) {
                throw error(peek(), "Expecting right-hand operand after " + previous().lexeme);
            }
        }

        return expr;
    }

    private Expr comparison() {

        Expr expr = addition();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr addition() {

        Expr expr = multiplication();

        while(match(MINUS, PLUS)) {

            Token operator = previous();

            try {
                Expr right = multiplication();
                expr = new Expr.Binary(expr, operator, right);
            } catch (ParseError error) {
                throw error(peek(), "Expecting right-hand operand after " + previous().lexeme);
            }
        }

        return expr;
    }

    private Expr multiplication() {

        Expr expr = unary();

        while (match(SLASH, STAR)) {

            Token operator = previous();

            try {
                Expr right = unary();
                expr = new Expr.Binary(expr, operator, right);
            } catch (ParseError error) {
                throw error(peek(), "Expecting right-hand operand after " + previous().lexeme);
            }
        }

        return expr;
    }

    private Expr unary() {

        if (match(BANG, MINUS)) {

            Token operator = previous();

            try {
                Expr right = unary();
                return new Expr.Unary(operator, right);
            } catch (ParseError error) {
                throw error(peek(), "Expecting right-hand operand after " + previous().lexeme);
            }
        }

        return call();
    }

    private Expr call() {

        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expecting a property name after '.'");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {

        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if(arguments.size() >= 8) {
                    error(peek(), "Cannot have more than 8 arguments");
                }

                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expecting ')' after arguments");

        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {

        if (match(FALSE)) {
            return new Expr.Literal(false);
        }

        if (match(TRUE)) {
            return new Expr.Literal(true);
        }

        if (match(NIL)) {
            return new Expr.Literal(null);
        }

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(SUPER)) {

            Token keyword = previous();
            consume(DOT, "Expecting '.' and method name after 'super'");
            Token method = consume(IDENTIFIER, "Expecting superclass method name after '.'");
            return new Expr.Super(keyword, method);
        }

        if (match(THIS)) {
            return new Expr.This(previous());
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PAREN)) {

            Expr expr = expression();
            consume(RIGHT_PAREN, "Expecting \")\" after expression \"" + previous().lexeme + "\"");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expecting expression");
    }

    private void synchronize() {

        advance();

        while (!isAtEnd()) {

            if (previous().type == SEMICOLON) {
                return;
            }

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    private Token consume(TokenType type, String message) {

        if (check(type)) {
            return advance();
        }

        throw error(peek(), message);
    }

    private boolean match(TokenType... types) {

        for (TokenType type : types) {

            if(check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType tokenType) {

        if (isAtEnd()) {
            return false;
        }

        return peek().type == tokenType;
    }

    private Token advance() {

        if (!isAtEnd()) {
            current++;
        }

        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
