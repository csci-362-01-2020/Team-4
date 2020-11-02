#!/bin/bash

cd ../project/src/org/eclipse/stem/test/driver/
find -name "*.java" -print -a -exec javac -cp "../../../../../../dependencies/*" {} \;
find -mindepth 1 -maxdepth 1 -type d \( -exec cp -r {} "../../../../../../../testCasesExecutables/org/eclipse/stem/test/driver/" \; \)
find -name "*.class" -exec rm {} \;
cd ../../../../../../../testCasesExecutables/org/eclipse/stem/test/driver/
find -name "*.java" -exec rm {} \;
