package com.nervestaple.jlox.interpreter;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {

    public final String name;
    private final Map<String, LoxFunction> methods;
    private final  LoxClass superclass;

    public LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    public LoxFunction findMethod(LoxInstance instance, String name) {
        if (methods.containsKey(name)) {
            return methods.get(name).bind(instance);
        }

        if (superclass != null) {
            return superclass.findMethod(instance, name);
        }

        return null;
    }

    @Override
    public int arity() {
        LoxFunction initializer = methods.get("init");
        if(initializer == null) {
            return 0;
        }

        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);

        LoxFunction initializer = methods.get("init");
        if(initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    @Override
    public String toString() {
        return name;
    }
}
