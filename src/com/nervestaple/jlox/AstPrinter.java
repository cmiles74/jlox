package com.nervestaple.jlox;

import com.nervestaple.jlox.parser.Expr;

public class AstPrinter implements Expr.Visitor<String> {

    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visit(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visit(Expr.Call expr) {

        StringBuilder arguments = new StringBuilder();
        for(Expr exprArg : expr.arguments) {
            if (arguments.length() > 0) {
                arguments.append(", ");
            }

            arguments.append(exprArg.accept(this));
        }

        return expr.callee + "(" + arguments + ")";
    }

    @Override
    public String visit(Expr.Get expr) {
        return expr.object + "." + expr.name;
    }

    @Override
    public String visit(Expr.Set expr) {
        return expr.object + "." + expr.name + " = " + expr.value;
    }

    @Override
    public String visit(Expr.This expr) {
        return expr.keyword.toString();
    }

    @Override
    public String visit(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visit(Expr.Literal expr) {
        if(expr.value == null) {
            return "nil";
        }

        return expr.value.toString();
    }

    @Override
    public String visit(Expr.Logical expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visit(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visit(Expr.Variable expr) {
        return "var " + expr.name.lexeme;
    }

    @Override
    public String visit(Expr.Assign expr) {
        return expr.name + " = " + expr.value;
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for(Expr expr: exprs){
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}
