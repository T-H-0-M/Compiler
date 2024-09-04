#!/bin/bash
javac A2.java
if [ $? -ne 0 ]; then
    echo "Compilation failed."
    exit 1
fi
# java A2 ../testfiles/SimpleWorking.cd
java A2 ./b.txt
# java A1 ../testfiles/dan.cd
# java A1 ~/Downloads/test.txt
rm *.class
echo "Execution completed. Class files have been removed."
