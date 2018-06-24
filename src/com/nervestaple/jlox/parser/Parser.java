package com.nervestaple.jlox.parser;

import com.nervestaple.jlox.Lox;
import com.nervestaple.jlox.scanner.Token;
import com.nervestaple.jlox.scanner.TokenType;

import java.util.List;

import static com.nervestaple.jlox.scanner.TokenType.*;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expr parse() {

        try {

            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    public static class ParseError extends RuntimeException {

    }

    private Expr expression() {
        return ternary();
    }

    private Expr ternary() {

        Expr expr = equality();

        while (match(TERN_OP)) {

            Token operator = previous();

            try {
                Expr right = ternaryThen();
                expr = new Expr.Binary(expr, operator, right);
            } catch (ParseError error) {
                throw error(peek(), "Expecting expression after ternary (\"?\") operator");
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

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
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
