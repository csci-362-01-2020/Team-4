#!/bin/bash

cd scripts
cd ../project/src
find -name "*.java" -print -a -exec javac -cp ".:../dependencies/*" {} \;
cd org/eclipse/stem/test/driver/

# Copy TestReporter.class to testCasesExecutables
find -mindepth 1 -maxdepth 1 -type f \( -exec cp {} "../../../../../../../testCasesExecutables/org/eclipse/stem/test/driver/" \; \)
# Copy rest of drivers to testCasesExecutables
find -mindepth 1 -maxdepth 1 -type d \( -exec cp -r {} "../../../../../../../testCasesExecutables/org/eclipse/stem/test/driver/" \; \)

find -name "*.class" -exec rm {} \;
cd ../../../../../../../testCasesExecutables/org/eclipse/stem/test/driver/
find -name "*.java" -exec rm {} \;
