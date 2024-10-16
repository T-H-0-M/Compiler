#!/bin/bash
javac A3.java
if [ $? -ne 0 ]; then
    echo "Compilation failed."
    exit 1
fi
# java A3 ../testfiles/successful/Vet.cd
java A3 ../testfiles/SimpleWorking.cd
# java A3 ../testfiles/successful/Arrays.cd
# java A3 ../testfiles/successful/SimpleVariables.cd
# java A3 ../testfiles/ExampleAST.cd
# java A3 ../testfiles/Simplest_Program_Ben.cd
# java A3 ../TestFiles/a.txt

rm *.class
echo "Execution completed. Class files have been removed."
