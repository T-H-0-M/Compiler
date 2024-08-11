#!/bin/bash
echo "Compiling Java program..."
rm *.class
javac A1.java
if [ $? -ne 0 ]; then
    echo "Compilation failed."
    exit 1
fi
echo "Compilation successful."
echo
echo "Running Java program..."
java A1 ../testfiles/SimpleWorking.cd
