# JLox: A Lox Interpreter in Java

This project represnets my progress as I work through the exercised and 
examples on the ["Crafting Interpreters"](http://craftinginterpreters.com) 
website.

# Building the Project

First you will need to generate the source code for the AST classes.

```
$ mkdir -p out/production/jlox
$ javac -d out/production/jlox src/com/nervestaple/jlox/tool/*
```

The "out" directory will now contain the code generator. Next, run that 
generator.

```
$ java -cp out/production/jlox com.nervestaple.jlox.tool.GenerateAst generated/com/nervestaple/jlox/parser
```

With the AST classes generated, you can now open up the project in your IDE 
(i.e. IntelliJ IDEA) and compile the rest of the project.

# Running Lox Code

You can run Lox code by passing the path of the file to the main method of the 
Lox class. A test file is included, you can give that a try.

```
$ java -cp out/production/jlox com.nervestaple.jlox.Main "test/fibonacci.lox"
``` 
