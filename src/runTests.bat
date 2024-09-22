@echo on

del /s /q *.class
del /s /q *.lst
set "folder=../TestFiles"
javac A2.java
java A2 C:\CODE\Compiler\TestFiles\SimpleWorking.cd