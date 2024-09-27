#!/bin/bash
javac A2.java
if [ $? -ne 0 ]; then
    echo "Compilation failed."
    exit 1
fi
java A2 ../testfiles/successful/Vet.cd
# java A2 ../testfiles/SimpleWorking.cd
# java A2 ../testfiles/successful/Arrays.cd
# java A2 ./Arrays.cd
# java A2 ../testfiles/successful/SimpleVariables.cd
# java A2 ../testfiles/ExampleAST.cd
# java A2 ../testfiles/Simplest_Program_Ben.cd
# java A2 ../TestFiles/a.txt
# java A1 ../testfiles/dan.cd
# java A1 ~/Downloads/test.txt
rm *.class
echo "Execution completed. Class files have been removed."
