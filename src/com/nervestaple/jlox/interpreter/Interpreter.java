package com.nervestaple.jlox.interpreter;

import com.nervestaple.jlox.Lox;
import com.nervestaple.jlox.parser.Expr;
import com.nervestaple.jlox.parser.Stmt;
import com.nervestaple.jlox.scanner.Token;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    public void interpret(List<Stmt> statements) {

        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Void visit(Stmt.Expression stmt) {

        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visit(Stmt.Print stmt) {

        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Object visit(Expr.Binary expr) {

        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {

            case MINUS:
                checkNumberOperand(expr.operator, left, right);
                return (double) left - (double) right;
            case SLASH:
                checkNumberOperand(expr.operator, left, right);

                if((double) right == 0) {
                    throw new RuntimeError(expr.operator, "Cannot divide by zero");
                }

                return (double) left / (double) right;
            case STAR:
                checkNumberOperand(expr.operator, left, right);
                return (double) left * (double) right;
            case PLUS:
                if(left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if(left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }

                if(left instanceof String || right instanceof String) {
                    return stringify(left) + stringify(right);
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers or strings");
            case GREATER:
                checkNumberOperand(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperand(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperand(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperand(expr.operator, left, right);
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
                checkNumberOperand(expr.operator, right);
                return - (double) right;
        }

        return null;
    }

    private String stringify(Object object) {

        if (object == null) {
            return "nil";
        }

        if (object instanceof Double) {
            String text = object.toString();

            // suppress ".0" when displaying Double values
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }

            return text;
        }

        return object.toString();
    }

    private void checkNumberOperand(Token operator, Object operand) {

        if (operand instanceof Double) {
            return;
        }

        throw new RuntimeError(operator, "Operand must be a number");
    }

    private void checkNumberOperand(Token operator, Object left, Object right) {

        if (left instanceof Double && right instanceof Double) {
            return;
        }

        throw new RuntimeError(operator, "Operands must be a number");
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
