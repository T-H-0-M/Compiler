#!/bin/bash
javac A1.java
if [ $? -ne 0 ]; then
    echo "Compilation failed."
    exit 1
fi
# java A1 ../testfiles/SimpleWorking.cd
java A1 ~/Downloads/test.txt
rm *.class
echo "Execution completed. Class files have been removed."
