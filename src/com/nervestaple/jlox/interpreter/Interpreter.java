package com.nervestaple.jlox.interpreter;

import com.nervestaple.jlox.parser.Expr;

public class Interpreter implements Expr.Visitor<Object> {

    @Override
    public Object visit(Expr.Binary expr) {

        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {

            case MINUS:
                return (double) left - (double) right;
            case SLASH:
                return (double) left / (double) right;
            case STAR:
                return (double) left * (double) right;
            case PLUS:
                if(left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if(left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
            case GREATER:
                return (double) left > (double) right;
            case GREATER_EQUAL:
                return (double) left >= (double) right;
            case LESS:
                return (double) left < (double) right;
            case LESS_EQUAL:
                return (double) left <= (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        return null;
    }

    @Override
    public Object visit(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visit(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visit(Expr.Unary expr) {

        Object right = evaluate(expr.right);
        switch (expr.operator.type) {

            case BANG:
                return !isTruthy(right);
            case MINUS:
                return - (double) right;
        }

        return null;
    }

    private boolean isEqual(Object a, Object b) {

        if (a == null && b ==null) {
            return true;
        }

        if (a == null) {
            return false;
        }

        return a.equals(b);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Object object) {

        if (object == null) {
            return false;
        }

        if (object instanceof Boolean) {
            return (boolean) object;
        }

        return true;
    }
}