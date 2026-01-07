# CD24 Compiler

A Java compiler that turns CD24 source code into SM24 machine code.

## What you need

- Java 17+
- Maven 3.6+

## How to run it

Build the JAR:

```bash
mvn clean package
```

Compile a CD24 file:

```bash
java -jar target/cd24-compiler-0.1.0.jar path/to/file.cd
```

Or just use Maven directly:

```bash
mvn exec:java -Dexec.mainClass="com.compiler.cd24.A3" -Dexec.args="path/to/file.cd"
```

This creates two files in your current directory:

- `filename.lst` - listing with tokens and any errors
- `filename.mod` - the compiled SM24 machine code

## Example

```bash
java -jar target/cd24-compiler-0.1.0.jar TestFiles/successful/SimpleVariables.cd
```

## What it does

Full 4-stage compiler: lexer → parser → semantic analysis → code generation

The CD24 language has the usual stuff: variables (int, float, bool), arrays,
structs, functions, if/else, loops, I/O, and all the standard operators.

## Test files

Check out `TestFiles/` for examples:

- `successful/` - programs that compile
- `lexical-failures/` - lexer error tests
- `syntactic-failures/` - parser error tests
- `semantic-failures/` - semantic error tests

## Extras

Want syntax highlighting for CD24? I've also build a plugin for nvim + vscode -
[CD24 VSCode extension](https://github.com/T-H-0-M/CD24-Syntax-Highlighting)
