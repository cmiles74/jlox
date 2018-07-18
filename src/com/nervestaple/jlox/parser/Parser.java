package com.nervestaple.jlox.parser;

import com.nervestaple.jlox.Lox;
import com.nervestaple.jlox.scanner.Token;
import com.nervestaple.jlox.scanner.TokenType;

import java.util.ArrayList;
import java.util.List;

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
            if (match(VAR)) {
                return varDeclaration();
            } else {
                return statement();
            }
        } catch (ParseError error) {
            synchronize();
            return null;
        }
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

        if (match(PRINT)) {
            return printStatement();
        }

        if (match(LEFT_BRACE)) {
            return new Stmt.Block(block());
        }

        return expressionStatement();
    }

    private Stmt printStatement() {

        Expr value = expression();
        consume(SEMICOLON, "Expecting \";\" after value");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {

        Expr expr = expression();
        consume(SEMICOLON, "Expecting \";\" after expression");
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {

        List<Stmt> statements = new ArrayList<>();

        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expeccted \"}\" after block");
        return statements;
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {

        Expr expr = ternary();

        if (match(EQUAL)) {

            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target");
        }

        return expr;
    }

    private Expr ternary() {

        Expr expr = equality();

        while (match(TERN_OP)) {

            Token operator = previous();

            try {
                Expr right = ternaryThen();
                expr = new Expr.Binary(expr, operator, right);
            } catch (ParseError error) {
                throw error(peek(), "Error parsing expression after ternary \"?\" operator");
            }
        }

        return expr;
    }

    private Expr ternaryThen() {

        Expr expr = equality();

        consume(TERN_ELSE, "Expecting \":\" for ternary operation");

        Token operator = previous();

        try {
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        } catch (ParseError error) {
            throw error(peek(), "Expecting expression after \":\" for ternary operation");
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

        return primary();
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
