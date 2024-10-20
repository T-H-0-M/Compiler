@echo on

del /s /q *.class
del /s /q *.lst
javac -d ./out/ *.java
java A3.java

