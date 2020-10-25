#!/bin/bash

cd ../project/src/org/eclipse/stem/test/driver/
find -regex ".*.java" -exec javac -cp "../../../../../../dependencies/*" {} \;
pwd
