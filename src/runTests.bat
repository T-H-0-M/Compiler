@echo on

del /s /q *.class
del /s /q *.lst
set "folder=../TestFiles"
javac A2.java

for %%F in ("%folder%\*") do (
    java A2 %%F
)
