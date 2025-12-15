# CD24 Compiler

A Java-based compiler for the CD24 programming language that translates CD24 source code into SM24 machine code.

## Overview

This is a complete 4-stage compiler implementation featuring lexical analysis, syntax parsing with AST generation, semantic analysis, and SM24 code generation. The compiler processes CD24 source files (.cd) and produces executable SM24 machine code (.sm24).

## Requirements

- Java Development Kit (JDK) 17 or higher

## Building the Compiler

Navigate to the src/ directory and compile the main entry point:

```bash
cd src
javac A3.java
```

Alternatively, use the provided build script which compiles, tests, and cleans up class files:

```bash
cd src
./compile.sh
```

## Usage

Run the compiler from the src/ directory:

```bash
java A3 <path-to-cd24-file>
```

The compiler generates an .sm24 output file in the current directory.

## Example

```bash
cd src
java A3 ../TestFiles/successful/Vet.cd
```

This compiles the Vet.cd program and generates the corresponding SM24 machine code.

## Language Features

The CD24 language supports:

**Data Types:**
- Primitive types: int, float, bool
- Composite types: structs, arrays
- Constants and type definitions

**Control Structures:**
- Conditionals: if/elif/else, switch/case
- Loops: for, repeat/until, do/while

**Operations:**
- Arithmetic: +, -, *, /, %, ^ (power)
- Logical: and, or, xor, not
- Relational: ==, !=, <, <=, >, >=
- Assignment variants: =, +=, -=, *=, /=

**I/O Operations:**
- input - read variables
- print - output expressions
- printline - output with newline

**Functions:**
- Function declarations with parameters
- Return values (or void)
- Function calls with arguments

## Compiler Architecture

The compiler implements a traditional multi-stage design:

1. **Scanner** (Lexical Analysis) - Tokenizes source code, detects lexical errors
2. **Parser** (Syntax Analysis) - Recursive descent parser, builds Abstract Syntax Tree (AST)
3. **Semantic Analyser** - Symbol table management, type checking, semantic validation
4. **Code Generator** - SM24 machine code generation with stack-based operations

## Test Suite

The TestFiles/ directory contains a comprehensive test suite organised by category:

- **successful/** - 9 programs that compile correctly
- **lexical-failures/** - 4 test cases for scanner error detection
- **syntactic-failures/** - 4 test cases for parser error detection
- **semantic-failures/** - 4 test cases for semantic error detection

## Project Structure

```
src/            Source code (A3.java, Scanner.java, Parser.java, SemanticAnalyser.java, CodeGenerator.java, etc.)
TestFiles/      Test cases organized by category
Documentation/  Grammar specification, First and Follow sets
```

## Documentation

- **Grammar.txt** - Complete CD24 language grammar
- **First Sets.txt** - First sets for parser implementation
- **Follow Sets.txt** - Follow sets for parser implementation

## Additional Resources

For syntax highlighting when writing CD24 code, check out the [CD24 Syntax Highlighting Extension](https://github.com/T-H-0-M/CD24-Syntax-Highlighting)
