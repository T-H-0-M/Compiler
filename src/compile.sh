#!/bin/bash
rm *.class
javac A1.java
if [ $? -ne 0 ]; then
    echo "Compilation failed."
    exit 1
fi
java A1 ../testfiles/SimpleWorking.cd
