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
