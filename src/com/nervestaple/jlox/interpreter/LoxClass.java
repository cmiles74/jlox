package com.nervestaple.jlox.interpreter;

public class LoxClass {

    public final String name;

    public LoxClass(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
