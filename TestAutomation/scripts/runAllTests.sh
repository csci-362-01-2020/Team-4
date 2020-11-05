#!/bin/bash

cd ../testCases/workingTestCases

readarray test_nums < <(find -name "*.txt" -exec awk '{if(NR==1) print $0}' '{}' \;)
readarray drivers < <(find -name "*.txt" -exec awk '{if(NR==5) print $0}' '{}' \;)
readarray inputs < <(find -name "*.txt" -exec awk '{if(NR==6) print $0}' '{}' \;)
readarray oracles < <(find -name "*.txt" -exec awk '{if(NR==7) print $0}' '{}' \;)

cd ../../testCasesExecutables
for ind in ${!drivers[@]}
do	
	test_num=$(echo ${test_nums[ind]} | awk '{sub(/Test Number: /,"")} 1' );
	driver=$(echo ${drivers[ind]} | awk '{sub(/Driver: /,"")} 1' );
	input=$(echo ${inputs[ind]} | awk '{sub(/Inputs: /,"")} 1' | tr -d ',');
	oracle=$(echo ${oracles[ind]} | awk '{sub(/Oracles: /,"")} 1' | tr -d ',');
	
	echo "Executing test case $test_num for:";
	echo "Driver: $driver";
	echo "Inputs: $input";
	echo "Oracles: $oracle";
	
	java -cp ".:../project/dependencies/*" $driver $input $oracle $test_num;
done
