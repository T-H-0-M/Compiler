@echo on

del /s /q *.class
del /s /q *.lst
set "folder=../TestFiles"
javac A3.java
java A3 C:\CODE\Compiler\TestFiles\successful\Arrays.cd