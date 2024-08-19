@echo on

set "folder=../TestFiles"
javac A1.java

for %%F in ("%folder%\*") do (
    java A1 %%F
)
