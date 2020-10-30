#!/bin/bash

n=1
while IFS= read -r line; do
	drivers[$n]=$line;
	n=$((n+1));
done < data/driverlist.txt

cd ../testCases/

n=1
while [[ $n -le ${#drivers[@]} ]]
do	
	test_case_folder=$(echo ${drivers[$n]} | tr . /) 
	cd $test_case_folder
	
	java -cp ".:../dependencies/*" ${drivers[$n]};
	n=$((n+1));
done
