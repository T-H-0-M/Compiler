# CD24 Compiler

A Java-based compiler for the CD24 programming language.

## Overview

This project implements a compiler for the CD24 programming language using Java.
The compiler translates CD24 source code into SM24 machine code.

## Current Features

## Eventual Features

- Full support for the CD24 language specification
- Efficient lexical analysis and parsing
- Semantic analysis and type checking
- Intermediate code generation
- Optimization passes
- Target code generation

## Requirements

- Java Development Kit (JDK) 17 or higher

## Building the Compiler

To build the compiler, run the following command in the project root directory:

`javac A1.java`

## Usage

After building the project, you can run the compiler as follows:

`java A1 <path-to-source-file>`

Replace `<path-to-source-file>` with the path to your cd24 source code file.

## Command-line Options

<!--TODO: Complete this-->

## Example

`java A1.jar  testfiles/source.cd`

This command compiles the `source.cd` file with optimizations enabled and
generates the SM24 machine code in a file named `output.sm24`.

## Development Road Map

Currently there are 3 stages of development -

- [ ] Scanner
- [ ] Parser
- [ ] Code Generator

Currently in progress is the Scanner, its estimated completion date is the
16/08/24.

## Other Notes

For writing CD24 code, check out the
[CD24 code highlighting extension](https://github.com/T-H-0-M/CD24-Syntax-Highlighting)

